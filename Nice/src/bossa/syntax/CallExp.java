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
//$Modified: Fri Jul 09 21:04:27 1999 by bonniot $
// Description : A method call

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class CallExp extends Expression
{
  public CallExp(Expression method, Collection typeParameters,
		 Collection parameters)
  {
    this.method=method;
    if(typeParameters==null)
      this.typeParameters=new ArrayList(0);
    else
      this.typeParameters=typeParameters;
    this.parameters=parameters;
  }

  Expression resolve(VarScope scope, TypeScope ts)
  {
    method=method.resolve(scope,ts);
    typeParameters=Type.resolve(ts,typeParameters);
    parameters=resolve(scope,ts,parameters);
    return this;
  }

  Type getType()
  {
    Type res=method.getType().instantiate(typeParameters).codomain();
    User.error(res==null,method+" is not a function");
    return res;
  }

  public String toString()
  {
    return method
      + Util.map("<",", ",">",typeParameters)
      + "(" + Util.map("",", ","",parameters) + ")"
      ;
  }

  protected Expression method;
  protected Collection typeParameters,parameters;
}
