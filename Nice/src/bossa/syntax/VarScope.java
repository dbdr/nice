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

// File    : VarScope.java
// Created : Fri Jul 09 11:28:11 1999 by bonniot
//$Modified: Tue Sep 21 16:45:10 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A Scope level for variables.
 * Is extended in each node that defined a new scope level.
 */
class VarScope
{
  public VarScope(VarScope outer)
  {
    this.outer=outer;
    this.defs=new HashMultiTable();
  }
  
  public VarScope(VarScope outer, Collection /* of VarSymbol */ defs)
  {
    this(outer);
    addSymbols(defs);
  }

  void addSymbol(VarSymbol s)
  {
    this.defs.put(s.name,s);
  }
  
  /**
   * Adds a collection of VarSymbols
   *
   */
  void addSymbols(Collection c)
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      {
	VarSymbol s=(VarSymbol)i.next();
	addSymbol(s);
      }
  }
  
  /**
   * The lookup method to call when you need to get a VarSymbol
   * from its name
   *
   * @param i the identifier to lookup
   * @return the symbols if it was found, null otherwise
   */
  public Collection lookup(LocatedString i)
  {
    Collection res=defs.getAll(i);
    if(res==null)
      res=new ArrayList();
    if(outer!=null)
      res.addAll(outer.lookup(i));
    return res;
  }

  public VarSymbol lookupOne(LocatedString s)
  {
    Collection i=lookup(s);
    if(i==null || i.size()==0)
      return null;
    User.error(i.size()>1,s,s+"'s usage is ambiguous");
    return (VarSymbol)i.iterator().next();
  }

  public VarSymbol lookupLast(LocatedString s)
  {
    VarSymbol res=(VarSymbol)defs.getLast(s);
    if(res!=null)
      return res;
    if(outer==null)
      return null;
    return outer.lookupLast(s);
  }
  
  public boolean overloaded(LocatedString s)
  {
    if(defs.containsKey(s))
      return defs.elementCount(s)>1 || outer.lookup(s).size()>0;
    else if(outer!=null)
      return outer.overloaded(s);
    else 
      return false;
  }
	
  /**
   * Verifies that a collection of VarSymbol
   * does not contains twice the same identifier
   *
   * @param symbols the collection of VarSymbols
   * @exception DuplicateIdentEx if the same identifer occurs twice
   */
  static void checkDuplicates(Collection symbols)
    throws DuplicateIdentEx
  {
    LocatedString name;
    Collection seen=new ArrayList(symbols.size());
    Iterator i=symbols.iterator();
    while(i.hasNext())
      {
	name=((VarSymbol)i.next()).name;
	if(seen.contains(name))
	  throw new DuplicateIdentEx(name);
	seen.add(name);
      }
  }

  /****************************************************************
   * Debugging
   ****************************************************************/
  
  public String toString()
  {
    return defs+";;\n"+outer;
  }
  
  private VarScope outer;
  private HashMultiTable defs;
}
