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

// File    : PolySymbol.java
// Created : Fri Jul 16 17:03:51 1999 by bonniot
//$Modified: Mon Oct 25 19:38:29 1999 by bonniot $
// Description : A local variable

package bossa.syntax;

import bossa.util.*;

/** A variable symbol which has a polytype (eg a local variable)
 *
 * @see MonoSymbol
 */
public class PolySymbol extends VarSymbol
{
  public PolySymbol(LocatedString name, Polytype type)
  {
    super(name);
    if(type!=null)
      addChild(type);
    this.type=type;
  }

  public PolySymbol(MonoSymbol s)
  {
    this(s.name,new Polytype(s.type));
  }
  
  public Polytype getType()
  {
    return type;
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return type+" "+name;
  }

  Polytype type;
}
