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

// File    : TypeScope.java
// Created : Fri Jul 09 11:29:17 1999 by bonniot
//$Modified: Sat Dec 04 11:14:25 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A Scope level for types.
 * Is extended in each node that defined a new scope level.
 */
class TypeScope
{
  public TypeScope(TypeScope outer)
  {
    this.outer=outer;
    this.map=new HashMap();
  }

  void addSymbol(TypeSymbol s)
  {
    map.put(s.getName(),s);
  }
  
  void addSymbols(Collection c)
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      addSymbol((TypeSymbol)i.next());
  }

  void addMapping(LocatedString name, TypeSymbol s)
  {
    map.put(name,s);
  }

  void addMappings(Collection names, Collection symbols)
  {
    for(Iterator in=names.iterator(),is=symbols.iterator();
	in.hasNext();)
      addMapping((LocatedString)in.next(),(TypeSymbol)is.next());
  }
  
  public TypeSymbol lookup(LocatedString name)
  {
    TypeSymbol res;
    if(map.containsKey(name))
      return (TypeSymbol)map.get(name);

    if(outer!=null)
      return outer.lookup(name);

    JavaTypeConstructor tc = JavaTypeConstructor.lookup(name);
    if(tc!=null)
      return tc;
    
    return null;
  }

  /****************************************************************
   * Debugging
   ****************************************************************/

  public String toString()
  {
    return map.toString()+";;\n"+outer;
  }

  public bossa.modules.Module module; //non-null only in the global type scope
  
  private TypeScope outer;
  private Map map;
}
