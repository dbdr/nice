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

import bossa.util.*;
import java.util.*;

/**
   Call of an object constructor.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class NewExp extends CallExp
{
  public NewExp(TypeIdent ti, Arguments arguments)
  {
    super(null, arguments);
    this.ti = ti;
  }

  private void resolveTC(TypeMap typeScope)
  {
    if (tc != null)
      return;
    
    tc = ti.resolveToTC(typeScope);
    ti = null;
    if(!TypeConstructors.instantiable(tc))
      if(TypeConstructors.constant(tc))
	User.error(this,
		   tc+" is abstract, it can't be instantiated");
      else
	User.error(this,
		   tc+" is a type variable, it can't be instantiated");
  }
  
  void resolve(TypeMap typeScope)
  {
    resolveTC(typeScope);
    
    LinkedList constructors = TypeConstructors.getConstructors(tc);
    if (constructors == null)
      User.error(this, tc + " has no constructor");

    // the list of constructors must be cloned, as
    // OverloadedSymbolExp removes elements from it
    constructors = (LinkedList) constructors.clone();

    function = new OverloadedSymbolExp
      (constructors, new LocatedString("new " + tc, location()), null);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    String cl = (ti == null ? tc.toString() : ti.toString());
    return "new " + cl + arguments;
  }

  private TypeIdent ti;
  private mlsub.typing.TypeConstructor tc;
}
