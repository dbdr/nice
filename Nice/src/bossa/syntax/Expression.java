/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import java.util.*;
import bossa.util.*;

import mlsub.typing.Polytype;
import mlsub.typing.Monotype;

/**
   An expression of the Nice language.

   Subclasses of Expression have the 'Exp' suffix in their name.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public abstract class Expression
  implements Located, Printable
{
  /** @return true iff this expression can be assigned a value. */
  public boolean isAssignable()
  {
    return false;
  }

  boolean isZero()
  {
    return false;
  }

  boolean isFalse()
  {
    return false;
  }

  boolean isTrue()
  {
    return false;
  }

  public boolean isNull()
  {
    return false;
  }

  /**
   * Resolve overloading, assuming that this expression
   * should have some Type.
   *
   * @param expectedType the type this expression should have.
   * @return the resolved expression. Doesn't return if OR is not possible.
   */
  Expression resolveOverloading(Polytype expectedType)
  {
    // Default implementation: do not consider the expected type.
    return noOverloading();
  }
  
  /**
   * No overloading information is known,
   * just checks there is only one alternative.
   *
   * @return the resolved expression. Doesn't return if OR is not possible.
   */
  Expression noOverloading()
  {
    return this;
  }

  /**
     Called with the type that this expression is expected to have.
     This information can be used to generate better code.
  */
  void adjustToExpectedType(Monotype expectedType)
  {
    // Default: do nothing.
  }

  static void adjustToExpectedType (Expression[] expressions, Monotype[] types)
  {    
    // The domain of a function can be null in rare circumstances, like
    // for a function of type <T> T
    if (types == null)
      return;

    for (int i = 0; i < types.length; i++)
      expressions[i].adjustToExpectedType(types[i]);
  }

  /** computes the static type of the expression */
  abstract void computeType();

  public Polytype getType()
  {
    if(type==null)
      {
	computeType();
      }
    if (type == null)
      Debug.println(this + "(" + this.getClass() + ") has null type");
    
    return type;
  }

  /**
   * Maps getType over a collection of Expressions
   *
   * @param Expressions the list of Expressions
   * @return the list of their PolyTypes
   */
  static Polytype[] getType(Expression[] expressions)
  {    
    Polytype[] res = new Polytype[expressions.length];

    for (int i = 0; i< expressions.length; i++)
      {
        //why has this different behavior compared to the previous method???     
	expressions[i] = expressions[i].noOverloading();
	res[i] = expressions[i].getType();
      }

    return res;
  }
  
  Polytype inferredReturnType()
  {
    Internal.error("inferredReturnType called in " + getClass());
    return null;
  }

  void checkSpecialRequirements(Expression[] arguments)
  {
    // Do nothing by default.
  }

  public void setLocation(Location l)
  {
    location = l;
  }

  public final Location location()
  {
    return location;
  }

  // from interface bossa.util.Printable
  public String toString(int param)
  {
    // default implementation
    return toString();
  }

  private Location location = Location.nowhere();
  protected Polytype type;
}
