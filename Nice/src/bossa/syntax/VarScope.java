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

/**
   A Scope level for variables.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
final class VarScope
{
  public VarScope(VarScope outer)
  {
    this.outer = outer;
    this.defs = new HashMultiTable();
  }
  
  public VarScope(VarScope outer, 
		  Collection /* of VarSymbol */ defs)
  {
    this(outer);
    addSymbols(defs);
  }

  void addSymbol(VarSymbol s)
  {
    this.defs.put(s.name,s);
  }
  
  /**
     Adds a collection of VarSymbols
   */
  void addSymbols(Collection c)
  {
    if (c == null) return;

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
   * @return the symbols if some were found, null otherwise
   */
  public List lookup(LocatedString i)
  {
    List res = defs.getAll(i);
    
    if(res!=null)
      return res;
    
    if(outer!=null)
      return outer.lookup(i);

    return new LinkedList();
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
}
