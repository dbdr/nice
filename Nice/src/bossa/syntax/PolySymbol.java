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
//$Modified: Tue Jul 27 10:14:36 1999 by bonniot $
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
    this.type=type;
  }

  public Type getType()
  {
    return type;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  void buildScope(VarScope scope, TypeScope ts)
  {
    super.buildScope(scope,ts);
    type.buildScope(this.typeScope);
  }

  void resolveScope()
  {
    type.resolve();
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  void typecheck()
  {
    //Nothing
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
