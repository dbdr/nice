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
//$Modified: Wed Sep 15 16:11:06 1999 by bonniot $
// Description : Abstract syntactic type, without constraint

package bossa.syntax;

import java.util.*;
import bossa.util.*;

abstract public class Monotype
  implements Located, bossa.engine.Element
{
  boolean allImperative(List symbols)
  {
    return false;
  }
  
  public abstract boolean appear(TypeSymbol s, boolean imperatively);
  
  static boolean appear(TypeSymbol s, boolean imperatively, Collection c)
  {
    for(Iterator i=c.iterator();i.hasNext();)
      {
	Monotype m=(Monotype)i.next();
	if(m.appear(s,imperatively))
	  return true;
      }
    return false;
  }
  
  abstract Monotype cloneType();

  Collection cloneTypes(Collection types)
  {
    Collection res=new ArrayList(types.size());
    Iterator i=types.iterator();
    while(i.hasNext())
      res.add((Monotype)i.next());
    return res;
  }

  /**
   * Constructs a fresh monotype
   *
   * @param associatedVariable a hint to make the name understandable by the user
   * @param m a monotype the new one should be comparable to
   * @return the fresh monotype
   */
  static MonotypeVar fresh(LocatedString associatedVariable,Monotype m)
  {
    MonotypeVar res=new MonotypeVar(true,associatedVariable);
    return res;
  }
  
  static MonotypeVar fresh(LocatedString associatedVariable)
  {
    return fresh(associatedVariable,null);
  }
  
  static List freshs(int arity, LocatedString associatedVariable)
  {
    List res=new ArrayList(arity);
    for(int i=1;i<=arity;i++)
      res.add(fresh(new LocatedString(associatedVariable.content+i,associatedVariable.location())));
    return res;
  }
  
  /**************************************************************
   * Scoping
   **************************************************************/
  
  abstract Monotype resolve(TypeScope ts);

  /** iterates resolve() on the collection of Monotype */
  static List resolve(TypeScope s, Collection c)
  //TODO: imperative version ?
  {
    List res=new ArrayList();
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
  
  void typecheck()
  {
  }
  
  /****************************************************************
   * Imperative type variables
   ****************************************************************/

  public boolean isImperative()
  {
    return false;
  }
  
  /****************************************************************
   * Functional types
   ****************************************************************/

  /** the list of input Monotypes if this type is functional */
  public Collection domain()
  {
    return null;
  }

  /** the return type if this type is functional */
  public Monotype codomain()
  {
    return null;
  }

  public TypeConstructor getTC()
  {
    User.error("decomposeTC should not be called in class "+getClass());
    return null;
  }
  
  public TypeParameters getTP()
  {
    User.error("decomposeTP should not be called in class "+getClass());
    return null;
  }
  
  abstract Monotype substitute(Map map);

  static List substitute(Map map, Collection c)
  {
    List res=new ArrayList(c.size());
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

  Constraint constraint=Constraint.True();
}

