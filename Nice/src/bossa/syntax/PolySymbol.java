/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import mlsub.typing.Polytype;

import bossa.util.*;

/**
   A variable symbol which has a polytype (eg a method symbol)
   
   @see MonoSymbol

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
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

  boolean isAssignable()
  {
    // No polymorphic references! 
    return false;
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

  private Polytype clonedType;
  
  final void makeClonedType(Polytype[] argTypes)
  {
    if(clonedType != null)
      Internal.error(this, "clonedType in use");
    
    java.util.Map map = type.getCloningMap();
    if (map == null)
      {
	clonedType = type;
	return;
      }

    clonedType = type.cloneType(map);
    if (argTypes != null)
      for (int i = 0; i < argTypes.length; i++)
	argTypes[i] = argTypes[i].applyMap(map);
  }
  
  void releaseClonedType()
  {
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
  bossa.syntax.Polytype syntacticType;
}
