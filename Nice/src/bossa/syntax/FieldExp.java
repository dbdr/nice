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
//$Modified: Tue Aug 24 17:01:30 1999 by bonniot $
// Description : 

package bossa.syntax;

import bossa.util.*;

/**
 * Access to the field of an expression.
 */
public class FieldExp extends Expression
{
  public FieldExp(Expression prefix, LocatedString field)
  {
    this.prefix=expChild(prefix);
    this.field=field;
  }
  
  Expression resolveExp()
  {
    //TODO
//      prefix=prefix.resolveExp();
//      access=prefix.memberScope().lookupOne(field);
//      // If we reach this point, prefix should be a class
//      User.error(access==null,"Field \""+field+"\" not found in class "+prefix.getType().toString/*Base*/());
    return this;
  }

  boolean isAssignable()
  {
    return access.isAssignable();
  }

  void computeType()
  {
    Internal.error(access==null,"getType while access is still null");
    type=access.getType();
  }

  public String toString()
  {
    return prefix+"."+field;
  }

  Expression prefix;
  LocatedString field; // before scoping resolution
  VarSymbol access; // after
}
