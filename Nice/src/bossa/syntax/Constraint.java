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

// File    : Constraint.java
// Created : Fri Jul 02 17:51:35 1999 by bonniot
//$Modified: Fri Aug 27 17:29:52 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A constraint between type constructors
 *
 * @see AtomicConstraint
 */
public class Constraint extends Node
{
  /**
   * Creates the constraint \forall binders . atomics
   *
   * @param binders a collection of TypeSymbols
   * @param atomics a collection of AtomicConstraints
   */
  public Constraint(List binders, List atomics)
  {
    super(Node.global);
    construct(binders,atomics);
  }
  
  Constraint(MonotypeVar binder, List atomics)
  {
    super(Node.global);
    List binders=new ArrayList(1);
    binders.add(binder);
    construct(binders,atomics);
  }
      
  private void construct(List binders, List atomics)
  {
    if(binders==null)
      binders=new ArrayList(0);
    else
      addTypeSymbols(binders);
    this.binders=binders;
    this.atomics=addChildren(atomics);
  }

  boolean hasBinders()
  {
    return binders.size()>0;
  }
  
  /**
   * The trivial constraint 
   *
   * @return a constraint with no binders, always true
   */
  public static final Constraint True()
  { 
    return new Constraint(new ArrayList(0),new ArrayList(0));
  }

  Constraint shallowClone()
  {
    return new Constraint((List)((ArrayList)binders).clone(),(List)((ArrayList)atomics).clone());
  }

  public static Constraint and(Constraint c1, Constraint c2)
  {
    Constraint res=c1.shallowClone();
    c1.addBinders(c2.binders);

    return res;
  }

  Constraint and(Collection c)
  {
    Constraint res=shallowClone();
    for(Iterator i=c.iterator();
	i.hasNext();)
      res.and((Constraint) i.next());
    return res;
  }

  void and(Constraint c)
  {
    this.addBinders(c.binders);
    this.atomics.addAll(c.atomics);
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  void resolve()
  {
    atomics=AtomicConstraint.resolve(typeScope,atomics);
  }
  
  Constraint substitute(Map map)
  {
    //TODO:check binders do not conflict with keys in map,
    // or is it done before ?
    return new Constraint(binders,AtomicConstraint.substitute(map,atomics));
  }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  public void assert()
    throws bossa.typing.TypingEx
  {
    bossa.typing.Typing.introduce(binders);
    AtomicConstraint.assert(atomics);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    if(atomics.size()==0)
      return Util.map("{",", ","}",binders);
    else if(binders.size()==0)
      return Util.map("{",", ","}",atomics);
    else 
      return 
	Util.map("{",", ","|",binders)
	+ Util.map("",", ","}",atomics);
  }

  /****************************************************************
   * Internal manipulation
   ****************************************************************/

  /**
   * Adds binders that are not already present
   *
   * @param b a collection of TypeSymbol
   */
  private void addBinders(Collection b)
  {
    Iterator i=b.iterator();
    while(i.hasNext())
      {
	//TODO: optimize by removing the cast
	TypeSymbol s=(TypeSymbol)i.next();
	if(!binders.contains(s))
	  binders.add(s);
      }
  }
  
  public List /* of TypeSymbol */ binders;
  List /* of AtomicConstraint */ atomics;
}
