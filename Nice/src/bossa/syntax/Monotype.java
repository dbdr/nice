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

// File    : Monotype.java
// Created : Thu Jul 01 19:28:28 1999 by bonniot
//$Modified: Fri Jul 23 18:46:28 1999 by bonniot $
// Description : Abstract syntactic type, without constraint

package bossa.syntax;

import java.util.*;
import bossa.util.*;

abstract public class Monotype
  implements Located
{
  abstract Monotype cloneType();

  Collection cloneTypes(Collection types)
  {
    Collection res=new ArrayList(types.size());
    Iterator i=types.iterator();
    while(i.hasNext())
      res.add((Monotype)i.next());
    return res;
  }

  /**************************************************************
   * Scoping
   **************************************************************/
  
  abstract Monotype resolve(TypeScope typeScope);

  /** iterates resolve on the collection of Monotype */
  static Collection resolve(TypeScope s, Collection c)
  //TODO: imperative version ?
  {
    Collection res=new ArrayList();
    Iterator i=c.iterator();
    while(i.hasNext())
      { 
	Monotype old=(Monotype)i.next();
	Monotype nou=old.resolve(s);
	User.error(nou==null,old+" : Monotype not defined");
	res.add(nou);
      }
    return res;
  }

  /** return the scope in which fields may be found */
  VarScope memberScope()
  {
    return null;
  }

  /** the list of input types if this type is functional */
  Collection domain()
  {
    return null;
  }

  /** the return type if this type is functional */
  Monotype codomain()
  {
    return null;
  }

  abstract Monotype substitute(Map map);

  static Collection substitute(Map map, Collection c)
  {
    Collection res=new ArrayList(c.size());
    Iterator i=c.iterator();

    while(i.hasNext())
      res.add( ((Monotype)i.next()).substitute(map));

    return res;
  }

   /****************************************************************
   * Printing
   ****************************************************************/

  /** called instead of toString if parenthesis are unnecessary */
  public String toStringExtern()
  {
    return toString();
  }

  /** don't print type parameters */
  public String toStringBase()
  {
    return toString();
  }

  Constraint constraint=Constraint.emptyConstraint();
}

