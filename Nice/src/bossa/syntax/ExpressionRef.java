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

// File    : ExpressionRef.java
// Created : Tue Aug 17 16:04:40 1999 by bonniot
//$Modified: Sat Dec 04 14:09:56 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Reference to an expression
 * Usefull for scoping resolution :
 * the reference remains constant, while the expression might change
 * 
 * @author bonniot
 */

// It set this class final since it may be used intensively
// so this will reduce the overhead,
// and I don't see why it should be subclassed

final public class ExpressionRef extends Expression
{
  public ExpressionRef(Expression e)
  {
    if(e==null)
      Internal.error("Null expression in a new ExpressionRef");
    content=e;
    addChild(content);
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  void resolve()
  {
    removeChild(content);
    content=content.resolveExp();
    if(content==null)
      User.error(this,
		 this+" was not declared");
  }
  
  void typecheck()
  {
    content.typecheck();
  }
  
  void computeType()
  {
    type=content.getType();
  }
  
  boolean isAssignable()
  {
    return content.isAssignable();
  }
  
  FieldAccessMethod getFieldAccessMethod()
  {
    return content.getFieldAccessMethod();
  }
  
  Expression resolveOverloading(List /* of Expression */ parameters)
  {
    content=content.resolveOverloading(parameters);
    return this;
  }
  
  Expression resolveOverloading(Polytype expectedType)
  {
    content=content.resolveOverloading(expectedType);
    return this;
  }
  
  Expression noOverloading()
  {
    content=content.noOverloading();
    return this;
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    return content.compile();
  }

  gnu.expr.Expression compileAssign(Expression value)
  {
    return content.compileAssign(value);
  }
  
  /****************************************************************
   * Locations
   ****************************************************************/

  public void setLocation(Location l)
  {
    content.setLocation(l);
  }

  public Location location()
  {
    return content.location();
  }

  public String toString()
  {
    return content.toString();
  }

  private Expression content;
}
