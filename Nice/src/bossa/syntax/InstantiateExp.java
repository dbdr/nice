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

// File    : InstantiateExp.java
// Created : Mon Jul 12 17:48:04 1999 by bonniot
//$Modified: Thu Sep 30 17:44:30 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
 * Instantiation of type parameters
 */
public class InstantiateExp extends Expression
{
  public InstantiateExp(Expression exp, TypeParameters tp)
  {
    this.exp=exp;
    this.typeParameters=tp;
    setLocation(exp.location().englobe(tp.content));
    addChild(exp);
  }

  public InstantiateExp(Expression exp, TypeParameters tp, Location loc)
  {
    this(exp,tp);
    setLocation(loc);
  }
  
  /**
   * Create an InstantiateExp object if necessary,
   * and returns the expression 'exp' otherwise.
   */
  static Expression create(Expression exp, TypeParameters tp)
  {
    if(tp==null || tp.size()==0)
      return exp;
    Expression res=new InstantiateExp(exp,tp);
    res.setLocation(exp.location());
    return res;
  }

  boolean isFieldAccess()
  {
    return exp.isFieldAccess();
  }
  
  void resolve()
  {
    exp=exp.resolveExp();
    typeParameters=typeParameters.resolve(this.typeScope);
  }

  Expression resolveOverloading(List parameters, TypeParameters tp)
  {
    Internal.error(tp!=null,this,
		   "Type parameters applied twice");
    exp=exp.resolveOverloading(parameters,typeParameters);
    // The instantiation is not done in resolveOverloading()
    return this;
  }
  
  Expression resolveOverloading(Type expectedType, TypeParameters tp)
  {
    Internal.error(tp!=null,this,
		   "Type parameters applied twice");
    exp=exp.resolveOverloading(expectedType,typeParameters);
    // The instantiation is not done in resolveOverloading()
    return this;
  }
  
  void computeType()
  {
    type=null;
    try{
      type=exp.getType().instantiate(typeParameters);
      }
    catch(BadSizeEx e){
      User.error(this,exp+" has "+e.expected+" type parameters");      
    }
    
    User.error(type==null,this,
	       "You cannot try to instantiate "+this.exp+", it has not a parametric type");
  }

  public String toString()
  {
    return ""+ exp + typeParameters;
  }

  Expression exp;
  TypeParameters typeParameters;
}
