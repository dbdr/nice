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
//$Modified: Fri Jul 16 19:21:30 1999 by bonniot $
// Description : a Scope level for types.
//   Is extended in each node that defined a new scope level


package bossa.syntax;

import java.util.*;
import bossa.util.*;

abstract class TypeScope
{
  public TypeScope(TypeScope outer)
  {
    this.outer=outer;
  }

  /** as to be defined in each extension */
  protected abstract TypeSymbol has(IdentType i);

  public TypeSymbol lookup(IdentType i)
  {
    TypeSymbol res=has(i);
    if(res!=null)
      return res;
    if(outer!=null)
      return outer.lookup(i);
    return null;
  }

  static TypeScope makeScope(TypeScope outer, 
			     final Collection /* of TypeSymbol */ locals)
  {
    TypeScope res=new TypeScope(outer)
    {
      public TypeSymbol has(IdentType id)
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

    //TODO: watch this
    // set the scope of the symbols
    //    Iterator i=locals.iterator();
    //while(i.hasNext())
    //  ((Symbol)i.next()).buildScope(res);

    return res;
  }

  private TypeScope outer;
}
