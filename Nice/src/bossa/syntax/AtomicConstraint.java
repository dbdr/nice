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

// File    : AtomicConstraint.java
// Created : Mon Jul 19 16:40:18 1999 by bonniot
//$Modified: Mon Aug 30 17:45:18 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
 * A constraint atom. See childs
 *
 * @see Constraint
 */
public abstract class AtomicConstraint extends Node
{
  AtomicConstraint()
  {
    super(Node.down);
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  abstract AtomicConstraint resolve(TypeScope scope);
  
  static List resolve(TypeScope scope, Collection c)
  {
    List res=new ArrayList(c.size());
    Iterator i=c.iterator();

    while(i.hasNext())
      res.add( ((AtomicConstraint)i.next()).resolve(scope));

    return res;
  }

  abstract AtomicConstraint substitute(Map map);

  static List substitute(Map map, Collection c)
  {
    List res=new ArrayList(c.size());
    Iterator i=c.iterator();

    while(i.hasNext())
      res.add( ((AtomicConstraint)i.next()).substitute(map));

    return res;
  }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  /**
   * Enter the constraint into the typing context
   *
   */
  abstract void assert() throws bossa.typing.TypingEx;
  
  /**
   * Iterates assert
   *
   * @param atomics a collection of AtomicConstraints
   */
  static void assert(Collection atomics)
    throws bossa.typing.TypingEx
  {
    for(Iterator i=atomics.iterator();i.hasNext();)
      ((AtomicConstraint) i.next()).assert();
  }

}
