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
//$Modified: Wed Oct 27 15:30:20 1999 by bonniot $
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
}
