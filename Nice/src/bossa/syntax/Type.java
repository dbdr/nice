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
// Created : Mon Jul 19 17:45:27 1999 by bonniot
//$Modified: Thu Aug 26 10:30:38 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Any type. 
 * Can either be a Polytype or a PolytypeConstructor.
 */
public abstract class Type extends Node
{
  Type()
  {
    super(Node.down);
  }
  
  /**
   * Constructs a type of the correct kind
   *
   * @param typeParameters a collection of TypeSymbol
   * @param p the type body
   * @return the new type
   */
  public static Type newType(List typeParameters, Polytype p)
  {
    if(typeParameters==null || 
       typeParameters.size()==0) 
      return p; 
    return new PolytypeConstructor(typeParameters,p);
  }

  Type removeUnusefullTypeParameters()
  {
    return this;
  }

  /**
   * Returns an equivalent type, but with fresh binders.
   */
  abstract Type cloneType();
  
  //the get* methods are used to construct new Type. 
  //See FunExp for instance
  public abstract List getTypeParameters();
  public abstract Constraint getConstraint();
  public abstract Monotype   getMonotype();

  static Collection getConstraint(Collection c)
  {
    Collection res=new ArrayList(c.size());
    for(Iterator i=c.iterator();
	i.hasNext();)
      res.add(((Type)i.next()).getConstraint());
    return res;
  }
  
  static Collection getMonotype(Collection c)
  {
    Collection res=new ArrayList(c.size());
    for(Iterator i=c.iterator();
	i.hasNext();)
      res.add(((Type)i.next()).getMonotype());
    return res;
  }
  
  /** overrided in PolytypeConstructor */
  Polytype instantiate(TypeParameters typeParameters) 
    throws BadSizeEx
  {
    return null;
  }

  /****************************************************************
   * Functional types
   ****************************************************************/

  /** both methods are overrided in Polytype */

  Collection /* of Monotype */ domain()
  {
    return null;
  }

  Monotype codomain()
  {
    return null;
  }

}
