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
//$Modified: Mon Oct 02 17:04:56 2000 by Daniel Bonniot $
// Description : 

package bossa.syntax;

import java.util.*;
import bossa.util.*;

import mlsub.typing.Polytype;

public abstract class Expression extends Node 
  implements Located, Printable
{
  Expression()
  {
    super(Node.down);
  }

  /**
     Returns an equivalent expression with scoping resolved.
     Expressions that resolve to a new expression should override this method.
     Others (that resolve by side effects) should override Node.resolve().
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

  /** @return true iff this expression can be assigned a value. */
  boolean isAssignable()
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
   * @return the FieldAccessMethod behind this expression, or null
   */
  FieldAccessMethod getFieldAccessMethod()
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
  
  /**
   * Resolves overloading, taking into account the parameters 
   * the expressions is applied to.
   *
   * @return the resolved expression. Doesn't return if OR is not possible.
   */
  Expression resolveOverloading(Polytype[] parameters,
				CallExp callExp)
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
  Expression resolveOverloading(Polytype expectedType)
  {
    return this;
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
   * Iterates the resolveOverloading() method.
   */
  static List /* of Expression */ noOverloading(List expressions)
  {
    Iterator i=expressions.iterator();
    List res=new ArrayList(expressions.size());

    while(i.hasNext())
      res.add( ((Expression) i.next()) .noOverloading());

    return res;
  }
  
  /** computes the static type of the expression */
  abstract void computeType();

  final Polytype getType()
  {
    if(type==null)
      {
	computeType();
      }
    return type;
  }
  
  /**
   * Maps getType over a collection of Expressions
   *
   * @param Expressions the list of Expressions
   * @return the list of their PolyTypes
   */
  static Polytype[] getType(List expressions)
  {    
    Polytype[] res = new Polytype[expressions.size()];

    int n = 0;
    for(Iterator i=expressions.iterator(); i.hasNext();)
      res[n++] = ((Expression) i.next()).getType();

    return res;
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
  abstract protected gnu.expr.Expression compile();
  
  /**
   * Creates the bytecode expression to evaluate this Expression.
   */
  final gnu.expr.Expression generateCode()
  {
    gnu.expr.Expression res = compile();
    res.setLine(location().getLine(), location().getColumn());
    
    return res;
  }
  
  /**
   * Maps {@link #generateCode()} over a list of expressions.
   */
  public static gnu.expr.Expression[] compile(List expressions)
  {
    gnu.expr.Expression[] res = new gnu.expr.Expression[expressions.size()];
    int n = 0;
    for(Iterator i = expressions.iterator(); i.hasNext();n++)
      {
	Expression exp = (Expression)i.next();
	res[n] = exp.generateCode();
      }

    return res;
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
