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
//$Modified: Tue Jul 13 14:21:41 1999 by bonniot $
// Description : A method call

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class CallExp extends Expression
{
  public CallExp(Expression method, 
		 Collection parameters)
  {
    this.method=method;
    this.parameters=parameters;
  }

  Expression resolve(VarScope scope, TypeScope ts)
  {
    method=method.resolve(scope,ts);
    parameters=resolve(scope,ts,parameters);
    return this;
  }

  Polytype getType()
  {
    Polytype res=method.getType().codomain();
    User.error(res==null,method+" is not a function");
    return res;
  }

  public String toString()
  {
    return method
      + "(" + Util.map("",", ","",parameters) + ")"
      ;
  }

  protected Expression method;
  protected Collection parameters;
}
