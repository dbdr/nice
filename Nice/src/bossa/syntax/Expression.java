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
//$Modified: Thu Sep 30 17:02:15 1999 by bonniot $
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

  boolean isFieldAccess()
  {
    return false;
  }
  
  /**
   * Resolves overloading, taking into account the parameters the expressions is applied to.
   *
   * @param tp type parameters to instantiate before applying the parameters, or null.
   * @return the resolved expression. Doesn't return if OR is not possible.
   */
  Expression resolveOverloading(List /* of Expression */ parameters, TypeParameters tp)
  {
    return this;
  }
  
  /**
   * Resolve overloading, assuming that this expression
   * should have some Type.
   *
   * @param expectedType the type this expression should have.
   * @return the resolved expression. Doesn't return if OR is not possible.
   */
  Expression resolveOverloading(Type expectedType, TypeParameters typeParameters)
  {
    return this;
  }
  
  /** computes the static type of the expression */
  abstract void computeType();

  final Type getType()
  {
    if(type==null)
      computeType();
    return type;
  }
  
  /**
   * Maps getType over a collection of Expressions
   *
   * @param Expressions the list of Expressions
   * @return the list of their Types
   */
  static List getType(List expressions)
  {
    Iterator i=expressions.iterator();
    List res=new ArrayList(expressions.size());

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
	Type t=((Expression) i.next()).getType();
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
  protected Type type;
}
