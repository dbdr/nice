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
//$Modified: Wed Nov 10 16:49:52 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A constrained monotype.
 */
public class Polytype extends Node
{
  public Polytype(Constraint cst, Monotype monotype)
  {
    super(Node.down);
    this.constraint=cst;
    this.monotype=monotype;
    addChild(cst);
  }

  /** Constructs a Polytype with the "True" constraint */
  public Polytype(Monotype monotype)
  {
    this(Constraint.True(),monotype);
  }

  static Polytype bottom()
  {
    MonotypeVar alpha=Monotype.fresh(new LocatedString("alpha",
						       Location.nowhere()),
				     null);
    return new Polytype
      (new Constraint(alpha,null),
       alpha);
  }

  static Polytype voidType(TypeScope typeScope)
  {
    Monotype m=new MonotypeConstructor(new TypeConstructor(new LocatedString("void",Location.nowhere())),null,Location.nowhere());
    m=m.resolve(typeScope);
    
    return new Polytype(m);
  }
  
  static Collection fromMonotypes(Collection monotypes)
  {
    Collection res=new ArrayList(monotypes.size());
    for(Iterator i=monotypes.iterator();
	i.hasNext();)
      res.add(new Polytype(Constraint.True(),(Monotype)i.next()));
    return res;
  }
  
  public Polytype cloneType()
  {
    //Optimization
    if(!constraint.hasBinders())
      return this;

    Map map=new HashMap();
    List newBinders=new ArrayList(constraint.binders.size());
    for(Iterator i=constraint.binders.iterator();i.hasNext();)
      {
	TypeSymbol old=(TypeSymbol)i.next();
	TypeSymbol nou=old.cloneTypeSymbol();
	newBinders.add(nou);
	map.put(old,nou);
      }
    return new Polytype(new Constraint(newBinders,AtomicConstraint.substitute(map,constraint.atomics)),monotype.substitute(map));
  }
  
  //Acces methods
  public List getTypeParameters()
  {
    return new ArrayList(0);
  }

  public Constraint getConstraint()
  {
    return constraint;
  }

  static Collection getConstraint(Collection c)
  {
    Collection res=new ArrayList(c.size());
    for(Iterator i=c.iterator();
	i.hasNext();)
      res.add(((Polytype)i.next()).getConstraint());
    return res;
  }
  
  static Collection getMonotype(Collection c)
  {
    Collection res=new ArrayList(c.size());
    for(Iterator i=c.iterator();
	i.hasNext();)
      res.add(((Polytype)i.next()).getMonotype());
    return res;
  }
  
  public Monotype getMonotype()
  {
    return monotype;
  }

  /****************************************************************
   * Functional types
   ****************************************************************/

  List /* of Monotype */ domain()
  {
    return monotype.domain();
  }

  Monotype codomain()
  {
    return monotype.codomain();
  }

  /*******************************************************************
   * Scoping
   *******************************************************************/

  void resolve()
  {
    monotype=monotype.resolve(typeScope);
  }
  
  Polytype substitute(Map map)
  {
    return new Polytype(constraint.substitute(map),monotype.substitute(map));
  }

  void typecheck()
  {
    monotype.typecheck();
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
