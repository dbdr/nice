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

// File    : IntConstantExp.java
// Created : Mon Jul 05 17:30:56 1999 by bonniot
//$Modified: Thu Jul 08 15:51:33 1999 by bonniot $
// Description : An integer constant

package bossa.syntax;

import bossa.util.*;

public class IntConstantExp extends ConstantExp
{
  public IntConstantExp(int value)
  {
    this.value=value;
  }

  Type getType()
  {
    return intType;
  }

  public String toString()
  {
    return new Integer(value).toString();
  }

  //TODO: change this
  final Type intType = new ClassType(null);
  protected int value;
}
