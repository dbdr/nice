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
//$Modified: Thu Jul 29 10:46:01 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A Scope level for types.
 * Is extended in each node that defined a new scope level.
 */
abstract class TypeScope
{
  public TypeScope(TypeScope outer)
  {
    this.outer=outer;
  }

  /** has to be defined in each extension */
  protected abstract TypeSymbol has(LocatedString name);

  public TypeSymbol lookup(LocatedString name)
  {
    TypeSymbol res=has(name);
    if(res!=null)
      return res;
    if(outer!=null)
      return outer.lookup(name);
    return null;
  }

  /**
   * Creates a scope which defines
   * the provided TypeSymbols
   *
   * @param outer the outer scope
   * @param locals collection of TypeSymbols
   * @return the new Scope
   * @exception NOT DuplicateIdentEx if the same identifer occurs twice
   */
  static TypeScope makeScope(TypeScope outer, 
			     final Collection /* of TypeSymbol */ locals)
  {
    Iterator ii=locals.iterator();
    while(ii.hasNext())
      {
	TypeSymbol s=(TypeSymbol)ii.next();
      }

    TypeScope res=new TypeScope(outer)
    {
      public TypeSymbol has(LocatedString id)
      {
	Iterator i=locals.iterator();
	while(i.hasNext())
	{
	  TypeSymbol s=(TypeSymbol)i.next();
	  if(s.hasName(id))
	  return s;
	}
	return null;
      }
    };

    return res;
  }

  /** returns a new TypeScope that maps vars to values */
  static TypeScope makeScope(TypeScope outer, 
			     final Collection /* of LocatedString */ vars,
			     final Collection /* of TypeSymbol*/ values)
    throws BadSizeEx
  {
    if(vars.size()!=values.size())
      throw new BadSizeEx(values.size(),vars.size());

    TypeScope res=new TypeScope(outer)
    {
      public TypeSymbol has(LocatedString id)
      {
	Iterator ivar=vars.iterator();
	Iterator ival=values.iterator();
	while(ivar.hasNext())
	{
	  LocatedString s=(LocatedString)ivar.next();
	  TypeSymbol v=(TypeSymbol)ival.next();
	  if(s.equals(id))
            return v;
	}
	return null;
      }
    };

    return res;
  }

  private TypeScope outer;
}
