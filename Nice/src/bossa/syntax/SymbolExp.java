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

// File    : SymbolExpr.java
// Created : Thu Jul 08 12:20:59 1999 by bonniot
//$Modified: Thu Aug 19 13:21:42 1999 by bonniot $
// Description : Access to the value of a symbol

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class SymbolExp extends Expression
{
  SymbolExp(VarSymbol s)
  {
    this.symbol=s;
  }

  boolean isAssignable()
  {
    return symbol.isAssignable();
  }

  Type getType()
  {
    return symbol.getType();
  }

  public String toString()
  {
    return 
      symbol.name.toString()
      ;
  }

  VarSymbol symbol;
}
