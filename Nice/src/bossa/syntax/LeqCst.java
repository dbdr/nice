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

// File    : LeqCst.java
// Created : Mon Jul 19 16:42:14 1999 by bonniot
//$Modified: Fri Jul 23 19:36:13 1999 by bonniot $
// Description : Inequality between type constructors

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Inequality between type constructors
 */
public class LeqCst extends AtomicConstraint
{
  /**
   * The constraint t1 <: t2
   *
   */
  public LeqCst(TypeSymbol t1, TypeSymbol t2)
  {
    this.t1=t1;
    this.t2=t2;
  }

  AtomicConstraint substitute(Map map)
  {
    if(map.containsKey(t1.getName()) || map.containsKey(t2.getName()))
      {
	Monotype m1,m2;
	try{
	  m1=(Monotype)map.get(t1.getName());
	  m2=(Monotype)map.get(t2.getName());
	  return new MonotypeLeqCst(m1,m2);
	}
	catch(ClassCastException e) {
	  User.error(t1+" and "+t2+" cannot be compared");
	}
      }
    return this;
  }

  public String toString()
  {
    return t1+"<:"+t2;
  }

  TypeSymbol t1,t2;
}
