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
// Created : Sat Jul 24 19:10:04 1999 by bonniot
//$Modified: Thu Dec 02 19:55:08 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A domain 
 * 
 * @author bonniot
 */

public class Domain
{
  public Domain(Constraint constraint, Monotype monotype)
  {
    this.constraint=constraint;
    this.monotype=monotype;
    this.monotypes=null;
  }

  /**
   * Constructs a 'tuple' Domain.
   *
   */
  public Domain(Constraint constraint, List /* of Monotype */ monotypes)
  {
    this.constraint=constraint;
    this.monotype=null;
    this.monotypes=monotypes;
  }

  public Monotype[] getTuple()
  {
    if(monotypes==null)
      Internal.error("Null monotypes");
    
    Object[] source = monotypes.toArray();
    Monotype[] res = new Monotype[source.length];
    System.arraycopy(source,0,res,0,source.length);
    
    return res;
  }

  public Monotype getMonotype()
  {
    if(monotype==null)
      Internal.error("Null monotype");
    
    return monotype;
  }
  
  // TODO: make better
  public final static Domain bot = null;
  
  static Domain bottom()
  {
    MonotypeVar alpha=Monotype.fresh(new LocatedString("alpha",
						       Location.nowhere()),
				     null);
    return new Domain
      (new Constraint(alpha,null),
       alpha);
  }
  
  /**
   * Returns true iff a <= b.
   *
   * Only looks at the head, valid for link tests only.
   */
  public static boolean leq(Domain a, Domain b)
  {
    if(b==null) // Ex \alpha . \alpha
      return true;
    else if(a==null)
      return false;
    
    TypeConstructor ta = a.monotype.getTC();
    TypeConstructor tb = b.monotype.getTC();

    if(ta==null)
      Internal.error("Null tycon: "+a.toString()+" "+a.getClass());
    if(tb==null)
      Internal.error("Null tycon: "+b.toString()+" "+b.getClass());
    
    return bossa.typing.Typing.testRigidLeq(ta,tb);
  }
  
  /**
   * Returns true iff tycon tc \in domain d.
   */
  public static boolean in(TypeConstructor tc, Domain d)
  {
    if(d==Domain.bot)
      return true;
    
    TypeConstructor t = d.monotype.getTC();

    return bossa.typing.Typing.testRigidLeq(tc,t);
  }

  public String toString()
  {
    if(monotypes!=null)
      return "Ex "+constraint+" "+Util.map("(",",",")",monotypes);
    else
      return "Ex "+constraint+" "+monotype;
  }

  static List fromMonotypes(Collection monotypes)
  {
    List res=new ArrayList(monotypes.size());
    for(Iterator i=monotypes.iterator();
	i.hasNext();)
      res.add(new Domain(Constraint.True(),(Monotype)i.next()));
    return res;
  }
  
  public Constraint constraint;
  public Monotype monotype;
  List /* of Monotype */ monotypes; // Used when the domain is a tuple
}
