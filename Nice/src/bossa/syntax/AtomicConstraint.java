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
//$Modified: Fri Jul 23 15:56:58 1999 by bonniot $

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

  static Collection substitute(Map map, Collection c)
  {
    Collection res=new ArrayList(c.size());
    Iterator i=c.iterator();

    while(i.hasNext())
      res.add( ((AtomicConstraint)i.next()).substitute(map));

    return res;
  }
}
