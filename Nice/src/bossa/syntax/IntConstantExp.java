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
//$Modified: Thu Sep 02 14:58:42 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * An integer constant
 */
public class IntConstantExp extends ConstantExp
{
  public IntConstantExp(int value)
  {
    className="Integer";
    this.value=value;
  }

  public String toString()
  {
    return new Integer(value).toString();
  }

  protected int value;
}
