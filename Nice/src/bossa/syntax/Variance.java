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

// File    : Variance.java
// Created : Fri Jul 23 12:15:46 1999 by bonniot
//$Modified: Thu Jul 29 16:03:50 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.engine.*;

/**
 * Variance of a type constructor
 * 
 * @author bonniot
 */

public class Variance 
  implements Kind /* for MonotypeConstructors */
{
  public Variance(int n)
  {
    this.size=n;
  }

  public void register(Element e)
  {
  }
  
  public void leq(Element e1, Element e2)
    throws Unsatisfiable
  {
    Monotype m1=(Monotype) e1, m2=(Monotype) e2;
    
    TypeConstructor tc1=m1.getTC();
    TypeConstructor tc2=m2.getTC();
    TypeParameters  tp1=m1.getTP();
    TypeParameters  tp2=m2.getTP();
    
    bossa.engine.Engine.leq(tc1,tc2);
    assertEq(tp1,tp2);
  }
  
  /**
   * Asserts the inequalities between type parameters belonging to this variance
   *
   * @param tp1 a least Collection of Monotypes
   * @param tp2 a greatest Collection of Monotypes
   */
  public void assertEq(TypeParameters tp1, TypeParameters tp2)
    throws bossa.engine.Unsatisfiable
  {
    if(tp1.size()!=size)
      throw new BadSizeEx(size,tp1.size());
    if(tp2.size()!=size)
      throw new BadSizeEx(size,tp2.size());
    Iterator i1=tp1.iterator();
    Iterator i2=tp2.iterator();
    for(int i=1;i<=size;i++)
      {
	Monotype m1=(Monotype)i1.next();
	Monotype m2=(Monotype)i2.next();
	//Non-variant
	bossa.engine.Engine.leq(m1,m2);
	bossa.engine.Engine.leq(m2,m1);
      }
  }
  
  public boolean isBase()
  {
    return false;
  }
  
  /****************************************************************
   * Hastable oriented methods : usefull for the Engine to determine the right constraint
   ****************************************************************/
  
  public int hashCode()
  {
    return size;
  }
  
  public boolean equals(Object o)
  {
    return (o instanceof Variance) && (((Variance) o).size==size);
  }
  
  int size;
}
