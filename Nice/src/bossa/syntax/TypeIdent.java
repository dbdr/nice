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

// File    : TypeIdent.java
// Created : Sat Jul 24 14:02:08 1999 by bonniot
//$Modified: Sat Dec 04 12:44:56 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * A syntactic type identifier.
 * 
 * After scoping, it will either reveal to be a 
 * TypeConstructor or a MonotypeVar.
 *
 * @author bonniot
 */

public class TypeIdent
  implements TypeSymbol
{
  public TypeIdent(LocatedString name)
  {
    this.name=name;
  }

  public TypeSymbol cloneTypeSymbol()
  {
    return new TypeIdent(name);
  }
  
  /****************************************************************
   * 
   ****************************************************************/

  TypeSymbol resolve(TypeScope scope)
  {
    TypeSymbol res = scope.lookup(name);
    if(res==null)
      User.error(this,name+" is not defined"," in "+scope);

    return res;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "\""+name+"\"";
  }

  public LocatedString getName()
  {
    return name;
  }
  
  public boolean hasName(LocatedString name)
  {
    return this.name.equals(name);
  }
  
  public Location location()
  {
    return name.location();
  }

  LocatedString name;
}
