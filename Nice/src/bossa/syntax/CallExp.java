/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import java.util.*;

import bossa.util.*;
import bossa.util.Debug;
import nice.tools.typing.Types;

import mlsub.typing.*;
import mlsub.typing.Polytype;
import mlsub.typing.Monotype;
import mlsub.typing.FunType;
import mlsub.typing.TupleType;
import mlsub.typing.Constraint;

import nice.tools.code.EnsureTypeProc;

/**
   A function call.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
public class CallExp extends Expression
{
  /**
     @param function the function to call
     @param arguments the call arguments
   */
  public CallExp(Expression function, Arguments arguments)
  {
    this(function, arguments, false, true);
  }

  /**
     @param function the function to call
     @param arguments the call arguments
     @param infix true if the first parameter was written before the function,
     using the dot notation: x1.f(x2, x3)
   */
  public CallExp(Expression function, Arguments arguments, 
		 boolean infix, boolean hasBrackets)
  {
    this.function = function;
    this.arguments = arguments;
    this.infix = infix;
    this.hasBrackets = hasBrackets;
  }

  public static CallExp create(Expression function, 
			       Expression param1)
  {
    List params = new LinkedList();
    params.add(new Arguments.Argument(param1));
    CallExp res = new CallExp(function, new Arguments(params));
    res.setLocation(function.location());
    return res;
  }
  
  public static CallExp create(Expression function, 
			       Expression param1, Expression param2)
  {
    List params = new ArrayList(2);
    params.add(new Arguments.Argument(param1));
    params.add(new Arguments.Argument(param2));
    CallExp res = new CallExp(function, new Arguments(params));
    res.setLocation(function.location());
    return res;
  }
  
  public static CallExp create(Expression function, 
			       Expression param1, Expression param2,
			       Expression param3)
  {
    List params = new ArrayList(2);
    params.add(new Arguments.Argument(param1));
    params.add(new Arguments.Argument(param2));
    params.add(new Arguments.Argument(param3));
    CallExp res = new CallExp(function, new Arguments(params));
    res.setLocation(function.location());
    return res;
  }
  
  public void addBlockArgument(Statement block, LocatedString name)
  {
    arguments.add(new FunExp(bossa.syntax.Constraint.True, new LinkedList(),
		block), name);
  } 

  /****************************************************************
   * Type checking
   ****************************************************************/

  static Polytype wellTyped(Polytype funt, Polytype[] parameters)
  {
    try{
      Polytype t = getType(funt, parameters, true);
      return t;
    }
    catch(ReportErrorEx e){}
    catch(TypingEx e){}
    catch(BadSizeEx e){}

    return null;
  }
  
  static Polytype getTypeAndReportErrors(Location loc,
					 Expression function,
					 Expression[] parameters)
  {
    Polytype[] paramTypes = Expression.getType(parameters);
    
    try{
      return getType(function.getType(), paramTypes, false);
    }
    catch(BadSizeEx e){
      User.error(loc, function.toString(Printable.detailed) + 
		 " expects " + e.expected + " parameters, " +
		 "not " + e.actual);
    }
    catch(ReportErrorEx e){
      User.error(loc, e.getMessage());
    }
    catch(TypingEx e){
      if(Typing.dbg) 
	Debug.println(e.getMessage());

      if(function.isFieldAccess())
	{
	  // There must be just one parameter in a field access
	  User.error(loc, parameters[0] + " has no field " + function);
	}
      else
	{
	  String end = "not within the domain of function \"" + function +"\"";
	  if(parameters.length >= 2)
	    User.error(loc,"Parameters \n"+
		       Util.map("(",", ",")",parameters) +
		       "\n of types \n" +
		       Util.map("(",",\n  ",")",paramTypes) +
		       "\n are "+end);
	  else
	    User.error(loc,"Parameter "+
		       Util.map("",", ","",parameters) +
		       " of type " +
		       Util.map("",", ","",paramTypes) +
		       " is "+end);      
	}
    }
    return null;
  }

  static class ReportErrorEx extends Exception {
    ReportErrorEx(String msg) { super(msg); }
  }
  
  /**
     @param tentative whether we only want to check if the call is valid
       (as during overloading resolution).
  */
  private static Polytype getType(Polytype type, Polytype[] parameters,
                                  boolean tentative)
    throws TypingEx, BadSizeEx, ReportErrorEx
  {
    Monotype m = type.getMonotype();

    // Check that the function cannot be null
    if (m.head() == null)
      throw new ReportErrorEx("Nullness check");
    try{
      Typing.leq(m.head(), PrimitiveType.sureTC);
    }
    catch(TypingEx ex) {
      throw new ReportErrorEx("This function may be null");
    }
    
    m = Types.rawType(m);

    // The function might not be of a functional kind.
    // Only usefull for null?
    // First reset the kind, that comes from a previous typing.
    if (m.getKind() == mlsub.typing.lowlevel.Engine.variablesConstraint)
      m.setKind(null);
    if (m.getKind() == null)
      m.setKind(FunTypeKind.get(parameters.length));

    m = m.equivalent();

    if (!(m instanceof FunType))
      throw new ReportErrorEx("Not a function");

    FunType funt = (FunType) m;

    Monotype[] dom = funt.domain();

    Constraint cst = type.getConstraint();
    final boolean doEnter = true; //Constraint.hasBinders(cst);
    
    if(doEnter) Typing.enter(tentative);

    boolean ok = false;
    try
      {
	try{ Constraint.enter(cst); }
	catch(TypingEx e) { 
	  throw new ReportErrorEx
	    ("The conditions for using this function are not fullfiled");
	}
    
	if(Typing.dbg)
	  {
	    Debug.println("Parameters:\n"+
			  Util.map("",", ","\n",parameters));
	    Debug.println("Domain:\n"+
			  Util.map("",", ","\n",dom));
	  }
	
	Typing.in(parameters, dom);
        ok = true;
      }
    finally{
      if(doEnter)
        // If we are in tentative mode and the call is valid, then
        // we make the effects last (that's only important when there are
        // existential type variables in the context).
	Typing.leave(tentative, tentative && ok);
    }

    return Polytype.apply(cst, funt, parameters);
  }

  /****************************************************************
   * Overloading resolution
   ****************************************************************/

  private boolean overloadingResolved;
  
  void resolveOverloading()
  {
    // do not resolve twice
    if(overloadingResolved)
      return;
    overloadingResolved = true;
    
    arguments.noOverloading();
    
    function = function.resolveOverloading(this);

    function.checkSpecialRequirements(arguments.computedExpressions);

    // make sure computedExpressions is valid.
    getType();
    // Allow expressions to know in what context they are used.
    // Important for litteral arrays and tuples.
    Expression.adjustToExpectedType(arguments.computedExpressions,
                                    Types.parameters(function.getType()));
  }

  void computeType()
  {
    resolveOverloading();
    if (type == null)
      // function should be a function abstraction here
      // no default arguments
      {
	type = getTypeAndReportErrors(location(), function, 
				      arguments.inOrder());
	arguments.computedExpressions = arguments.inOrder();
      }

    if (! type.isMonomorphic() && argTypes != null)
      {
	/* 
	   We construct the instantiated version of the function type:
	   the type of the function, constrained by the actual arguments. 
	   Then we simplify it. 
	   It is useful to constrain the arguments to have the expected 
	   bytecode types.
	*/
	instanciatedDomain = new Polytype
	  (type.getConstraint(), new TupleType(argTypes));
	instanciatedDomain = instanciatedDomain.cloneType();
	// By default, a polytype is supposed to be simplified.
	instanciatedDomain.setNotSimplified();
	instanciatedDomain.simplify();
      }

    if (! type.trySimplify())
      User.warning(this, "This call might have a type error, or this might be a bug in the compiler. \nPlease contact bonniot@users.sourceforge.net");
  }

  /** The types of the formal arguments of the function, in the same
      polymorphic instance as the computed type.
  */  
  Monotype[] argTypes;

  /** The type of the function, constrained by the actual arguments. */
  private Polytype instanciatedDomain;

  public boolean isAssignable()
  {
    resolveOverloading();
    return function.isFieldAccess();
  }

  /**
     @return the FieldAccess if this expression resolves to a field, 
     which is true if it is the application a of FieldAccess to an object 
     value. Returns null otherwise.
   */
  FieldAccess getField()
  {
    resolveOverloading();
    return function.getFieldAccessMethod();
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.expr.Expression compile()
  {
    gnu.expr.Expression res;
    gnu.expr.LetExp firstLetExp = null;
    gnu.expr.Expression letExpRes = null;
    if (localVars != null)
      {
        for(int i = localVars.length-1; i >= 0; i--)
	  {
            gnu.expr.Expression[] eVal = new gnu.expr.Expression[1];
            gnu.expr.LetExp letExp = new gnu.expr.LetExp(eVal);
            eVal[0] = localVars[i].variable.compile(letExp);
            if (i == localVars.length-1)
              {
		firstLetExp = letExp;
              }
            else
              {
                letExp.setBody(letExpRes);
              }

       	    letExpRes = letExp;
	  }
      }

    if (function.isFieldAccess())
      res = function.getFieldAccessMethod().compileAccess(compileParams());
    else
      res = new gnu.expr.ApplyExp(function.generateCodeInCallPosition(), 
                                  compileParams());
    location().write(res);

    if (firstLetExp != null)
      {
        firstLetExp.setBody(res);
	res = letExpRes;
      }

    return EnsureTypeProc.ensure(res, nice.tools.code.Types.javaType(type));
  }

  private gnu.expr.Expression[] compileParams()
  { 
    gnu.expr.Expression[] params = 
      Expression.compile(arguments.computedExpressions);

    // Make sure the arguments have the expected bytecode type,
    // matching the instantiated type of the (polymorphic) function.
    Monotype[] domain = null;
    if (instanciatedDomain != null)
      domain = ((TupleType) instanciatedDomain.getMonotype()).getComponents();
    if (domain != null)
      for (int i = 0; i < params.length; i++)
	params[i] = EnsureTypeProc.ensure
	  (params[i], nice.tools.code.Types.javaType(domain[i]));

    return params;
  }

  gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    if (!function.isFieldAccess())
      Internal.error(this, "Assignment to a call that is not a field access");

    FieldAccess access = function.getFieldAccessMethod();

    if (access.isFinal())
      User.error(this, "Field " + access + " is final");

    switch (arguments.size())
    {
    case 0:
      return access.compileAssign(value);
    case 1:
      return access.compileAssign(arguments.getExp(0).generateCode(), value);
    default:
      Internal.error(this, "A field access should have 0 or 1 parameter");
      return null;
    }
  }

  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    if (!infix)
      return function.toString(Printable.parsable) + arguments;

    if (declaringClass == null)
      return arguments.getExp(0) + "." + function + arguments.toStringInfix();

    if (function instanceof SymbolExp)
      {
	Definition def = ((SymbolExp)function).getSymbol().getDefinition();

	if (def instanceof RetypedJavaMethod)
          return function.toString() + arguments;

	if (def instanceof JavaFieldAccess)
	  return declaringClass.getName() + "." + 
		((JavaFieldAccess)def).fieldName + arguments;
      }

    return declaringClass.getName() + "." + function + arguments;
  }

  Expression function;
  protected Arguments arguments;
  
  /** true iff the first argument was written before the application: e.f(x) */
  final boolean infix;

  /** true iff this call was made using brackets (i.e. not like 'x.f'). */
  public final boolean hasBrackets;

  /** Class this static method is defined in, or null */
  gnu.bytecode.ClassType declaringClass = null;

  ExpLocalVariable[] localVars = null;
}
