/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : CallExp.java
// Created : Mon Jul 05 16:27:27 1999 by bonniot
//$Modified: Thu Aug 31 18:29:22 2000 by Daniel Bonniot $

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
 * A function call.
 */
public class CallExp extends Expression
{
  /**
   * @param fun the function to call
   * @param parameters a collection of Expressions
   */
  public CallExp(Expression fun, 
		 List parameters)
  {
    this.fun = expChild(fun);
    this.parameters = expChildren(parameters);
  }

  /**
     @param fun the function to call
     @param parameters a collection of Expressions
     @param infix true if the first parameter was written before the function,
     using the dot notation: x1.f(x2, x3)
   */
  public CallExp(Expression fun, List parameters, boolean infix)
  {
    this(fun, parameters);
    this.infix = infix;
  }

  public static CallExp create(Expression fun, 
			       Expression param1)
  {
    List params = new LinkedList();
    params.add(param1);
    CallExp res = new CallExp(fun, params);
    res.setLocation(fun.location());
    return res;
  }
  
  public static CallExp create(Expression fun, 
			       Expression param1, Expression param2)
  {
    List params = new ArrayList(2);
    params.add(param1);
    params.add(param2);
    CallExp res = new CallExp(fun, params);
    res.setLocation(fun.location());
    return res;
  }
  
  void resolve()
  {
    // in infix applications, the symbol is either a
    // class method or a field name, so it must be found in
    // global scope
    if(infix)
      {
	Expression e = fun.content();
	e.scope = Node.getGlobalScope();
	if(e instanceof IdentExp)
	  ((IdentExp) e).ignoreInexistant = true;
	
	e = ((ExpressionRef) parameters.get(0)).content();
	if(e instanceof IdentExp)
	  ((IdentExp) e).enableClassExp = true;
      }
  }
  
  /****************************************************************
   * Type checking
   ****************************************************************/

