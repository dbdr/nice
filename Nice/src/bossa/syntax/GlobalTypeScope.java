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

import bossa.util.*;
import mlsub.typing.TypeSymbol;
import mlsub.typing.TypeConstructor;

/**
   The global, toplevel type scope.

   @version $Date$
   @author  Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class GlobalTypeScope extends TypeScope
{
  GlobalTypeScope()
  {
    super(null);
  }

  public TypeConstructor globalLookup(String name, Location loc)
  {
    TypeSymbol res = lookup(name, loc);

    if (res instanceof TypeConstructor)
      return (TypeConstructor) res;

    if (res != null)
      Internal.warning("Non type-constructor found in global type scope");

    return null;
  }

  TypeSymbol lookup(String name, Location loc)
  {
    TypeSymbol res = super.lookup(name, loc);
    if (res != null)
      return res;

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
	    TypeSymbol sym = get(fullName);
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

    return JavaClasses.lookup(name, loc);
  }

  public Module module;
}
