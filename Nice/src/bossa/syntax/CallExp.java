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
import nice.tools.code.*;

import mlsub.typing.*;
import mlsub.typing.Polytype;
import mlsub.typing.Monotype;
import mlsub.typing.Constraint;
import mlsub.typing.MonotypeLeqCst;

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
    this.function = expChild(function);
    this.arguments = arguments;
    addChild(arguments);
  }

  /**
     @param function the function to call
     @param arguments the call arguments
     @param infix true if the first parameter was written before the function,
     using the dot notation: x1.f(x2, x3)
   */
  public CallExp(Expression function, Arguments arguments, boolean infix)
  {
    this(function, arguments);
    this.infix = infix;
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
  
  void findJavaClasses()
  {
    if (infix)
      {
	Expression e = arguments.getExp(0);
	// force the finding of class prefixes to methods
	// so that java classes are discovered early and put in global context
	if(e instanceof IdentExp)
	  {
	    ((IdentExp) e).enableClassExp = true;
	    e.resolveExp();
	  }
      }
  }
  
  void resolve()
  {
    Expression e = function.content();
    if (infix)
      // in infix applications, the symbol is either a
      // class method or a field name, so it must be found in
      // global scope
      {
	e.scope = Node.getGlobalScope();
      }

    if(e instanceof IdentExp)
      {
	IdentExp ie = (IdentExp) e;
	if (infix)
	  // it might be a package or class expression
	  ie.ignoreInexistant = true;
	//so type computation happens in OverloadedSymbolExp
	ie.alwaysOverloadedSymbol = true;
      }
  }
  
  /****************************************************************
   * Type checking
   ****************************************************************/

  static Polytype wellTyped(Polytype funt, Polytype[] parameters)
  {
    try{
      Polytype t = getType(funt, parameters);
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
      return getType(function.getType(), paramTypes);
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
  
  private static Polytype getType(Polytype funt,
				  Polytype[] parameters)
    throws TypingEx, BadSizeEx, ReportErrorEx
  {
    Monotype[] dom = funt.domain();
    Monotype codom = funt.codomain();

    if(dom==null || codom==null)
      throw new ReportErrorEx("Not a function");

    Constraint cst = funt.getConstraint();
    final boolean doEnter = true; //Constraint.hasBinders(cst);
    
    if(doEnter) Typing.enter();
    
    try
      {
	try{ Constraint.assert(cst); }
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
	
	Typing.in(parameters, Domain.fromMonotypes(dom));
      }
    finally{
      if(doEnter)
	Typing.leave();
    }

    //computes the resulting type

    /*
      Optimization:
      If we know codom is a constant,
      the constraint parameters<dom is useless.
    */
    if(codom.isRigid())
      return new Polytype(Constraint.True, codom);

    cst = Constraint.and
      (Polytype.getConstraint(parameters),
       cst,
       MonotypeLeqCst.constraint(Polytype.getMonotype(parameters),dom));
    
    return new Polytype(cst, codom);
  }

  /****************************************************************
   * Overloading resolution
   ****************************************************************/

  public Expression noOverloading()
  {
    PackageExp packageExp = arguments.packageExp();
    
    if (packageExp != null &&
	(function.content() instanceof IdentExp || 
	 function.content() instanceof SymbolExp))
      return ClassExp.create(packageExp, function.toString());
    else
      return this;
  }
  
  private boolean overloadingResolved;
  
  private void resolveOverloading()
  {
    // do not resolve twice
    if(overloadingResolved)
      return;
    overloadingResolved = true;
    
    arguments.noOverloading();
    resolveStaticClassPrefix();
    
    function = function.resolveOverloading(this);
  }

  /** Handle static functions, prefixed by the class name */
  private void resolveStaticClassPrefix()
  {
    if(infix)
      {
	declaringClass = arguments.staticClass();
	
	Expression funExp = function.content();
	    
	if(declaringClass == null)
	  // check that the function was found in scope
	  {
	    if (funExp instanceof IdentExp)
	      User.error(funExp, 
			 funExp + " is not declared");
	    return;
	  }
	
	// A static function is called
	LocatedString funName;
	    
	if(funExp instanceof OverloadedSymbolExp)
	  funName = ((OverloadedSymbolExp) funExp).ident;
	else if(funExp instanceof IdentExp)
	  funName = ((IdentExp) funExp).getIdent();
	else if(funExp instanceof SymbolExp)
	  funName = ((SymbolExp) funExp).getName();
	else
	  {
	    Internal.error("Unknown case " +
			   funExp.getClass() + " in CallExp");
	    return;
	  }

	List possibilities = new LinkedList();
	declaringClass.addMethods();
	int arity = arguments.size();
	    
	// search methods
	for(gnu.bytecode.Method method = declaringClass.getMethods();
	    method!=null; method = method.getNext())
	  if(method.getName().equals(funName.content) &&
	     (method.arg_types.length + 
	      (method.getStaticFlag() ? 0 : 1)) == arity)
	    {
	      MethodDeclaration md = 
		JavaMethod.addFetchedMethod(method);
	      if(md!=null)
		possibilities.add(md.symbol);
	      else
		Debug.println(method + " ignored");
	    }

	// search a field
	if (arity == 0)
	  {
	    gnu.bytecode.Field field = 
	      declaringClass.getField(funName.toString());
	    if(field!=null)
	      possibilities.add(JavaMethod.addFetchedMethod(field).symbol);
	  }
	    
	if (possibilities.size() == 0)
	  User.error(this, "class " + declaringClass.getName() +
		     " has no method or field " + funName);
	    
	function = new ExpressionRef
	  (new OverloadedSymbolExp(possibilities, funName,
				   function.content().getScope()));
      }
  }

  /** Hold real parameter, after default parameters
      and parameter reordering has been done.
  */
  Expression[] computedExpressions;
  
  void computeType()
  {
    resolveOverloading();
    if (type == null)
      // function should be a function abstraction here
      // no default arguments
      {
	type = getTypeAndReportErrors(location(), function, 
				      arguments.inOrder());
	computedExpressions = arguments.inOrder();
      }
    
    type.simplify();
  }

  boolean isAssignable()
  {
    resolveOverloading();
    return function.isFieldAccess();
  }
  
  /****************************************************************
   * Typechecking
   ****************************************************************/

  void typecheck()
  {
    // forces computation of the type if not done.
    Polytype t = getType();
    
    // Prepare the bytecode type for EnsureTypeProc
    if (t.getConstraint() != Constraint.True)
      try{
	Typing.enter();
	try{
	  Constraint.assert(t.getConstraint());
	  Types.setBytecodeType(t.getMonotype());
	}
	finally{
	  Typing.leave();
	}
      }
      catch(TypingEx e){
      }
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.expr.Expression compile()
  {
    // wraps the arguments that reference methods into LambdaExps
    gnu.expr.Expression[] params = Expression.compile(computedExpressions);
    for(int i=0; i<params.length; i++)
      if(params[i] instanceof gnu.expr.QuoteExp)
	{
	  gnu.expr.QuoteExp q = (gnu.expr.QuoteExp) params[i];
	  if(q.getValue() instanceof gnu.expr.PrimProcedure)
	    params[i] = ((gnu.expr.PrimProcedure) q.getValue()).wrapInLambda();
	}
    
    gnu.expr.Expression res;
    if (function.isFieldAccess())
      res = function.getFieldAccessMethod().compileAccess(arguments);
    else
      res = new gnu.expr.ApplyExp(function.generateCode(), params);

    return Inline.inline(new EnsureTypeProc(Types.javaType(getType())), res);
  }
  
  gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    if (!function.isFieldAccess())
      Internal.error(this, "Assignment to a call that is not a field access");
    if (arguments.size() != 1)
      Internal.error(this, "A field access should have 1 parameter");

    return function.getFieldAccessMethod().compileAssign
      (arguments.getExp(0), value);
  }

  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    if(infix)
      if (declaringClass != null)
	return declaringClass.getName() +
	  "." + function + arguments;
      else
	return
	  arguments.getExp(0) + 
	  "." + function + 
	  arguments.toStringInfix();
    else
      return function.toString() + arguments;
  }

  Expression function;
  protected Arguments arguments;
  
  /** true iff the first argument was written before the application: e.f(x) */
  boolean infix;
  /** Class this static method is defined in, or null */
  gnu.bytecode.ClassType declaringClass = null;
}
