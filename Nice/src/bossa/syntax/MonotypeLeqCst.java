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

// File    : MonotypeLeqCst.java
// Created : Fri Jul 23 19:26:17 1999 by bonniot
//$Modified: Tue Jun 13 18:11:37 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;

import bossa.util.*;

/**
 * Inequality between monotypes
 * 
 * @author bonniot
 */

public class MonotypeLeqCst extends AtomicConstraint
{
  public MonotypeLeqCst(Monotype m1, Monotype m2)
  {
    this.m1 = m1;
    this.m2 = m2;
  }

  mlsub.typing.AtomicConstraint resolve(TypeScope ts)
  {
    return new mlsub.typing.MonotypeLeqCst
      (m1.resolve(ts), m2.resolve(ts));
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return m1+" <: "+m2;
  }

  Monotype m1,m2;
}
