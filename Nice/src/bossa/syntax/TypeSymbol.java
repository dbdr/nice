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
//$Modified: Thu Jul 22 10:26:43 1999 by bonniot $
// Description : Used to lookup Type names
//   Separed from VarSymbol since it can only appear in types 
//   (and New expression)
//   whereas VarSymbol can only appear in Expressions

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class TypeSymbol extends Node
  implements Located
{
  public TypeSymbol(LocatedString name)
  {
    this.name=name;
  }

  /**
   * Construct the list of TypeIdents
   * contained in a collection of TypeSymbols
   *
   * @param typeSymbols the collection
   * @return the collection of TypeIdents
   */
  static Collection toLocatedString(Collection typeSymbols)
  {
    Collection res=new ArrayList(typeSymbols.size());
    Iterator i=typeSymbols.iterator();
    while(i.hasNext())
      res.add(((TypeSymbol)i.next()).name);
    return res;
  }

  public boolean hasName(LocatedString name)
  {
    return this.name.equals(name);
  }

  void resolveScope()
  {
    //Nothing to do, whe are already a symbol
  }

  VarScope memberScope()
  {
    Internal.error("TypeSymbol.memberScope should not be called");
    return null;
  }
    
  /****************************************************************
   * Type checking
   ****************************************************************/

  void typecheck()
  {
    
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return name.location();
  }  
  
  public String toString()
  {
    return name.toString();
  }

  LocatedString name;
}
