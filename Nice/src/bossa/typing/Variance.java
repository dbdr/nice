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
//$Modified: Thu Feb 03 15:14:59 2000 by Daniel Bonniot $

package bossa.typing;

import java.util.*;
import bossa.util.*;

import bossa.engine.*;
import bossa.syntax.*;

/**
 * Variance of a type constructor
 * 
 * @author bonniot
 */

public class Variance 
  implements Kind /* Variance is the Kind of MonotypeConstructors */
{
  private Variance(int n)
  {
    this.size=n;
  }

  public static final Variance make(int n)
  {
    if(n==0)
      return nullVariance;
    else
      return new Variance(n);
  }
  
  // just to optimize memory footprint, Variance(0) is shared
  private static final Variance nullVariance = new Variance(0);
  
  public bossa.engine.Engine.Constraint getConstraint()
  {
    return bossa.engine.Engine.getConstraint(this);
  }
  
  public void register(Element e)
  {
//      if(!(e instanceof MonotypeConstructor))
//        return;
    
//      MonotypeConstructor mc = (MonotypeConstructor) e;
//      TypeConstructor tc = mc.getTC();
//      Engine.Constraint k = Engine.getConstraint(this);
    
//      if(tc.getKind()!=k)
//        if(tc.getKind()==null)
//  	Engine.setKind(tc,k);
//        else
//  	throw new TypingEx("Bad kinding for "+e);
  }
  

  public int newInterface()
  {
    return getConstraint().newInterface();
  }

  public void subInterface(int i1, int i2)
  {
    getConstraint().subInterface(i1,i2);
  }
  
  public void initialImplements(int x, int iid)
  {
    getConstraint().initialImplements(x,iid);
  }
  
  public void initialAbstracts(int x, int iid)
  {
    getConstraint().initialAbstracts(x,iid);
  }
  
  public void indexImplements(int x, int iid) throws Unsatisfiable
  {
    getConstraint().indexImplements(x,iid);
  }
  
  public void leq(Element e1, Element e2, boolean initial)
    throws Unsatisfiable
  {
    Internal.error(initial,"initial leq in Variance");
    leq(e1,e2);
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
  
  /****************************************************************
   * Hastable oriented methods : usefull for the Engine 
   * to determine the right constraint
   ****************************************************************/
  
  public int hashCode()
  {
    return size;
  }
  
  public boolean equals(Object o)
  {
    return (o instanceof Variance) && (((Variance) o).size==size);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "Variance (arity "+size+")";
  }
  
  public int size;
}
