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
//$Modified: Fri Jun 16 16:13:04 2000 by Daniel Bonniot $

package mlsub.typing;

import java.util.*;

import mlsub.typing.lowlevel.*;

/**
 * Variance of a type constructor
 * 
 * @author bonniot
 */

public final class Variance 
  implements Kind /* Variance is the Kind of MonotypeConstructors */
{
  private Variance(int n)
  {
    this.size = n;
    this.top = new Interface(this);
  }

  public static final Variance make(int n)
  {
    Variance res = variances[n];
    if (res==null)
      return variances[n] = new Variance(n);
    else
      return res;
  }
  
  private static final Variance[] variances = new Variance[20];

  /****************************************************************
   * The top interface
   ****************************************************************/
  
  /**
   * top interface for this variance.
   */
  public final Interface top;
  
  public mlsub.typing.lowlevel.Engine.Constraint getConstraint()
  {
    return mlsub.typing.lowlevel.Engine.getConstraint(this);
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
    if(initial) throw new InternalError("initial leq in Variance");
    leq(e1,e2);
  }

  public void leq(Element e1, Element e2)
    throws Unsatisfiable
  {
    Monotype m1 = (Monotype) e1,  m2 = (Monotype) e2;
    
    TypeConstructor tc1 = m1.getTC();
    TypeConstructor tc2 = m2.getTC();
    Monotype[] tp1 = m1.getTP();
    Monotype[] tp2 = m2.getTP();
    
    mlsub.typing.lowlevel.Engine.leq(tc1,tc2);
    assertEq(tp1,tp2);
  }
  
  /**
   * Asserts the inequalities between type parameters 
   * belonging to this variance
   *
   * @param tp1 a least Collection of Monotypes
   * @param tp2 a greatest Collection of Monotypes
   */
  public void assertEq(Monotype[] tp1, Monotype[] tp2)
    throws mlsub.typing.lowlevel.Unsatisfiable
  {
    if (size==0)
      if (tp1==null && tp2==null)
	//OK
	return;
      else
	throw new InternalError("Incorrect sizes"+tp2.length);
    
    if(tp1.length!=size)
      throw new BadSizeEx(size,tp1.length);
    if(tp2.length!=size)
      throw new BadSizeEx(size,tp2.length);
    for(int i=0; i<size; i++)
      {
	//Non-variant
	mlsub.typing.lowlevel.Engine.leq(tp1[i],tp2[i]);
	mlsub.typing.lowlevel.Engine.leq(tp2[i],tp1[i]);
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
