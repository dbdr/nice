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
import mlsub.typing.TypeConstructor;
import mlsub.typing.Variance;

/**
   A Scope level for types.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class TypeScope implements TypeMap
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

  void addSymbols(TypeSymbol[] c)
  throws DuplicateName
  {
    for (int i = c.length; --i >= 0;)
      addSymbol(c[i]);
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
    if (old != null)
      throw new DuplicateName(name, (TypeSymbol) old, s);
    
    map.put(name,s);
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
    TypeSymbol res = (TypeSymbol) map.get(name);
    if (res != null)
      return res;

    if(outer!=null)
      return outer.lookup(name, loc);

    // This is the global type scope
    boolean notFullyQualified = name.indexOf('.') == -1;
	
    if (notFullyQualified && (module != null))
      {
	/* Try first to find the symbol in Nice definitions.
	   The first package is the current package.
	   If the symbol is not found there, we check there is no 
	   ambiguity with another symbol from another package.
	*/
	boolean first = true;
	String[] pkgs = module.listImplicitPackages();
	for (int i = 0; i < pkgs.length; i++)
	  {
	    String fullName = pkgs[i] + "." + name;
	    TypeSymbol sym = (TypeSymbol) map.get(fullName);
	    if (sym != null)
	      if (res == null)
		{
		  res = sym;
		  if (first) break;
		}
	      else
		User.error(loc, "Ambiguity for symbol " + name + 
			   ":\n" + res + " and " + sym +
			   " both exist");
	    first = false;
	  }
      }

    if (res != null)
      return res;

    res = lookupNative(name, loc);
    
    if (res != null)
      return res;

    // Try inner classes
    StringBuffer innerName = new StringBuffer(name);
    for (int i = innerName.length(); --i > 0; )
      if (innerName.charAt(i) == '.')
	{
	  innerName.setCharAt(i, '$');
	  res = lookupNative(innerName.toString(), loc);
	  if (res != null)
	    return res;
	}

    return null;
  }

  private TypeSymbol lookupNative(String name, Location loc)
  {
    TypeSymbol res = null;
    TypeConstructor tc = JavaClasses.lookup(name);
	
    if (tc != null)
      return tc;
	
    boolean notFullyQualified = name.indexOf('.') == -1;
    boolean first = true;
    if (notFullyQualified && (module != null))
      {
	String[] pkgs = module.listImplicitPackages();
	for (int i = 0; i < pkgs.length; i++)
	  {
	    String fullName = pkgs[i] + "." + name;
	    tc = JavaClasses.lookup(fullName);
	    if (tc != null)
	      if (res == null)
		{
		  res = tc;
		  if (first) break;
		}
	      else
		User.error(loc, "Ambiguity for native class " + name + 
			   ":\n" + res + " and " + tc +
			   " both exist");
	    first = false;
	  }
      }
    
    return res;
  }
  
  /****************************************************************
   * Debugging
   ****************************************************************/

  public String toString()
  {
    return map.toString() + 
      (outer != null ? ";;\n" + outer : "");
  }

  public Module module; //non-null only in the global type scope

  private TypeScope outer;
  private Map map;
}
