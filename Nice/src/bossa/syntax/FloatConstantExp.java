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
//$Modified: Fri Nov 05 16:54:23 1999 by bonniot $
// Description : a Float constant

package bossa.syntax;

import bossa.util.*;

public class FloatConstantExp extends ConstantExp
{
  public FloatConstantExp(double value)
  {
    className="Float";
    this.value=new Double(value);
  }

  public String toString()
  {
    return ((Double)value).toString();
  }
}
