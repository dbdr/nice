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
//$Modified: Tue Jul 27 10:29:24 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A constraint between type constructors
 *
 * @see AtomicConstraint
 */
public class Constraint
{
  /**
   * Creates the constraint \forall binders . atomics
   *
   * @param binders a collection of TypeSymbols
   * @param atomics a collection of AtomicConstraints
   */
  public Constraint(Collection binders, Collection atomics)
  {
    if(binders==null)
      binders=new ArrayList(0);
    this.binders=binders;

    if(atomics==null)
      atomics=new ArrayList(0);
    this.atomics=atomics;
  }

  Constraint(MonotypeVar binder, Collection atomics)
  {
    this.binders=new ArrayList(1);
    binders.add(binder);

    if(atomics==null)
      atomics=new ArrayList(0);
    this.atomics=atomics;
  }
      
  /**
   * Nickname for True()
   *
   * @return the trivial constraint
   */
  public static final Constraint emptyConstraint()
  { 
    return new Constraint(new ArrayList(0),new ArrayList(0));
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

  Constraint cloneConstraint()
  {
    return new Constraint((Collection)((ArrayList)binders).clone(),
			  (Collection)((ArrayList)atomics).clone());
  }

  public static Constraint and(Constraint c1, Constraint c2)
  {
    Constraint res=c1.cloneConstraint();
    c1.binders.addAll(c2.binders);

    return res;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  void resolve(TypeScope scope)
  {
    atomics=AtomicConstraint.resolve(scope,atomics);
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

  public Collection /* of TypeSymbol */ binders;
  Collection /* of AtomicConstraint */ atomics;
}
