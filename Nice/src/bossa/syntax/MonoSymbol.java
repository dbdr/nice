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

// File    : MonoSymbol.java
// Created : Fri Jul 16 17:10:53 1999 by bonniot
//$Modified: Tue Aug 24 15:11:27 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/** A variable symbol which has a monotype (eg a function parameter)
 *
 * @see PolySymbol
 */
public class MonoSymbol extends VarSymbol
{
  public MonoSymbol(LocatedString name, Monotype type)
  {
    this(name,type,null);
  }
  
  public MonoSymbol(LocatedString name, Monotype type, ClassDefinition memberOf)
  {
    super(name);
    this.type=type;
    this.memberOf=memberOf;
  }

  public Type getType()
  {
    if(memberOf!=null)
      return new Polytype(memberOf.getConstraint(),type);
    else
      return new Polytype(Constraint.True(),type);
  }

  public Monotype getMonotype()
  {
    return type;
  }

  /**
   * Maps getMonotype over a collection of MonoSymbols
   *
   * @param varsymbols the collection of MonoSymbols
   * @return the collection of their Monotypes
   */
  static Collection getMonotype(Collection c)
  {
    Collection res=new ArrayList(c.size());
    for(Iterator i=c.iterator();i.hasNext();)
      res.add(((MonoSymbol)i.next()).getMonotype());
    return res;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  void resolve()
  {
    type=type.resolve(typeScope);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return type+" "+name;
  }

  Monotype type;
  //When this is a field. Maybe unusefull
  ClassDefinition memberOf;
}
