/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import gnu.expr.*;

/**
   Access to a symbol (variable, function parameter).

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class SymbolExp extends Expression
{
  SymbolExp(VarSymbol symbol, Location loc)
  {
    this.symbol = symbol;
    setLocation(loc);
  }
  
  public boolean isAssignable()
  {
    return symbol.isAssignable();
  }

  FieldAccess getFieldAccessMethod()
  {
    return symbol.getFieldAccessMethod();
  }

  void checkSpecialRequirements(Expression[] arguments)
  {
    symbol.checkSpecialRequirements(arguments);
  }

  void computeType()
  {
    // Very important: each SymbolExp gets a copy of the type of the symbol.
    // Thus it has fresh binders.
    // Otherwise there would be an "aliasing" effect.
    // An important supposition is that two uses of a symbol
    // are two different SymbolExp objects 
    // (with a reference to the same symbol).
    // So they hold different (but equivalent) types.

    type = symbol.getType().cloneType();
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    try {
      return symbol.compile();
    }
    catch(FieldAccess.UsingAsValue e) {
      throw User.error(this, 
		       "You must supply the object that contains this field");
    }
  }
  
  gnu.expr.Expression generateCodeInCallPosition()
  {
    try {
      gnu.expr.Expression res = symbol.compileInCallPosition();
      location().write(res);
      return res;
    }
    catch(FieldAccess.UsingAsValue e) {
      throw User.error(this, 
		       "You must supply the object that contains this field");
    }
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
    return symbol.name.toQuotedString();
  }

  VarSymbol getSymbol()
  {
    return symbol;
  }

  private VarSymbol symbol;
}
