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

// File    : StringConstantExp.java
// Created : Thu Sep 02 14:49:48 1999 by bonniot
//$Modified: Tue Nov 09 17:46:38 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * A String constant
 */
public class StringConstantExp extends ConstantExp
{
  public StringConstantExp(String value)
  {
    className="java.lang.String";
    this.value=value;
  }

  public String toString()
  {
    return "\""+value+"\"";
  }
}
