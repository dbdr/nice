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
//$Modified: Mon Nov 08 20:13:17 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
 * Access to the field of an expression.
 * Replaced by the call of a field access method.
 */
abstract public class FieldExp extends Expression
{
  private FieldExp(Expression prefix, LocatedString field)
  {
    this.prefix=expChild(prefix);
    this.field=field;
  }
  
  public static Expression create(Expression prefix, LocatedString field)
  {
    List params=new ArrayList(1);
    params.add(prefix);
    return new CallExp(new IdentExp(field),params);
  }
  
  Expression resolveExp()
  {
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

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    Internal.error("compile in FieldExp");
    throw new Error();
  }
  
  public String toString()
  {
    return prefix+"."+field;
  }

  Expression prefix;
  LocatedString field; // before scoping resolution
  VarSymbol access; // after
}
