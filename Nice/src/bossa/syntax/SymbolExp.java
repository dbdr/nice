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
//$Modified: Mon Jan 24 16:23:31 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Access to a symbol
 */
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

  FieldAccessMethod getFieldAccessMethod()
  {
    if(symbol instanceof FieldAccessMethod)
      return (FieldAccessMethod)symbol;
    else
      return null;
  }

  void computeType()
  {
    // Very important: each SymbolExp gets a copy of the type of the symbol.
    // Thus it has fresh binders.
    // Otherwise there would be an "aliasing" effect.
    // An important supposition is that two uses of a symbol
    // are two different SymbolExp objects (with a reference to the same symbol).
    // So they hold different (but equivalent) types.
    type=symbol.getType().cloneType();
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    if(symbol instanceof MethodDefinition)
      return new gnu.expr.QuoteExp(((MethodDefinition)symbol).getDispatchMethod());
    
    gnu.expr.Declaration decl = symbol.getDeclaration();
    
    if(decl==null)
      Internal.error(this+" has no bytecode declaration");
    
    return new gnu.expr.ReferenceExp(symbol.name.toString(),decl);
  }
  
  gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    gnu.expr.Declaration decl = symbol.getDeclaration();
    
    if(decl==null)
      Internal.error(this+" has no bytecode declaration");
    
    return new gnu.expr.SetExp(decl,value);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return symbol.name.toString();
  }

  private VarSymbol symbol;
}
