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
//$Modified: Mon Aug 30 14:07:11 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * Inequality between TypeConstructors
 * 
 * @author bonniot
 */

public class TypeConstructorLeqCst extends AtomicConstraint
{
  public TypeConstructorLeqCst(TypeConstructor t1, TypeConstructor t2)
  {
    this.t1=t1;
    this.t2=t2;
  }

  AtomicConstraint substitute(java.util.Map m)
  {
    t1=t1.substitute(m);
    t2=t2.substitute(m);
    return this;
  }

  AtomicConstraint resolve(TypeScope ts)
  {
    t1=t1.resolve(ts);
    // If t2 resolve to an interface definition,
    // this constraint meant t1 implements t2
    TypeSymbol s=t2.resolveToTypeSymbol(ts);
    if(s instanceof InterfaceDefinition)
      return new ImplementsCst(t1,(InterfaceDefinition)s);
    else if(s instanceof TypeConstructor)
      t2=(TypeConstructor)s;
    else
      Internal.error(t2+" resolved to a "+s.getClass());
    return this;
  }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  void assert()
    throws bossa.typing.TypingEx
  {
    bossa.typing.Typing.leq(t1,t2);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return t1+" <: "+t2;
  }

  TypeConstructor t1,t2;
}
