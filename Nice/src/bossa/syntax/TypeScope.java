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
//$Modified: Mon Apr 03 15:16:34 2000 by Daniel Bonniot $

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
    map.put(s.getName().toString(),s);
  }
  
  void addSymbols(Collection c)
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      addSymbol((TypeSymbol)i.next());
  }

  void addMapping(String name, TypeSymbol s)
  {
    map.put(name,s);
  }

  void addMappings(Collection names, Collection symbols) throws BadSizeEx
  {
    Iterator is;
    if(symbols==null)
      is=null;
    else
      {
	is=symbols.iterator();
	if(names.size()!=symbols.size())
	  throw new BadSizeEx(names.size(),symbols.size());
      }
    
    for(Iterator in=names.iterator();
	in.hasNext();)
      {
	Object name_o = in.next();
	String name;
	if(name_o instanceof LocatedString)
	  name = ((LocatedString) name_o).toString();
	else
	  name = (String) name_o;
	
	if(is==null)
	  addMapping(name,TypeSymbol.dummy);
	else
	  addMapping(name,(TypeSymbol)is.next());
      }
  }
  
  public TypeSymbol lookup(String name)
  {
    TypeSymbol res;
    if(map.containsKey(name))
      return (TypeSymbol)map.get(name);

    if(outer!=null)
      return outer.lookup(name);
    else
      // We are in the global type scope
      {
	TypeConstructor tc = JavaTypeConstructor.lookup(name);
	    
	if(tc!=null)
	  return tc;
	    
	for(Iterator i = module.listImplicitPackages(); i.hasNext();)
	  {
	    String pkg = ((LocatedString) i.next()).toString();
	    String fullName = pkg+"."+name;

	    if(map.containsKey(fullName))
	      return (TypeSymbol)map.get(fullName);

	    tc = JavaTypeConstructor.lookup(fullName);
	    if(tc!=null)
	      return tc;
	  }
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
