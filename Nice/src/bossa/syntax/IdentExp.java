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

// File    : IdentExp.java
// Created : Mon Jul 05 16:25:58 1999 by bonniot
//$Modified: Fri Jul 09 12:11:19 1999 by bonniot $
// Description : Just an identifier

package bossa.syntax;

import bossa.util.*;

public class IdentExp extends Expression
{
  public IdentExp(Ident i)
  {
    this.ident=i;
  }

  Type getType()
  {
    Internal.error("getType in IdentExp");
    return null;
  }

  Expression resolve(VarScope scope, TypeScope ts)
  {
    VarSymbol s=scope.lookup(ident);
    User.error(s==null,ident+" is not defined");
    return new SymbolExp(s);
  }

  public String toString()
  {
    return ident.toString();
  }

  protected Ident ident;
}
