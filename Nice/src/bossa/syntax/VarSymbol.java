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
//$Modified: Thu Mar 02 15:27:10 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A variable (local, field, parameter)
 */
abstract class VarSymbol extends Node implements Located
{
  public VarSymbol(LocatedString name)
  {
    super(Node.upper);
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

  abstract Polytype getType();

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

  public Location location()
  {
    return name.location();
  }
  
  LocatedString name;

  /****************************************************************
   * Code generation
   ****************************************************************/

  void setDeclaration(gnu.expr.Declaration declaration)
  {
    this.decl = declaration;
    this.decl.setLine(name.location().getLine());
    this.decl.setCanRead(true);
    this.decl.setCanWrite(true);
    if(declaration.getContext()==null)
      Internal.error(this+" has no englobing context");
  }
  
  gnu.expr.Declaration getDeclaration()
  {
    return decl;
  }
  
  private gnu.expr.Declaration decl = null;
}
