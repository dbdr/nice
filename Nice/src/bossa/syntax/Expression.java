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
  static final Expression[] toArray(List expressions)
  {
    if (expressions == null || expressions.size() == 0)
      return noExpressions;

    return (Expression[]) 
	expressions.toArray(new Expression[expressions.size()]);
  }
  
  public static final Expression[] noExpressions = new Expression[0];

  /** @return true iff this expression can be assigned a value. */
  public boolean isAssignable()
  {
    return false;
  }

  /**
     @return true iff this expression is a method 
     to access to the field of a class.
   */
  final boolean isFieldAccess()
  {
    return getFieldAccessMethod() != null;
  }

  /**
   * @return the FieldAccess behind this expression, or null
   */
  /*FieldAccess*/Object getFieldAccessMethod()
  {
    return null;
  }  

  /**
     @return the FieldAccess if this expression resolves to a field, 
     which is true if it is the application a of FieldAccess to an object 
     value. Returns null otherwise.
   */
  /*FieldAccess*/Object getField()
  {
    return null;
  }  

  /** 
      @return null, or the underlying java class if this
      expression is a constant class (used in static method calls).
  */
  gnu.bytecode.ClassType staticClass()
  {
    return null;
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

  /****************************************************************
   * Code generation
   ****************************************************************/
  
  /**
   * Creates the bytecode expression to evaluate this Expression.
   *
   * This must be overrided in any Expression, but not called directly. 
   * Call {@link #generateCode()} instead.
   */
  protected abstract gnu.expr.Expression compile();
  
  /**
   * Creates the bytecode expression to evaluate this Expression.
   */
  final gnu.expr.Expression generateCode()
  {
    gnu.expr.Expression res = compile();
    location().write(res);
    
    return res;
  }
  
  /**
     Creates the bytecode expression to evaluate this Expression,
     when it is used as a function that is immediately called.
   */
  gnu.expr.Expression generateCodeInCallPosition()
  {
    // Default implementation.
    return generateCode();
  }
  
  /**
   * Maps {@link #generateCode()} over an array of expressions.
   */
  public static gnu.expr.Expression[] compile(Expression[] expressions)
  {
    gnu.expr.Expression[] res = new gnu.expr.Expression[expressions.length];
    for (int i=0; i<res.length; i++)
      res[i] = expressions[i].generateCode();

    return res;
  }
  
  /** @return the declaration of the local variable denoted by this expression,
      or <code>null</code> if this expression is not a local variable.
  */
  gnu.expr.Declaration getDeclaration()
  {
    return null;
  }
  
  gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  // default implementation using getDeclaration()
  {
    gnu.expr.Declaration decl = getDeclaration();
    if (decl != null)
      {
	gnu.expr.SetExp res = new gnu.expr.SetExp(decl, value);
	res.setHasValue(true);
	return res;
      }

    Internal.error(this, this + " doesn't know how to be modified, it is a " +
		   this.getClass());
    return null;
  }
  
  /****************************************************************
   * Locations
   ****************************************************************/

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
