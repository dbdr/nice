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
//$Modified: Fri Jul 16 19:18:31 1999 by bonniot $
// Description : A variable (local, field, parameter)

package bossa.syntax;

import java.util.*;
import bossa.util.*;

abstract class VarSymbol extends Node
{
  public VarSymbol(Ident name)
  {
    this.name=name;
  }

  public boolean hasName(Ident i)
  {
    return this.name.equals(i);
  }

  boolean isAssignable()
  {
    return true;
  }

  abstract Polytype getType();

  static void resolveScope(Collection c)
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      ((VarSymbol)i.next()).resolveScope();
  }

  Ident name;
}
