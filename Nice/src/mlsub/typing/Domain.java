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

// File    : Domain.java
// Created : Fri Jun 02 16:59:06 2000 by Daniel Bonniot
//$Modified: Thu Jun 15 16:25:14 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * Ex V. K. \theta
 * 
 * @author Daniel Bonniot
 */

public class Domain
{
  public Domain(Constraint constraint,
		Monotype monotype)
  {
    this.constraint = constraint;
    this.monotype = monotype;
  }

  public final static Domain bot = null;
  
  public Constraint getConstraint()
  {
    return constraint;
  }
  
  public Monotype getMonotype()
  {
    return monotype;
  }
  
  public static Domain[] fromMonotypes(Monotype[] monotypes)
  {
    Domain[] res = new Domain[monotypes.length];
    for(int i=0; i<monotypes.length; i++)
      res[i] = new Domain(Constraint.True, monotypes[i]);
    return res;
  }
  
  /**
   * Returns true iff a <= b.
   *
   * Only looks at the head, so is valid for link tests only.
   */
  /*
  public static boolean leq(Domain a, Domain b)
  {
    if(b==null) // Ex \alpha . \alpha
      return true;
    else if(a==null)
      return false;
    
    TypeConstructor ta = a.monotype.getTC();
    TypeConstructor tb = b.monotype.getTC();

    if(ta==null)
      throw new InternalError("Null tycon: "+a.toString()+" "+a.getClass());
    if(tb==null)
      throw new InternalError("Null tycon: "+b.toString()+" "+b.getClass());
    
    return Typing.testRigidLeq(ta,tb);
  }
  */
  /**
   * Returns true iff tycon tc \in domain d.
   *//*
  public static boolean in(TypeConstructor tc, Domain d)
  {
    if(d==Domain.bot)
      return true;
    // a null tc means a tag that connot be matched (e.g. a function)
    else if(tc==null)
      return false;
    
    TypeConstructor t = d.monotype.getTC();

    return Typing.testRigidLeq(tc,t);
  }
     */
  /****************************************************************
   * Misc
   ****************************************************************/

  public String toString()
  {
    return (constraint==null ? "" : "Ex "+constraint) + monotype.toString();
  }

  private Constraint constraint;
  private Monotype monotype;  
}
