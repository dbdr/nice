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
//$Modified: Mon Jun 05 18:18:07 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import mlsub.typing.TypeSymbol;
import mlsub.typing.TypeConstructor;

/**
 * A Scope level for types.
 * Is extended in each node that defined a new scope level.
 */
public class TypeScope
{
  public TypeScope(TypeScope outer)
  {
    this.outer = outer;
    this.map = new HashMap();
  }

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

  class DuplicateName extends Exception
  {
    DuplicateName(String name, TypeSymbol old, TypeSymbol nou)
    {
      super(name+" is defined twice");
    }
  }
  
  void addMapping(String name, TypeSymbol s)
  throws DuplicateName
  {
    Object old = map.get(name);
    if(old!=null && old!=dummyTypeSymbol)
      throw new DuplicateName(name, (TypeSymbol) old, s);
    
    map.put(name,s);
  }

  void addMappings(Collection names, TypeSymbol[] symbols) 
  throws DuplicateName
  {
    if(symbols!=null &&
       names.size()!=symbols.length)
      throw new Error(names.size()+" != "+symbols.length);
    
    int n = 0;
    for(Iterator in=names.iterator();
	in.hasNext();)
      {
	Object name_o = in.next();
	String name;
	if(name_o instanceof LocatedString)
	  name = ((LocatedString) name_o).toString();
	else
	  name = (String) name_o;
	
	if(symbols==null)
	  addMapping(name, dummyTypeSymbol);
	else
	  addMapping(name, symbols[n++]);
      }
  }
  
  /**
   * Used for the search of java classes, as the type symbol
   * of type binders. See MethodBodyDefinition.
   */
  private TypeSymbol dummyTypeSymbol = new TypeConstructor(null);

  public TypeSymbol lookup(String name)
  {
    TypeSymbol res;
    if(map.containsKey(name))
      return (TypeSymbol)map.get(name);

    if(outer!=null)
      return outer.lookup(name);
    else
      // This is the global type scope
      {
	TypeConstructor tc = JavaClasses.lookup(name);
	    
	if(tc!=null)
	  return tc;
	    
	if(module!=null)
	  for(Iterator i = module.listImplicitPackages(); i.hasNext();)
	    {
	      String pkg = ((LocatedString) i.next()).toString();
	      String fullName = pkg+"."+name;

	      if(map.containsKey(fullName))
		return (TypeSymbol)map.get(fullName);
	      
	      tc = JavaClasses.lookup(fullName);
	      if(tc!=null)
		return tc;
	    }
	else
	  Internal.warning("null module in TypeScope");
      }
    
    return null;
  }

  /****************************************************************
   * Debugging
   ****************************************************************/

  public String toString()
  {
    return map.toString()+";;\n"+outer;
  }

  public bossa.modules.Package module; //non-null only in the global type scope
  
  private TypeScope outer;
  private Map map;
}
