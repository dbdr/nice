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
//$Modified: Wed Jun 14 15:07:19 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Identifier supposed to be a variable (not a type)
 */
public class IdentExp extends Expression
{
  public IdentExp(LocatedString i)
  {
    this.ident = i;
    setLocation(i.location());
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  Expression resolveExp()
  {
    if(scope.overloaded(ident))
       {
	 List c = scope.lookup(ident);
	 return new OverloadedSymbolExp(c,ident,scope);
       }
    VarSymbol s = scope.lookupOne(ident);
    if (s==null)
      if(ignoreInexistant)
	return this;
      else if(enableClassExp)
	return ClassExp.create(ident.toString());
      else
	User.error(ident, "Variable "+ident+" is not declared");
    
    return new SymbolExp(s);
  }

  /****************************************************************
   * Unimplemented methods
   ****************************************************************/

  void computeType()
  {
    Internal.error("computeType in IdentExp ("+this+")");
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    Internal.error("compile in IdentExp");
    throw new Error();
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public LocatedString getIdent()
  {
    return ident;
  }
  
  public String toString()
  {
    return ident.toString();
  }

  protected LocatedString ident;
  
  /** True if it should not be an error 
      if this ident does not exist. 
      Then it should resolve to itself.
      Set by CallExp.
  */
  boolean ignoreInexistant;

  /** Idem, except it should resolve to a ClassExp or a PackageExp. */
  boolean enableClassExp;
}
