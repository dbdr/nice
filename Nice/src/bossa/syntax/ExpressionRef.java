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
//$Modified: Fri Jul 21 15:33:48 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

import mlsub.typing.Polytype;

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
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  Scopes buildScope(VarScope outer, TypeScope typeOuter)
  {
    return content.buildScope(outer, typeOuter);
  }

  void findJavaClasses() 
  {
    // call fJC on the children of content too
    content.doFindJavaClasses();
  }

  void resolve()
  {
    content = content.resolveExp();
    if(content == null)
      User.error(this, this+" was not declared");
  }
  
  void typecheck()
  {
    // call doTypecheck to call typecheck on the childs of content also
    content.doTypecheck();

    // To force the typechking of the expression,
    // if it is done while computing its type
    // and it has not yet been done
    //content.getType();
  }
  
  void computeType()
  {
    // if we want to know the type,
    // there must not be overloading ambiguity
    
    // XXX we could remove some noOverloading here and there now
    content=content.noOverloading();

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
  
  gnu.bytecode.ClassType staticClass()
  {
    return content.staticClass();
  }  

  Expression resolveOverloading(Polytype[] parameters,
				CallExp callExp)
  {
    content = content.resolveOverloading(parameters, callExp);
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
    content = content.noOverloading();
    return content.compile();
  }

  /** @return the declaration of the variable denoted by this expression,
      or <code>null</code> if this expression is not a variable.
  */
  gnu.expr.Declaration getDeclaration()
  {
    return content.getDeclaration();
  }
  
  gnu.expr.Expression compileAssign(gnu.expr.Expression value)
  {
    return content.compileAssign(value);
  }
  
  /****************************************************************
   * Locations
   ****************************************************************/

  public void setLocation(Location l)
  {
    super.setLocation(l);
    content.setLocation(l);
  }

  public String toString()
  {
    return content.toString();
  }

  Expression content()
  {
    return content;
  }
  
  private Expression content;
}
