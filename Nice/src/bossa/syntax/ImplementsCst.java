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

// File    : ImplementsCst.java
// Created : Fri Aug 27 10:45:33 1999 by bonniot
//$Modified: Tue Jun 06 18:14:16 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;
import mlsub.typing.*;

/**
 * A type constructor implements an interface.
 * 
 * @author bonniot
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
