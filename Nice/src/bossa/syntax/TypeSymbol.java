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

// File    : TypeSymbol.java
// Created : Fri Jul 09 11:20:46 1999 by bonniot
//$Modified: Thu Jan 20 12:19:02 2000 by bonniot $
// Description : Used to lookup type names
//   Separed from VarSymbol since it can only appear in types 
//   (and New expression)
//   whereas VarSymbol can only appear in Expressions

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public interface TypeSymbol extends Located
{
  /****************************************************************
   * Scoping
   ****************************************************************/

  boolean hasName(LocatedString name);
  LocatedString getName();
  TypeSymbol cloneTypeSymbol();
  
  /**
   * Used for the search of java classes, as the type symbol
   * of type binders. See MethodBodyDefinition.
   */
  static TypeSymbol dummy = new TypeConstructor(new LocatedString("dummy type symbol", Location.nowhere()));
}
