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
//$Modified: Fri Jul 09 12:03:50 1999 by bonniot $
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

  /** as to be defined in each extension */
  protected abstract VarSymbol has(Ident i);

  public VarSymbol lookup(Ident i)
  {
    VarSymbol res=has(i);
    if(res!=null)
      return res;
    if(outer!=null)
      return outer.lookup(i);
    return null;
  }

  static VarScope makeScope(VarScope outer, 
			    final Collection /* of VarSymbol */ locals)
  {
    VarScope res=new VarScope(outer)
      {
	public VarSymbol has(Ident id)
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
