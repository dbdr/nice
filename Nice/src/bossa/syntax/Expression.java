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
//$Modified: Fri Jul 09 12:14:27 1999 by bonniot $
// Description : 

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public abstract class Expression
{
  /*
  class Resolution
  {
    Resolution(Expression e, Scope s)
    {
      exp=e; scope=s;
    }
    Expression expr;
    Scope scope;
  }
  */

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

  /** return the scope in which fields may be found */
  VarScope memberScope()
  {
    VarScope res=getType().memberScope();
    User.error(res==null,this+" is not a record");
    return res;
  }

}
