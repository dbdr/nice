/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : TupleKind.java
// Created : Wed Aug 02 15:36:53 2000 by Daniel Bonniot
//$Modified: Wed Aug 02 18:51:49 2000 by Daniel Bonniot $

package mlsub.typing;

import mlsub.typing.lowlevel.*;

/**
 * The kind of tuples (one per arity).
 * 
 * @author Daniel Bonniot
 */

public class TupleKind implements Kind
{
  public static TupleKind get(int arity)
  {
    if (tupleKinds[arity] == null)
      tupleKinds[arity] = new TupleKind(arity);
    return tupleKinds[arity];
  }
  
  private TupleKind(int arity)
  {
    this.arity = arity;
    // forces the creation of the constraint
    // we don't want it to be created during link.
    Engine.getConstraint(this);
  }

  /** Arity arbitrarily limited to 40 for implementation reasons. */
  private static final TupleKind tupleKinds[] = new TupleKind[40];
  
  public Monotype freshMonotype()
  {
    return new TupleType(MonotypeVar.news(arity));
  }
  
  public void register(Element e)
  {
  }
  
  public void leq(Element e1, Element e2, boolean initial)
    throws Unsatisfiable
  {
    if(initial)
      throw new InternalError("initial leq in TupleKind");
    leq(e1,e2);
  }
  
  public void leq(Element e1, Element e2)
    throws Unsatisfiable
  {
    TupleType t1 = tuple(e1), t2 = tuple(e2);
    
    Engine.leq(t1.types, t2.types);
  }
  
  private TupleType tuple(Element e)
  {
    try
      {
	return (TupleType) ((Monotype) e).equivalent();
      }
    catch(ClassCastException ex)
      {
	throw new InternalError
	  (e + " was expected to be a tuple type, it's a " + e.getClass());
      }
  }
  
  public boolean isBase()
  {
    return false;
  }
  
  int arity;
}
