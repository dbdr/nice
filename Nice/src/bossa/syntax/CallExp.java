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
//$Modified: Thu Sep 30 18:02:39 1999 by bonniot $

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
    this.parameters=addChildren(parameters);
  }

  public static CallExp create(Expression fun, 
			Expression param1, Expression param2)
  {
    List params=new ArrayList(2);
    params.add(param1);
    params.add(param2);
    return new CallExp(fun,params);
  }
  
  void resolve()
  {
    removeChild(fun);
    fun=fun.resolveExp();
    removeChildren(parameters);
    parameters=Expression.resolveExp(parameters);
  }

  static boolean wellTyped(Expression fun,
			   List /* of Type */ parameters)
  {
    try{
      Type t=getType(fun,parameters);
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
  
  static Type getTypeAndReportErrors(Location loc,
				     Expression fun,
				     List /* of Expression */ parameters)
  {
    try{
      return getType(fun,Expression.getType(parameters));
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
      String end="not within the domain of the function \""+fun+"\"";
      if(parameters.size()>=2)
	Internal.error(loc,"The parameters "+
		   Util.map("(",", ",")",parameters) +
		   " are "+end);
      else
	User.error(loc,"The parameter \""+
		   Util.map("",", ","",parameters) +
		   "\" is "+end);      
    }
    return null;
  }

  static class ReportErrorEx extends Exception {
    ReportErrorEx(String msg) { super(msg); }
  }
  
  private static Type getType(Expression fun,
			      List /* of Type */ parameters)
    throws TypingEx,BadSizeEx,ReportErrorEx
  {
    Type funt=fun.getType();

    Collection dom=funt.domain();
    Monotype codom=funt.codomain();

    if(dom==null || codom==null)
      throw new ReportErrorEx(fun+" is not a function");
    if(!(funt instanceof Polytype))
      throw new ReportErrorEx("You have to specify the type parameters for function "+fun);
       
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
    Constraint cst=funt.getConstraint().and(Type.getConstraint(parameters));
    cst.and(MonotypeLeqCst.constraint(Type.getMonotype(parameters),dom));
    return new Polytype(cst,codom);
  }
  
  void computeType()
  {
    fun=fun.resolveOverloading(Expression.getType(parameters),null);
    type=getTypeAndReportErrors(location(),fun,parameters);
  }

  boolean isAssignable()
  {
    //TODO: decide where to resolve overloading, and do it just once
    fun=fun.resolveOverloading(Expression.getType(parameters),null);
    return fun.isFieldAccess();
  }
  
  public String toString()
  {
    return fun
      + "(" + Util.map("",", ","",parameters) + ")"
      ;
  }

  protected Expression fun;
  protected List /* of Expression */ parameters;
}
