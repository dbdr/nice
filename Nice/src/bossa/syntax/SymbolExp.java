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
//$Modified: Thu Aug 26 10:36:19 1999 by bonniot $
// Description : Access to the value of a symbol

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class SymbolExp extends Expression
{
  SymbolExp(VarSymbol s)
  {
    this.symbol=s;
    setLocation(s.name.location());
  }

  SymbolExp(VarSymbol s, Location loc)
  {
    this(s);
    setLocation(loc);
  }
  
  boolean isAssignable()
  {
    return symbol.isAssignable();
  }

  boolean isFieldAccess()
  {
    return symbol instanceof MethodDefinition
      && ((MethodDefinition)symbol).isFieldAccess;
  }
  
  void computeType()
  {
    // Very important: each SymbolExp gets a copy of the type of the symbol.
    // Thus it has fresh binders.
    // Otherwise there is an "aliasing" effect.
    // An important supposition is that two uses of a symbol
    // are two different SymbolExp objects (with a reference to the same symbol).
    // So they hold different (but equivalent) types.
    type=symbol.getType().cloneType();
  }

  public String toString()
  {
    return 
      symbol.name.toString()
      ;
  }

  VarSymbol symbol;
}
