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
//$Modified: Wed Aug 18 18:59:08 1999 by bonniot $

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
  
  Type getType()
  {
    Internal.error("getType in IdentExp ("+this+")");
    return null;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  Expression resolveExp()
  {
    VarSymbol s=scope.lookupOne(ident);
    User.error(s==null,ident,"Variable \""+ident+"\" is not defined");
    return new SymbolExp(s);
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
