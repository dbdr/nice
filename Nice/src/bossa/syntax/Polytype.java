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

// File    : Polytype.java
// Created : Tue Jul 13 12:51:38 1999 by bonniot
//$Modified: Fri Jun 16 12:25:14 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

import mlsub.typing.TypeConstructor;

/**
   A constrained monotype.
 */
public class Polytype extends Node
{
  public Polytype(Constraint cst, Monotype monotype)
  {
    super(Node.down);
    this.constraint = cst;
    addChild(this.constraint);
    this.monotype = monotype;
  }

  /** Constructs a Polytype with the "True" constraint */
  public Polytype(Monotype monotype)
  {
    this(Constraint.True,monotype);
  }

  static Collection fromMonotypes(Collection monotypes)
  {
    Collection res = new ArrayList(monotypes.size());
    for(Iterator i = monotypes.iterator();
	i.hasNext();)
      res.add(new Polytype(Constraint.True, (Monotype) i.next()));
    return res;
  }  

  public Constraint getConstraint()
  {
    return constraint;
  }

  static Collection getMonotype(Collection c)
  {
    Collection res = new ArrayList(c.size());
    for(Iterator i = c.iterator();
	i.hasNext();)
      res.add(((Polytype)i.next()).getMonotype());
    return res;
  }
  
  public Monotype getMonotype()
  {
    return monotype;
  }

  /*******************************************************************
   * Scoping
   *******************************************************************/

  mlsub.typing.Polytype resolveToLowlevel()
  {
    return new mlsub.typing.Polytype(constraint.resolveToLowlevel(),
				     monotype.resolve(typeScope));
  }  

  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    return constraint+" "+monotype.toStringExtern();
  }

  private Constraint constraint;
  private Monotype monotype;
}
