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
//$Modified: Wed Aug 18 13:29:52 1999 by bonniot $

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
    this.m1=m1;
    this.m2=m2;
  }

  static Constraint constraint(Collection c1, Collection c2)
  {
    List a=new ArrayList(c1.size());
    
    for(Iterator i1=c1.iterator(),i2=c2.iterator();
	i1.hasNext();)
      a.add(new MonotypeLeqCst((Monotype) i1.next(),
			       (Monotype) i2.next()));
    
    return new Constraint(new ArrayList(0),a);
  }
  
  AtomicConstraint substitute(java.util.Map map)
  {
    return new MonotypeLeqCst(m1.substitute(map),m2.substitute(map));
  }

  AtomicConstraint resolve(TypeScope ts)
  {
    m1=m1.resolve(ts);
    m2=m2.resolve(ts);
    return this;
  }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  void assert()
    throws bossa.typing.TypingEx
  {
    bossa.typing.Typing.leq(m1,m2);
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
