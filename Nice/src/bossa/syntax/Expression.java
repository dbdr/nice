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

// File    : Expression.java
// Created : Mon Jul 05 16:25:02 1999 by bonniot
//$Modified: Thu Jul 22 14:52:38 1999 by bonniot $
// Description : 

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public abstract class Expression implements Located
{
  /** return an equivalent expression with scoping resolved */
  abstract Expression resolve(VarScope vs, TypeScope ts);

  /** iterates resolve on the collection of Expression */
  static Collection resolve(VarScope s, TypeScope t, Collection c)
  //TODO: imperative version ?
  {
    Collection res=new ArrayList();
    Iterator i=c.iterator();
    while(i.hasNext())
      res.add(((Expression)i.next()).resolve(s,t));
    return res;
  }

  boolean isAssignable()
  {
    return false;
  }

  /** returns the static type of the expression */
  abstract Type getType();

  /**
   * Maps getType over a collection of Expressions
   *
   * @param Expressions the colleciton of Expressions
   * @return the collection of their Types
   */
  static Collection getType(Collection expressions)
  {
    Iterator i=expressions.iterator();
    Collection res=new ArrayList(expressions.size());

    while(i.hasNext())
      res.add( ((Expression) i.next()) .getType());

    return res;
  }

  /** return the scope in which fields may be found */
  VarScope memberScope()
  {
    Type t=getType();
    VarScope res=t.memberScope();
    User.error(res==null,this+" is not a class, it has type "+t);
    return res;
  }

  public void setLocation(Location l)
  {
    loc=l;
  }

  public Location location()
  {
    return loc;
  }

  Location loc=Location.nowhere();
}
