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

// File    : VarSymbol.java
// Created : Wed Jul 07 16:56:06 1999 by bonniot
//$Modified: Thu Aug 19 13:27:35 1999 by bonniot $
// Description : A variable (local, field, parameter)

package bossa.syntax;

import java.util.*;
import bossa.util.*;

abstract class VarSymbol extends Node
{
  public VarSymbol(LocatedString name)
  {
    super(Node.global);
    this.name=name;
    addSymbol(this);
  }

  public boolean hasName(LocatedString i)
  {
    return this.name.equals(i);
  }

  boolean isAssignable()
  {
    return true;
  }

  abstract Type getType();

  /**
   * Maps getType over a collection of VarSymbols
   *
   * @param varsymbols the colleciton of Varsymbols
   * @return the collection of their Types
   */
  static Collection getType(Collection varsymbols)
  {
    Iterator i=varsymbols.iterator();
    Collection res=new ArrayList(varsymbols.size());

    while(i.hasNext())
      res.add(((VarSymbol)i.next()).getType());

    return res;
  }

  LocatedString name;
}
