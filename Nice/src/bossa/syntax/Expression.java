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
//$Modified: Thu Aug 19 14:33:34 1999 by bonniot $
// Description : 

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public abstract class Expression extends Node 
  implements Located
{
  Expression()
  {
    super(Node.down);
  }
  
  /** 
   * Returns an equivalent expression with scoping resolved 
   * Expressions that resolve to a new expressions should
   * override this method.
   * Others (that resolve by side effects) 
   * should override Node.resolve().
   */
  Expression resolveExp()
  {
    doResolve();
    return this;
  }

  /** iterates resolveExp on the collection of Expression */
  static List resolveExp(Collection c)
  //TODO: imperative version ?
  {
    List res=new ArrayList();
    Iterator i=c.iterator();
    while(i.hasNext())
      res.add(((Expression)i.next()).resolveExp());
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
   * @param Expressions the collection of Expressions
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

  /**
   * Maps getType over a collection of Expressions
   * and checks all Types are Polytypes and not
   * PolytypeConstructors
   *
   * @param Expressions the collection of Expressions
   * @return the collection of their Polytypes, 
   * or null if there is a PolytypeConstructor
   */
  static Collection getPolytype(Collection expressions)
  {
    Iterator i=expressions.iterator();
    Collection res=new ArrayList(expressions.size());

    while(i.hasNext())
      {
	Type t=((Expression) i.next()) .getType();
	if(!(t instanceof Polytype))
	  return null;
	res.add(t);
      }

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
