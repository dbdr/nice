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
//$Modified: Fri Jul 23 20:23:29 1999 by bonniot $
// Description : A type with a constraint

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class Polytype extends Type
{
  public Polytype(Constraint cst, Monotype monotype)
  {
    this.constraint=cst;
    this.monotype=monotype;
  }

  /** Constructs a Polytype with the "True" constraint */
  public Polytype(Monotype monotype)
  {
    this(Constraint.True(),monotype);
  }

  Polytype clonePolytype()
  {
    // we don't clone the monotype, it is unnecessary
    // see PolytypeConstructor.instantiate
    return new Polytype(constraint.cloneConstraint(), monotype);
  }

  //Acces methods
  public Collection getTypeParameters()
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

  void buildScope(TypeScope ts)
  {
    typeScope=TypeScope.makeScope(ts,constraint.binders);
  }

  void resolve()
  {
    monotype=monotype.resolve(typeScope);
  }

  VarScope memberScope()
  {
    return monotype.memberScope();
  }

  Polytype substitute(Map map)
  {
    return new Polytype(constraint.substitute(map),monotype.substitute(map));
  }

  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    return constraint+" "+monotype.toStringExtern();
  }

  Constraint constraint;
  Monotype monotype;
}
