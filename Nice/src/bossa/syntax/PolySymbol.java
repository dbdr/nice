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
//$Modified: Tue Aug 24 16:40:59 1999 by bonniot $
// Description : A local variable

package bossa.syntax;

import bossa.util.*;

/** A variable symbol which has a polytype (eg a local variable)
 *
 * @see MonoSymbol
 */
public class PolySymbol extends VarSymbol
{
  public PolySymbol(LocatedString name, Type type)
  {
    super(name);
    if(type!=null)
      addChild(type);
    this.type=type;
  }

  public Type getType()
  {
    return type.removeUnusefullTypeParameters();
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return type+" "+name;
  }

  Type type;
}
