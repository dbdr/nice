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
//$Modified: Fri Jun 09 15:14:00 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A Scope level for variables.
 * Is extended in each node that defined a new scope level.
 */
class VarScope
{
  public VarScope(VarScope outer, boolean isStop)
  {
    this.outer = outer;
    this.defs = new HashMultiTable();
    this.stop = isStop;
  }
  
  public VarScope(VarScope outer, 
		  Collection /* of VarSymbol */ defs,
		  boolean isStop)
  {
    this(outer, isStop);
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
    Iterator i = c.iterator();
    while(i.hasNext())
      {
	VarSymbol s = (VarSymbol)i.next();
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
  public List lookup(LocatedString i)
  {
    List res = defs.getAll(i);
    
    if(outer!=null)
      if(!stop || res==null)
	{
	  if(res==null)
	    res = new ArrayList();
	  res.addAll(outer.lookup(i));
	}
    
    if(res==null)
      res = new LinkedList();
    return res;
  }

  /** Do not stop at stops. */
  public List lookupGlobal(LocatedString i)
  {
    List res = defs.getAll(i);
    
    if(outer!=null)
      {
	if(res==null)
	  res = new ArrayList();
	res.addAll(outer.lookupGlobal(i));
      }
    
    if(res==null)
      res = new LinkedList();
    return res;
  }

  public VarSymbol lookupOne(LocatedString s)
  {
    Collection i = lookup(s);
    if(i==null || i.size()==0)
      return null;
    if(i.size()>1)
      User.error(s,s+"'s usage is ambiguous");
    return (VarSymbol)i.iterator().next();
  }

  public VarSymbol lookupLast(LocatedString s)
  {
    VarSymbol res = (VarSymbol)defs.getLast(s);
    if(res!=null)
      return res;
    if(outer==null)
      return null;
    return outer.lookupLast(s);
  }
  
  public boolean overloaded(LocatedString s)
  {
    if(defs.containsKey(s))
      return defs.elementCount(s)>1 || (outer!=null && outer.lookup(s).size()>0);
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
    Collection seen = new ArrayList(symbols.size());
    Iterator i = symbols.iterator();
    while(i.hasNext())
      {
	name = ((VarSymbol)i.next()).name;
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
    return defs.elementCount()+";;\n"+outer;
  }
  
  private VarScope outer;
  private HashMultiTable defs;
  private boolean stop;
}
