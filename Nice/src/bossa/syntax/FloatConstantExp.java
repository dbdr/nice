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

// File    : FloatConstantExp.java
// Created : Mon Jul 05 18:04:30 1999 by bonniot
//$Modified: Thu Jul 08 15:51:19 1999 by bonniot $
// Description : a Float constant

package bossa.syntax;

import bossa.util.*;

public class FloatConstantExp extends ConstantExp
{
  public FloatConstantExp(double value)
  {
    this.value=value;
  }

  Type getType()
  {
    return floatType;
  }

  public String toString()
  {
    return new Double(value).toString();
  }

  // TODO: change this
  final Type floatType = new ClassType(null);
  protected double value;
}
