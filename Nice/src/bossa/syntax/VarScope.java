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
//$Modified: Fri Jul 23 13:15:41 1999 by bonniot $
// Description : a Scope level for variables.
//   Is extended in each node that defined a new scope level


package bossa.syntax;

import java.util.*;
import bossa.util.*;

abstract class VarScope
{
  public VarScope(VarScope outer)
  {
    this.outer=outer;
  }

  /**
   * Has to be defined in each extension.
   *
   * @param i the identifier to lookup
   * @return the symbol if it was found, null otherwise
   */
  protected abstract VarSymbol has(LocatedString i);

  /**
   * The lookup method to call when you need to get a VarSymbol
   * from its name
   *
   * @param i the identifier to lookup
   * @return the symbol if it was found, null otherwise
   */
  public VarSymbol lookup(LocatedString i)
  {
    VarSymbol res=has(i);
    if(res!=null)
      return res;
    if(outer!=null)
      return outer.lookup(i);
    return null;
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

  /**
   * Creates a scope which defines
   * the provided VarSymbols
   *
   * @param outer the outer scope
   * @param locals collection of VarSymbols
   * @return the new Scope
   * @exception DuplicateIdentEx if the same identifer occurs twice
   */
  static VarScope makeScope(VarScope outer, 
			    final Collection /* of VarSymbol */ locals)
    throws DuplicateIdentEx
  {
    checkDuplicates(locals);
    VarScope res=new VarScope(outer)
      {
	public VarSymbol has(LocatedString id)
	  {
	    Iterator i=locals.iterator();
	    while(i.hasNext())
	      {
		VarSymbol s=(VarSymbol)i.next();
		if(s.hasName(id))
		  return s;
	      }
	    return null;
	  }
      };

    return res;
  }

  private VarScope outer;
}
