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
   A variable symbol which has a monotype (eg a function parameter)
 
   @see PolySymbol
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class MonoSymbol extends VarSymbol
{
  public MonoSymbol(LocatedString name, Monotype type)
  {
    this(name,type,null);
  }
  
  public MonoSymbol(LocatedString name, mlsub.typing.Monotype type)
  {
    super(name);
    this.type = type;
  }
  
  public MonoSymbol(LocatedString name, Monotype type, ClassDefinition memberOf)
  {
    super(name);
    this.syntacticType=type;
    this.memberOf = memberOf;
  }

  public Polytype getType()
  {
    if(memberOf!=null)
      return null; //new Polytype(memberOf.getConstraint(),type);
    else
      return new Polytype(type);
  }

  public mlsub.typing.Monotype getMonotype()
  {
    return type;
  }

  /**
     Maps getMonotype over an array of MonoSymbols.

     @param symbols the array of MonoSymbols
     @return the array of their Monotypes
   */
  static mlsub.typing.Monotype[] getMonotype(MonoSymbol[] symbols)
  {
    if (symbols == null) return null;

    mlsub.typing.Monotype[] res = new mlsub.typing.Monotype[symbols.length];
    for(int i = 0; i < symbols.length; i++)
      res[i] = symbols[i].getMonotype();
    return res;
  }

  /**
     Maps getMonotype over an array of MonoSymbols.

     @param symbols the array of MonoSymbols
     @return the array of their syntactic Monotypes
   */
  static Monotype[] getSyntacticMonotype(MonoSymbol[] symbols)
  {
    if (symbols == null)
      return Monotype.array0;

    Monotype[] res = new Monotype[symbols.length];
    for(int i = 0; i < symbols.length; i++)
      res[i] = symbols[i].syntacticType;
    return res;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  void resolve()
  {
    type = syntacticType.resolve(typeScope);
    syntacticType = null;
  }

  /****************************************************************
   * Overloading resolution
   ****************************************************************/

  /**
     @return
     0 : doesn't match
     1 : wasn't even a function
     2 : matches
  */
  int match(Arguments arguments)
  {
    mlsub.typing.lowlevel.Kind k = Types.rawType(type).getKind();
    if(k instanceof mlsub.typing.FunTypeKind)
      if (!arguments.plainApplication(((mlsub.typing.FunTypeKind) k).domainArity))
	return 0;
      else
	return 2;
    else 
      return 1;
  }

  /****************************************************************
   * Cloning types
   ****************************************************************/

  // explained in OverloadedSymbolExp

  final void makeClonedType() {}
  
  final void releaseClonedType() {}

  final Polytype getClonedType() 
  {
    return getType();
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return type+" "+name;
  }

  Monotype syntacticType;
  mlsub.typing.Monotype type;
  //When this is a field. Maybe unusefull
  ClassDefinition memberOf;
}
