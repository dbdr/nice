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
//$Modified: Thu Jul 29 11:11:48 1999 by bonniot $
// Description : instantiation of type parameters

package bossa.syntax;

import bossa.util.*;

public class InstantiateExp extends Expression
{
  public InstantiateExp(Expression exp, TypeParameters tp)
  {
    this.exp=exp;
    this.typeParameters=tp;
    loc=exp.location().englobe(tp.content);
  }

  Expression resolve(VarScope s, TypeScope ts)
  {
    exp=exp.resolve(s,ts);
    typeParameters=typeParameters.resolve(ts);
    return this;
  }

  Type getType()
  {
    Polytype res=null;
    try{
      res=exp.getType().instantiate(typeParameters);
      }
    catch(BadSizeEx e){
      User.error(this,exp+" has "+e.expected+" type parameters");      
    }
    
    User.error(res==null,this,"You cannot try to instantiate "+this.exp+
	       ", it has not a parametric type");
    return res;
  }

  public String toString()
  {
    return ""+ exp + typeParameters;
  }

  Expression exp;
  TypeParameters typeParameters;
}
