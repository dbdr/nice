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
//$Modified: Thu Jan 20 14:32:12 2000 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.typing.*;

/**
 * A function call
 */
public class CallExp extends Expression
{
  /**
   *
   * @param fun the function to call
   * @param parameters a collection of Expressions
   */
  public CallExp(Expression fun, 
		 List parameters)
  {
    this.fun=expChild(fun);
    this.parameters=expChildren(parameters);
  }

  public static CallExp create(Expression fun, 
			Expression param1, Expression param2)
  {
    List params=new ArrayList(2);
    params.add(param1);
    params.add(param2);
    return new CallExp(fun,params);
  }
  
  static boolean wellTyped(Expression fun,
			   List /* of Polytype */ parameters)
  {
    try{
      Polytype t=getType(fun,parameters);
      if(t==null) return false;
    }
    catch(ReportErrorEx e){
      return false;
    }
    catch(TypingEx e){
      return false;
    }
    catch(BadSizeEx e){
      return false;
    }
    return true;
  }
  
  static Polytype getTypeAndReportErrors(Location loc,
				     Expression fun,
				     List /* of Expression */ parameters)
  {
    List paramTypes = Expression.getType(parameters);
    
    try{
      return getType(fun,paramTypes);
    }
    catch(BadSizeEx e){
      User.error(loc,e.expected+" parameters expected, "+
		 "not "+e.actual);
    }
    catch(ReportErrorEx e){
      User.error(loc,e.getMessage());
    }
    catch(TypingEx e){
      if(Typing.dbg) Debug.println(e.getMessage());

      if(fun.isFieldAccess())
	{
	  // There must be just one parameter in a field access
	  User.error(loc,parameters.get(0)+
		     " has no field "+fun);
	}
      else
	{  
	  String end="not within the domain of function \""+fun+"\"";
	  if(parameters.size()>=2)
	    User.error(loc,"Parameters \n"+
		       Util.map("(",", ",")",parameters) +
		       "\n of types \n" +
		       Util.map("(",",\n  ",")",paramTypes) +
		       "\n are "+end);
	  else
	    User.error(loc,"The parameter \""+
		       Util.map("",", ","",parameters) +
		       "\" is "+end);      
	}
    }
    return null;
  }

  static class ReportErrorEx extends Exception {
    ReportErrorEx(String msg) { super(msg); }
  }
  
  private static Polytype getType(Expression fun,
				  List /* of Polytype */ parameters)
    throws TypingEx,BadSizeEx,ReportErrorEx
  {
    Polytype funt=fun.getType();

    Collection dom=funt.domain();
    Monotype codom=funt.codomain();

    if(dom==null || codom==null)
      throw new ReportErrorEx(fun+" is not a function");

    Typing.enter(funt.getTypeParameters(),"call "+fun);
    
    Typing.implies();

    try{ funt.getConstraint().assert(); }
    catch(TypingEx e) { 
      throw new ReportErrorEx("The conditions for using this function are not fullfiled");
    }    
    
    if(Typing.dbg)
      {
	Debug.println("Parameters:\n"+
		      Util.map("",", ","\n",parameters));
	Debug.println("Types of parameters:\n"+
		      Util.map("",", ","\n",parameters));
      }
    
    Typing.in(parameters,
	      Domain.fromMonotypes(funt.domain()));

    Typing.leave();

    //computes the resulting type
    Constraint cst=funt.getConstraint().and(Polytype.getConstraint(parameters));
    cst.and(MonotypeLeqCst.constraint(Polytype.getMonotype(parameters),dom));
    return new Polytype(cst,codom);
  }

  private void resolveOverloading()
  //TODO: decide where to resolve overloading, and do it just once
  {
    parameters=Expression.noOverloading(parameters);
    fun.resolveOverloading(Expression.getType(parameters));
  }
  
  void computeType()
  {
    resolveOverloading();
    type=getTypeAndReportErrors(location(),fun,parameters);
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
    resolveOverloading();
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    //resolveOverloading();
    return new gnu.expr.ApplyExp(fun.generateCode(),
				 Expression.compile(parameters));
  }
  
  gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    if(!fun.isFieldAccess())
      Internal.error(this,"Assignment to a call that is not a field access.");
    
    return fun.getFieldAccessMethod().compileAssign(parameters,value);
  }

  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    return fun
      + "(" + Util.map("",", ","",parameters) + ")"
      ;
  }

  protected ExpressionRef fun;
  protected List /* of ExpressionRef */ parameters;
}
