/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
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
import mlsub.typing.TypeSymbol;

/**
   A Scope level for types.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
*/
public class TypeScope implements TypeMap
{
  public TypeScope(TypeScope outer)
  {
    this.outer = outer;
    this.map = new HashMap();
  }

  // only for GlobalTypeScope
  void setPackage(bossa.modules.Package pkg) {}
  bossa.modules.Package getPackage() { return null; }
  public mlsub.typing.TypeConstructor globalLookup(String name, Location loc)
  { return null; }

  void addSymbol(TypeSymbol s)
  throws DuplicateName
  {
    addMapping(s.toString(),s);
  }
  
  void addSymbols(Collection c)
  throws DuplicateName
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      addSymbol((TypeSymbol)i.next());
  }

  void addSymbols(TypeSymbol[] c)
  throws DuplicateName
  {
    for (int i = c.length; --i >= 0;)
      addSymbol(c[i]);
  }

  class DuplicateName extends Exception
  {
    DuplicateName(String name)
    {
      super(name + " is already declared");
    }
  }
  
  void addMapping(String name, TypeSymbol s)
  throws DuplicateName
  {
    Object old = map.put(name,s);
    if (old != null)
      throw new DuplicateName(name);
  }

  /** Change a previous mapping. */
  void updateMapping(String name, TypeSymbol s)
  {
    map.put(name, s);
  }

  void addMappings(Collection names, TypeSymbol[] symbols) 
  throws DuplicateName
  {
    if(symbols!=null &&
       names.size()!=symbols.length)
      throw new mlsub.typing.BadSizeEx(symbols.length, names.size());
    
    int n = 0;
    for(Iterator in = names.iterator(); in.hasNext();)
      addMapping((String) in.next(), symbols[n++]);
  }
  
  void addMappingsLS(Collection names, TypeSymbol[] symbols) 
  throws DuplicateName
  {
    if(symbols!=null &&
       names.size()!=symbols.length)
      throw new mlsub.typing.BadSizeEx(symbols.length, names.size());
    
    int n = 0;
    for(Iterator in = names.iterator(); in.hasNext();)
      addMapping(((LocatedString) in.next()).toString(), symbols[n++]);
  }
  
  TypeSymbol get(String name)
  {
    return (TypeSymbol) map.get(name);
  }

  public final TypeSymbol lookup(String name)
  {
    return lookup(name, null);
  }
  
  public final TypeSymbol lookup(LocatedString name)
  {
    return lookup(name.toString(), name.location());
  }

  TypeSymbol lookup(String name, Location loc)
  {
    TypeSymbol res = get(name);
    if (res != null)
      return res;

    if (outer != null)
      return outer.lookup(name, loc);

    return null;
  }

  /****************************************************************
   * Debugging
   ****************************************************************/

  public String toString()
  {
    return map.toString() + 
      (outer != null ? ";;\n" + outer : "");
  }

  private TypeScope outer;
  private Map map;
}
