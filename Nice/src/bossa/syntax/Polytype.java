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

// File    : Polytype.java
// Created : Tue Jul 13 12:51:38 1999 by bonniot
//$Modified: Tue Aug 24 16:22:12 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A type with a constraint
 */
public class Polytype extends Type
{
  public Polytype(Constraint cst, Monotype monotype)
  {
    this.constraint=cst;
    this.monotype=monotype;
    addChild(cst);
  }

  /** Constructs a Polytype with the "True" constraint */
  public Polytype(Monotype monotype)
  {
    this(Constraint.True(),monotype);
  }

  boolean allImperative(List symbols)
  {
    return monotype.allImperative(symbols);
  }
  
  static Polytype bottom()
  {
    MonotypeVar alpha=Monotype.fresh(new LocatedString("alpha",
						       Location.nowhere()),
				     null);
    return new Polytype
      (new Constraint(alpha,null),
       alpha);
  }

  static Polytype voidType(TypeScope typeScope)
  {
    Monotype m=new MonotypeConstructor(new TypeConstructor(new LocatedString("void",Location.nowhere())),null,Location.nowhere());
    m.resolve(typeScope);
    
    return new Polytype(m);
  }
  
  static Collection fromMonotypes(Collection monotypes)
  {
    Collection res=new ArrayList(monotypes.size());
    for(Iterator i=monotypes.iterator();
	i.hasNext();)
      res.add(new Polytype(Constraint.True(),(Monotype)i.next()));
    return res;
  }
  
  Polytype clonePolytype()
  {
    // we don't clone the monotype, it is unnecessary
    // see PolytypeConstructor.instantiate
    return new Polytype(constraint.cloneConstraint(), monotype);
  }

  //Acces methods
  public List getTypeParameters()
  {
    return new ArrayList(0);
  }

  public Constraint getConstraint()
  {
    return constraint;
  }

  public Monotype getMonotype()
  {
    return monotype;
  }

  /****************************************************************
   * Functional types
   ****************************************************************/

  Collection /* of Monotype */ domain()
  {
    return monotype.domain();
  }

  Monotype codomain()
  {
    return monotype.codomain();
  }

  /*******************************************************************
   * Scoping
   *******************************************************************/

  void resolve()
  {
    monotype=monotype.resolve(typeScope);
  }
  
  Polytype substitute(Map map)
  {
    return new Polytype(constraint.substitute(map),monotype.substitute(map));
  }

  void typecheck()
  {
    monotype.typecheck();
  }
  
  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    return constraint+" "+monotype.toStringExtern();
  }

  private Constraint constraint;
  private Monotype monotype;
}
