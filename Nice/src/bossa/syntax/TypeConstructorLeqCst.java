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

// File    : TypeConstructorLeqCst.java
// Created : Sat Jul 24 12:02:15 1999 by bonniot
//$Modified: Tue Jun 13 19:01:18 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import mlsub.typing.TypeConstructor;
import mlsub.typing.TypeSymbol;
import mlsub.typing.Interface;

/**
 * Inequality between TypeConstructors.
 * 
 * @author bonniot
 */

public class TypeConstructorLeqCst extends AtomicConstraint
{
  public TypeConstructorLeqCst(TypeConstructor t1, TypeIdent t2)
  {
    this.t1 = t1;
    this.t2 = t2;
  }

  mlsub.typing.AtomicConstraint resolve(TypeScope ts)
  {
    // If t2 resolve to an interface definition,
    // this constraint meant t1 implements t2

    TypeSymbol s = t2.resolveToTypeSymbol(ts);

    if(s == null)
      User.error(t2, t2 + " is not declared");

    if(s instanceof Interface)
      return new mlsub.typing.ImplementsCst(t1, (Interface) s);
    
    if (!(s instanceof TypeConstructor))
      Internal.error(t2+" resolved to a "+s.getClass());

    TypeConstructor tc2 = (TypeConstructor) s;

    ClassDefinition c2 = ClassDefinition.get(tc2);
    if(c2!=null)
      {
	Interface associatedInterface =
	  c2.getAssociatedInterface();
    
	if(associatedInterface!=null)
	  return new mlsub.typing.ImplementsCst(t1, associatedInterface);
      }

    return new mlsub.typing.TypeConstructorLeqCst(t1, tc2);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return t1+" <: "+t2;
  }

  String getParentFor(TypeConstructor tc)
  {
    if(this.t1 == tc)
      return t2.toString();
    else
      return null;
  }

  TypeConstructor t1;
  TypeIdent t2;
}
