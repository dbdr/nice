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
//$Modified: Thu Sep 02 14:58:21 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * A String constant
 */
public class StringConstantExp extends ConstantExp
{
  public StringConstantExp(String value)
  {
    className="String";
    this.value=value;
  }

  public String toString()
  {
    return "\""+value+"\"";
  }

  protected String value;
}
