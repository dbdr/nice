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
//$Modified: Fri Jun 09 16:52:09 2000 by Daniel Bonniot $

package mlsub.typing;

import java.util.*;

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

  /**
   * Perform type symbol substitution inside the constraint.
   *
   * Does not need to create a new object, but must not
   * imperatively modify the constraint.
   *
   * @param map a map from TypeSymbols to TypeSymbols
   * @return an atomic constraint with substitution performed
   */
  AtomicConstraint substitute(java.util.Map map)
  {
    Monotype new1 = m1.substitute(map);
    Monotype new2 = m2.substitute(map);
    
    if (new1 == m1 && new2 == m2)
      return this;
    
    return new MonotypeLeqCst(new1, new2);
  }

  void assert()
  throws TypingEx
  {
    Typing.leq(m1,m2);
  }
  
  /****************************************************************
   * Manipulation
   ****************************************************************/
  
  public static Constraint constraint(Monotype[] c1, Monotype[] c2)
  {
    ArrayList a = new ArrayList(c1.length);
    
    for(int i = 0; i<c1.length; i++)
      {
	Monotype 
	  m1 = c1[i],
	  m2 = c2[i];

	// optimization: the constraint m<=m is useless since trivially true
	if(m1.equals(m2))
	  continue;
	
	a.add(new MonotypeLeqCst(m1,m2));
      }
    
    return new Constraint
      (null, (AtomicConstraint[]) a.toArray(new AtomicConstraint[a.size()]));
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return m1+" <: "+m2;
  }

  private Monotype m1,m2;
}