  static Polytype wellTyped(Polytype funt,
			    Polytype[] parameters)
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
					 Expression fun,
					 List /* of Expression */ parameters)
  {
    Polytype[] paramTypes = Expression.getType(parameters);
    
    try{
      return getType(fun.getType(), paramTypes);
    }
    catch(BadSizeEx e){
      User.error(loc, fun.toString(Printable.detailed) + 
		 " expects " + e.expected + " parameters, " +
		 "not " + e.actual);
    }
    catch(ReportErrorEx e){
      User.error(loc, e.getMessage());
    }
    catch(TypingEx e){
      if(Typing.dbg) 
	Debug.println(e.getMessage());

      if(fun.isFieldAccess())
	{
	  // There must be just one parameter in a field access
	  User.error(loc, parameters.get(0)+
		     " has no field "+fun);
	}
      else
	{
	  String end = "not within the domain of function \""+fun+"\"";
	  if(parameters.size()>=2)
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
    parameters = Expression.noOverloading(parameters);

    // case where the parameters is a package, or a package prefix
    if(parameters.size() == 1)
      {
	Expression param0 = ((ExpressionRef) parameters.get(0)).content();
	if(param0 instanceof PackageExp &&
	   (fun.content() instanceof IdentExp || 
	    fun.content() instanceof SymbolExp))
	  return ClassExp.create((PackageExp) param0, fun.toString());
      }
    
    return this;
  }
  
  private boolean overloadingResolved;
  
  private void resolveOverloading()
  {
    // do not resolve twice
    if(overloadingResolved)
      return;
    overloadingResolved = true;
    
    parameters = Expression.noOverloading(parameters);
    
    resolveStaticClassPrefix();
    
    fun.resolveOverloading(Expression.getType(parameters), this);
  }

  /** Handle static functions, prefixed by the class name */
  private void resolveStaticClassPrefix()
  {
    if(parameters.size()>=1)
      {
	declaringClass = 
	  ((Expression) parameters.get(0)).staticClass();
	if(declaringClass != null)
	  // A static function is called
	  {
	    Expression funExp = fun.content();
	    
	    LocatedString funName;
	    
	    if(funExp instanceof OverloadedSymbolExp)
	      funName = ((OverloadedSymbolExp) funExp).ident;
	    else if(funExp instanceof IdentExp)
	      funName = ((IdentExp) funExp).getIdent();
	    else if(funExp instanceof SymbolExp)
	      funName = ((SymbolExp) funExp).getName();
	    else
	      {
		Debug.println(this+"");
		
		Internal.error("Unknown case " +
			       funExp.getClass() + " in CallExp");
		return;
	      }

	    parameters.remove(0);
	    List possibilities = new LinkedList();
	    declaringClass.addMethods();
	    
	    // search methods
	    for(gnu.bytecode.Method method = declaringClass.getMethods();
		method!=null; method = method.getNext())
	      if(method.getName().equals(funName.content) &&
		 method.arg_types.length==parameters.size())
		{
		  MethodDefinition md = 
		    JavaMethodDefinition.addFetchedMethod(method);
		  if(md!=null)
		    possibilities.add(md.symbol);
		}

	    // search a field
	    if(parameters.size()==0)
	      {
		gnu.bytecode.Field field = 
		  declaringClass.getField(funName.toString());
		if(field!=null)
		  possibilities.add(JavaMethodDefinition.addFetchedMethod(field).symbol);
	      }
	    
	    if (possibilities.size() == 0)
	      User.error(this, "Class " + declaringClass.getName() +
			 " has no method " + funName);
	    
	    fun = new ExpressionRef
	      (new OverloadedSymbolExp(possibilities, funName,
				       fun.content().getScope()));
	  }
      }
  }
  
  void computeType()
  {
    resolveOverloading();
    if(type == null)
      type = getTypeAndReportErrors(location(), fun, parameters);

    type.simplify();
  }

  boolean isAssignable()
  {
    resolveOverloading();
    return fun.isFieldAccess();
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
    gnu.expr.Expression[] params = Expression.compile(parameters);
    for(int i=0; i<params.length; i++)
      if(params[i] instanceof gnu.expr.QuoteExp)
	{
	  gnu.expr.QuoteExp q = (gnu.expr.QuoteExp) params[i];
	  if(q.getValue() instanceof gnu.expr.PrimProcedure)
	    params[i] = ((gnu.expr.PrimProcedure) q.getValue()).wrapInLambda();
	}
    
    gnu.expr.Expression res;
    if (fun.isFieldAccess())
      {
	if (parameters.size() != 1)
	  Internal.error(this, "A field access should have 1 parameter");

	res = fun.getFieldAccessMethod().compileAccess
	  ((Expression) parameters.get(0));
      }
    else
      res = new gnu.expr.ApplyExp(fun.generateCode(), params);

    return Inline.inline(new EnsureTypeProc(Types.javaType(getType())), res);
  }
  
  gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    if (!fun.isFieldAccess())
      Internal.error(this, "Assignment to a call that is not a field access");
    if (parameters.size() != 1)
      Internal.error(this, "A field access should have 1 parameter");

    return fun.getFieldAccessMethod().compileAssign
      ((Expression) parameters.get(0), value);
  }

  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    if(infix)
      if (declaringClass != null)
	return declaringClass.getName() +
	  "." + fun + 
	  "(" + 
	  Util.map("",", ","",parameters) +
	  ")";
      else
	return
	  parameters.get(0) + 
	  "." + fun + 
	  "(" + 
	  Util.map("",", ","",parameters.subList(1, parameters.size())) +
	  ")";
    else
      return fun + "(" + Util.map("",", ","",parameters) + ")";
  }

  protected ExpressionRef fun;
  protected List /* of ExpressionRef */ parameters;
  
  /** true iff the first argument was written before the application: e.f(x) */
  boolean infix;
  /** Class this static method is defined in, or null */
  gnu.bytecode.ClassType declaringClass = null;
}
