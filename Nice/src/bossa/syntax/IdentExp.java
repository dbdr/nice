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
//$Modified: Tue Mar 14 18:28:55 2000 by Daniel Bonniot $

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
    this.ident=i;
    loc=i.location();
  }
  
  void computeType()
  {
    Internal.error("computeType in IdentExp ("+this+")");
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  Expression resolveExp()
  {
    // Always returns a collection, because of java fields access

    //if(scope.overloaded(ident))
       {
	 Collection c=scope.lookup(ident);
	 return new OverloadedSymbolExp(c,ident,scope);
       }
       //VarSymbol s=scope.lookupOne(ident);
       //User.error(s==null,ident,"Variable \""+ident+"\" is not declared");
       //return new SymbolExp(s);
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

  public String toString()
  {
    return "\""
      +ident
      +"\"";
  }

  protected LocatedString ident;
}
