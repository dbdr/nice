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

// File    : ReturnStmt.java
// Created : Mon Jul 05 17:21:40 1999 by bonniot
//$Modified: Fri Jul 09 15:20:58 1999 by bonniot $
// Description : return in a function or method

package bossa.syntax;

import bossa.util.*;

public class ReturnStmt extends Statement
{
  public ReturnStmt(Expression value)
  {
    this.value=value;
  }

  void resolveScope()
  {
    value=value.resolve(scope,typeScope);
  }

  public String toString()
  {
    return "return "+value;
  }

  protected Expression value;
}
