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
import nice.tools.code.Types;

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
    // Check that resolving has not already been done.
    if (syntacticType != null)
      {
        type = syntacticType.resolveToLowlevel();
        syntacticType = null;
      }
  }
  
  /****************************************************************
   * Cloning types
   ****************************************************************/

  // explained in OverloadedSymbolExp

  private Polytype clonedType;
  
  final void makeClonedType(Polytype[] argTypes, int[] used)
  {
    if (clonedType != null)
      Internal.error(this, "clonedType in use");
    
    clonedType = type.cloneType();

    /* Where a default value was used, use the declared argument type instead
       of the value's type. This is more robust, as the application type will 
       not depend on the default value.
       Furthermore, this avoids running into problems when the default value
       refers to type parameters (in anonymous functions, by refering to
       previous arguments, ...) which would not be in sync with the cloned
       ones.
       This is only needed when the type is polymorphic.
    */

    if (clonedType == type || argTypes == null || used == null)
      return;

    mlsub.typing.Monotype fun = Types.rawType(clonedType.getMonotype());
    mlsub.typing.Monotype[] domain = ((mlsub.typing.FunType) fun).domain();

    for (int i = 0; i < argTypes.length; i++)
      if (used[i] == 0)
        argTypes[i] = new Polytype(domain[i]);
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
