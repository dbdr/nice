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
//$Modified: Thu Aug 26 17:38:21 1999 by bonniot $

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
    this.parameters=parameters;
    addChildren(this.parameters);
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
    fun=fun.resolveExp();
    parameters=Expression.resolveExp(parameters);
  }

  static Type getType(Location loc,
		      Expression fun,
		      List /* of Expression */ parameters,
		      boolean report)
  {
    Type funt=fun.getType();
    Collection parametersTypes=null;//=Expression.getPolytype(parameters);

    Collection dom=funt.domain();
    Monotype codom=funt.codomain();
    
    if(report)
      {
	User.error(dom==null || codom==null,
		   loc,fun+" is not a function");
	
	User.error(!(funt instanceof Polytype),loc,
		   "You have to specify the type parameters for function "+
		   fun);
      }
    else if(dom==null || codom==null || !(funt instanceof Polytype))
      return null;

    Typing.enter(funt.getTypeParameters(),"call "+fun);

    try{
      Typing.implies();

      try{ funt.getConstraint().assert(); }
      catch(TypingEx e) { 
	if(report)
	  User.error(loc,"The conditions for using this function are not fullfiled"); 
	else return null;
      }
      
      parametersTypes=Expression.getPolytype(parameters);
      User.error(parametersTypes==null,loc,
		   "Arguments of functions must not be imperative-parametric");
      
      Typing.in(parametersTypes,
		Domain.fromMonotypes(funt.domain()));

      Typing.leave();
    }
    catch(BadSizeEx e){
      if(report)
	User.error(loc,e.expected+" parameters expected, "+
		   "not "+e.actual);
      else
	return null;
    }
    catch(TypingEx e){
      if(report){
	String end="not within the domain of the function \""+fun+"\"";
	if(parameters.size()>=2)
	  User.error(loc,"The parameters "+
		     Util.map("(",", ",")",parameters) +
		     " are "+end);
	else
	  User.error(loc,"The parameter \""+
		     Util.map("",", ","",parameters) +
		     "\" is "+end);
      }
      else
	return null;
    }
    //computes the resulting type
    Constraint cst=funt.getConstraint().and(Type.getConstraint(parametersTypes));
    cst.and(MonotypeLeqCst.constraint(Type.getMonotype(parametersTypes),dom));    
    return new Polytype(cst,codom);
  }
  
  void computeType()
  {
    fun=fun.resolveOverloading(parameters,null);
    type=getType(location(),fun,parameters,true);
  }

  boolean isAssignable()
  {
    fun=fun.resolveOverloading(parameters,null);
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
