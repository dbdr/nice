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

// File    : FieldExp.java
// Created : Mon Jul 05 17:29:46 1999 by bonniot
//$Modified: Fri Jul 23 12:52:59 1999 by bonniot $
// Description : Access to the field of an expression
//   the field may be the name of a method
//   since it can be considered as a field holding a closure

package bossa.syntax;

import bossa.util.*;

public class FieldExp extends Expression
{
  public FieldExp(Expression prefix, LocatedString field)
  {
    this.prefix=prefix;
    this.field=field;
  }
  
  Expression resolve(VarScope scope, TypeScope ts)
  {
    prefix=prefix.resolve(scope,ts);
    access=prefix.memberScope().lookup(field);
    // If we reach this point, prefix should be a class
    User.error(access==null,"Field \""+field+"\" not found in class "+prefix.getType().toString/*Base*/());
    return this;
  }

  boolean isAssignable()
  {
    return access.isAssignable();
  }

  Type getType()
  {
    Internal.error(access==null,"getType while access is still null");
    return access.getType();
  }

  public String toString()
  {
    return prefix+"."+field;
  }

  Expression prefix;
  LocatedString field; // before scoping resolution
  VarSymbol access; // after
}
