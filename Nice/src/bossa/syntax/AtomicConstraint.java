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
//$Modified: Tue Jul 27 10:23:00 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
 * A constraint atom. See childs
 *
 * @see Constraint
 */
public abstract class AtomicConstraint
{
  abstract AtomicConstraint substitute(Map map);

  /****************************************************************
   * Scoping
   ****************************************************************/

  abstract AtomicConstraint resolve(TypeScope scope);

  static Collection resolve(TypeScope scope, Collection c)
  {
    Collection res=new ArrayList(c.size());
    Iterator i=c.iterator();

    while(i.hasNext())
      res.add( ((AtomicConstraint)i.next()).resolve(scope));

    return res;
  }

  static Collection substitute(Map map, Collection c)
  {
    Collection res=new ArrayList(c.size());
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
