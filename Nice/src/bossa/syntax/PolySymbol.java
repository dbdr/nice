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
//$Modified: Fri Jun 09 17:54:13 2000 by Daniel Bonniot $

package bossa.syntax;

import mlsub.typing.Polytype;

import bossa.util.*;

/** A variable symbol which has a polytype (eg a method symbol)
 *
 * @see MonoSymbol
 */
public class PolySymbol extends VarSymbol
{
  public PolySymbol(LocatedString name, bossa.syntax.Polytype type)
  {
    super(name);
    this.syntacticType = type;
    if(syntacticType!=null)
      addChild(syntacticType);
  }
  
  public PolySymbol(MonoSymbol s)
  {
    this(s.name, new bossa.syntax.Polytype(s.syntacticType));
  }
  
  public Polytype getType()
  {
    return type;
  }

  /****************************************************************
   * Resolution
   ****************************************************************/

  void resolve()
  {
    type = syntacticType.resolveToLowlevel();
    syntacticType = null;
  }
  
  /****************************************************************
   * Cloning types
   ****************************************************************/

  // explained in OverloadedSymbolExp

  private boolean clonedTypeInUse;
  private Polytype clonedType;
  
  final void makeClonedType()
  {
    if(clonedTypeInUse)
      Internal.error(this, "clonedType in use");
    clonedTypeInUse = true;
    
    if(clonedType==null)
      clonedType = type.cloneType();
  }
  
  void releaseClonedType()
  {
    clonedTypeInUse = false;
    clonedType = null;
  }
  
  final Polytype getClonedType()
  {
    return clonedType;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return (type==null ? String.valueOf(syntacticType) : type.toString()) 
      + " " + name;
  }

  protected Polytype type;
  private bossa.syntax.Polytype syntacticType;
}
