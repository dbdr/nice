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
//$Modified: Tue Jan 25 10:55:56 2000 by Daniel Bonniot $
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

  /**
   * @return true iff this method codes
   * the access to the field of a class.
   */
  final boolean isFieldAccess()
  {
    return getFieldAccessMethod()!=null;
  }

  /**
   * @return the FieldAccessMethod behind this expression, or null
   */
  FieldAccessMethod getFieldAccessMethod()
  {
    return null;
  }
  
  /**
   * Resolves overloading, taking into account the parameters the expressions is applied to.
   *
   * @return the resolved expression. Doesn't return if OR is not possible.
   */
  Expression resolveOverloading(List /* of Expression */ parameters)
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
   * just checks there is one alternative.
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
  static List getType(List expressions)
  {
    Iterator i=expressions.iterator();
    List res=new ArrayList(expressions.size());

    while(i.hasNext())
      res.add( ((Expression) i.next()) .getType());

    return res;
  }

  /**
   * Maps getType over a collection of Expressions.
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
	Polytype t=((Expression) i.next()).getType();
	res.add(t);
      }

    return res;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/
  
  /**
   * Creates the bytecode expression to evaluate this Expression.
   *
   * This must be overrided in any Expression, but not called directly. 
   * Call generateCode() instead.
   *
   * @see generateCode
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
  
  public static gnu.expr.Expression[] compile(List expressions)
  {
    gnu.expr.Expression[] res=new gnu.expr.Expression[expressions.size()];
    int n=0;
    for(Iterator i=expressions.iterator();
	i.hasNext();n++)
      {
	Expression exp = (Expression)i.next();
	res[n]=exp.generateCode();
      }

    return res;
  }
  
  gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    Internal.error(this,this+" doesn't know how to be modified, it is a "+this.getClass());
    return null;
  }
  
  /**
   * @return The declaration 
   */
  public Boolean declaration()
  {
    return null;
  }
  
  /****************************************************************
   * Locations
   ****************************************************************/

  public void setLocation(Location l)
  {
    loc=l;
  }

  public Location location()
  {
    return loc;
  }

  Location loc=Location.nowhere();
  protected Polytype type;
}
