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

// File    : NewExp.java
// Created : Thu Jul 08 17:15:15 1999 by bonniot
//$Modified: Fri Jul 23 17:42:32 1999 by bonniot $
// Description : Allocation of a new object

package bossa.syntax;

import bossa.util.*;

public class NewExp extends Expression
{
  public NewExp(TypeConstructor typeConstructor, TypeParameters tp)
  {
    this.tc=typeConstructor;
    if(tp==null)
      this.tp=new TypeParameters(null);
    else
      this.tp=tp;
  }
  
  Expression resolve(VarScope s, TypeScope ts)
  {
    tc=tc.resolve(ts);
    tp=tp.resolve(ts);
    return this;
  }
  
  Type getType()
  {
//      Type t=tc.getType();
//      User.error(tc==null,this,"Can't use an interface in a new expression");
    Type res=null;
    try{
      res=tc.instantiate(tp);
    }
    catch(BadSizeEx e){
      User.error(this,e.expected+" type parameters expected "+
		 ", not "+e.actual);
    }

    return res;
  }

  public String toString()
  {
    return "new "+tc+tp;
  }

  TypeConstructor tc;
  TypeParameters tp;
}
