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
import mlsub.typing.*;

/**
   A type constructor implements an interface.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class ImplementsCst extends AtomicConstraint
{
  public ImplementsCst(TypeIdent tc, TypeIdent itf)
  {
    this.tc=tc;
    this.itf=itf;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  mlsub.typing.AtomicConstraint resolve(TypeScope scope)
  {
    TypeConstructor stc = tc.resolveToTC(scope);
    
    TypeSymbol sitf = itf.resolveToTypeSymbol(scope);
    if (!(sitf instanceof Interface))
      User.error(itf, itf+" should be an interface");
    
    return new mlsub.typing.ImplementsCst
      (stc, (Interface) sitf);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return tc+":"+itf;
  }

  /*
  String getParentFor(TypeConstructor tc)
  {
    if(this.tc==tc)
      return def().toString();
    else
      return null;
  }
  */
  TypeIdent tc, itf;
}
