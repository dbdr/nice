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
//$Modified: Fri Jul 21 14:25:04 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import gnu.expr.*;

/**
 * Access to a symbol (variable, function parameter).
 */
public class SymbolExp extends Expression
{
  SymbolExp(VarSymbol s)
  {
    this.symbol = s;
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
    if(symbol instanceof MethodDefinition.Symbol)
      {
	MethodDefinition.Symbol s = (MethodDefinition.Symbol) symbol;
	if(s.definition instanceof FieldAccessMethod)
	  return (FieldAccessMethod)s.definition;
      }
    return null;
  }

  void computeType()
  {
    //Internal.printStackTrace();
    
    // Very important: each SymbolExp gets a copy of the type of the symbol.
    // Thus it has fresh binders.
    // Otherwise there would be an "aliasing" effect.
    // An important supposition is that two uses of a symbol
    // are two different SymbolExp objects 
    // (with a reference to the same symbol).
    // So they hold different (but equivalent) types.

    // it's not necessary if the symbol is monomorphic
    if (symbol instanceof PolySymbol)
      type = symbol.getType().cloneType();
    else
      type = symbol.getType();
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    if(symbol instanceof MethodDefinition.Symbol)
      {
	gnu.mapping.Procedure proc = 
	  ((MethodDefinition.Symbol)symbol).definition.getDispatchMethod();

	return new QuoteExp(proc);
      }
    
    gnu.expr.Declaration decl = symbol.getDeclaration();
    
    if(decl==null)
      Internal.error(this+" has no bytecode declaration");
    
    return new gnu.expr.ReferenceExp(symbol.name.toString(),decl);
  }
  
  /** @return the declaration of the variable denoted by this expression,
      or <code>null</code> if this expression is not a variable.
  */
  gnu.expr.Declaration getDeclaration()
  {
    gnu.expr.Declaration decl = symbol.getDeclaration();
    
    if(decl==null)
      Internal.error(this+" has no bytecode declaration");
    
    return decl;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public LocatedString getName()
  {
    return symbol.name;
  }
  
  public String toString()
  {
    return symbol.name.toString();
  }

  private VarSymbol symbol;
}
