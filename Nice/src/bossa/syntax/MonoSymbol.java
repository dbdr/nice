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

import java.util.*;
import bossa.util.*;
import mlsub.typing.Polytype;

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
      return new Polytype(mlsub.typing.Constraint.True, type);
  }

  public mlsub.typing.Monotype getMonotype()
  {
    return type;
  }

  public bossa.syntax.Monotype getSyntacticMonotype()
  {
    return syntacticType;
  }

  /**
   * Maps getMonotype over a collection of MonoSymbols
   *
   * @param varsymbols the collection of MonoSymbols
   * @return the list of their Monotypes
   */
  static mlsub.typing.Monotype[] getMonotype(Collection c)
  {
    mlsub.typing.Monotype[] res =
      new mlsub.typing.Monotype[c.size()];

    int n = 0;
    for(Iterator i=c.iterator();i.hasNext();)
      res[n++] = ((MonoSymbol)i.next()).getMonotype();
    return res;
  }

  /**
   * Maps getSyntacticMonotype over a collection of MonoSymbols
   *
   * @param varsymbols the collection of MonoSymbols
   * @return the list of their Monotypes
   */
  static List getSyntacticMonotype(Collection c)
  {
    List res=new ArrayList(c.size());
    for(Iterator i=c.iterator();i.hasNext();)
      res.add(((MonoSymbol)i.next()).syntacticType);
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
