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
//$Modified: Sat Jul 24 13:47:28 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * Inequality between TypeConstructors
 * 
 * @author bonniot
 */

public class TypeConstructorLeqCst extends AtomicConstraint
{
  public TypeConstructorLeqCst(TypeConstructor m1, TypeConstructor m2)
  {
    this.m1=m1;
    this.m2=m2;
  }

  AtomicConstraint substitute(java.util.Map m)
  {
    return this;
  }

  AtomicConstraint resolve(TypeScope ts)
  {
    return this;
  }

  public String toString()
  {
    return m1+" <: "+m2;
  }

  TypeConstructor m1,m2;
}
