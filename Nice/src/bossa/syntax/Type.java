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

// File    : Type.java
// Created : Thu Jul 01 19:28:28 1999 by bonniot
//$Modified: Fri Jul 09 21:03:32 1999 by bonniot $
// Description : Abstract syntactic type

package bossa.syntax;

import java.util.*;

abstract public class Type
{
  /** return the scope in which fields may be found */
  VarScope memberScope()
  {
    return null;
  }

  /** instantiates the type with the type parameters */
  abstract Type instantiate(Collection parameters);

  /** the list of input types if this type is functional */
  Collection domain()
  {
    return null;
  }

  /** the return type if this type is functional */
  Type codomain()
  {
    return null;
  }

  /** resolve scoping */
  abstract Type resolve(TypeScope s);

  /** iterates resolve on the collection of Type */
  static Collection resolve(TypeScope s, Collection c)
  //TODO: imperative version ?
  {
    Collection res=new ArrayList();
    Iterator i=c.iterator();
    while(i.hasNext())
      res.add(((Type)i.next()).resolve(s));
    return res;
  }

  /** called instead of toString if parenthesis are unnecessary */
  public String toStringExtern()
  {
    return toString();
  }
}


