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

// File    : LeqCst.java
// Created : Mon Jul 19 16:42:14 1999 by bonniot
//$Modified: Mon Jul 26 15:08:00 1999 by bonniot $
// Description : Inequality between type constructors

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Inequality constraint.
 * Should not live after scoping, where we determine
 * if the constraint is on monotypes or on type constructors
 *
 * @see MonotypeLeqCst
 * @see TypeConstructorLeqCst
 */
public class LeqCst extends AtomicConstraint
{
  /**
   * The constraint t1 <: t2
   *
   */
  public LeqCst(TypeIdent t1, TypeIdent t2)
  {
    this.t1=t1;
    this.t2=t2;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  AtomicConstraint resolve(TypeScope scope)
  {
    TypeSymbol s1=t1.resolve(scope);
    TypeSymbol s2=t2.resolve(scope);
    if(s1 instanceof TypeConstructor && s2 instanceof TypeConstructor)
      return new TypeConstructorLeqCst((TypeConstructor) s1,
				       (TypeConstructor) s2);
    else if(s1 instanceof Monotype && s2 instanceof Monotype)
      return new MonotypeLeqCst((Monotype) s1,
				(Monotype) s2);


    User.error(t1,"Constraint "+this+" is not well kinded");
    return null;
  }

  AtomicConstraint substitute(Map map)
  {
    if(map.containsKey(t1.getName()) || map.containsKey(t2.getName()))
      {
	Monotype m1,m2;
	try{
	  m1=(Monotype)map.get(t1.getName());
	  m2=(Monotype)map.get(t2.getName());
	  return new MonotypeLeqCst(m1,m2);
	}
	catch(ClassCastException e) {
	  User.error(t1+" and "+t2+" cannot be compared");
	}
      }
    return this;
  }

  public String toString()
  {
    return t1+"<<"+t2;
  }

  TypeIdent t1,t2;
}
