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
//$Modified: Fri Jul 09 19:56:41 1999 by bonniot $
// Description : Used to lookup Type names
//   Separed from VarSymbol since it can only appear in types 
//   (and New expression)
//   whereas VarSymbol can only appear in Expressions

package bossa.syntax;

import bossa.util.*;

public class TypeSymbol extends Node
{
  public TypeSymbol(Ident name)
  {
    this.name=name;
  }

  public boolean hasName(IdentType i)
  {
    return this.name.equals(i.name.content);
  }

  void resolveScope()
  {
    //Nothing to do, whe are already a symbol
  }

  public String toString()
  {
    return name.toString();
  }

  Ident name;
}
