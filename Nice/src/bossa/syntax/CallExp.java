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
    this.function = function;
    this.arguments = arguments;
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

    return Polytype.apply(funt, parameters);
  }

  /****************************************************************
   * Overloading resolution
   ****************************************************************/

  private boolean overloadingResolved;
  
  private void resolveOverloading()
  {
    // do not resolve twice
    if(overloadingResolved)
      return;
    overloadingResolved = true;
    
    arguments.noOverloading();
    
    function = function.resolveOverloading(this);
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
  }

  boolean isAssignable()
  {
    resolveOverloading();
    return function.isFieldAccess();
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.expr.Expression compile()
  {
    gnu.expr.Expression[] params = Expression.compile(computedExpressions);

    // wraps the arguments that reference methods into LambdaExps
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

    gnu.bytecode.Type expectedType = Types.javaType(getType());
    if (res.getType().isSubtype(expectedType))
      return res;
    else
      // Is there any case where this is necessary?
      // If yes, add a regtest case
      // If not, remove this case and the EnsureTypeProc class
      return Inline.inline(new EnsureTypeProc(expectedType), res);
  }
  
  gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    if (!function.isFieldAccess())
      Internal.error(this, "Assignment to a call that is not a field access");

    FieldAccess access = function.getFieldAccessMethod();
    switch (arguments.size())
    {
    case 0:
      return access.compileAssign(value);
    case 1:
      return access.compileAssign(arguments.getExp(0), value);
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
