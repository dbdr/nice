/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : TupleExp.java
// Created : Wed Aug 02 19:49:23 2000 by Daniel Bonniot
//$Modified: Fri Aug 04 12:00:52 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

import mlsub.typing.TupleType;
import mlsub.typing.Polytype;
import mlsub.typing.Constraint;

import gnu.bytecode.Type;
import gnu.bytecode.ArrayType;
import gnu.expr.*;
import gnu.expr.Expression;

/**
   Creation of a tuple.
   
   @author Daniel Bonniot
 */

public class TupleExp extends bossa.syntax.Expression
{
  public TupleExp(List expressions)
  {
    this.expressions = expChildren(expressions);
  }

  /****************************************************************
   * Typing
   ****************************************************************/

  boolean isAssignable()
  {
    for (Iterator i = expressions.iterator(); i.hasNext();)
      if (!((bossa.syntax.Expression) i.next()).isAssignable())
	return false;

    return true;
  }
  
  void computeType()
  {
    Polytype[] types = bossa.syntax.Expression.getType(expressions);
    // should create a new and method without the last dummy parameters
    Constraint cst = Constraint.and(Polytype.getConstraint(types), 
				    null, null);
    type = new Polytype(cst, new TupleType(Polytype.getMonotype(types)));
  }
  
  /****************************************************************
   * Code genaration
   ****************************************************************/

  protected gnu.expr.Expression compile()
  {
    int len = expressions.size();
    Type elementType = Type.pointer_type;
    
    // The array is not a special array
    
    Expression arrayVal = 
      new ApplyExp(new gnu.kawa.reflect.ArrayNew(elementType),
		   new Expression[]{intExp(len)});

    LetExp let = new LetExp(new Expression[]{arrayVal});
    let.outer = Statement.currentScopeExp;
    Declaration arrayDecl = let.addDeclaration("tuple", 
					       ArrayType.make(elementType));
    Expression array = new ReferenceExp(arrayDecl);
    
    Expression[] stmts = new Expression[1 + len];
    for (int i=0; i<len; i++)
      stmts[i] = 
	new ApplyExp(new gnu.kawa.reflect.ArraySet(elementType),
		     new Expression[]{
		       array, intExp(i),
		       ((bossa.syntax.Expression) expressions.get(i))
		       .generateCode()
		     });
    stmts[len] = array;
    
    let.setBody(new BeginExp(stmts));
    
    return let;
  }

  gnu.expr.Expression compileAssign(gnu.expr.Expression array)
  {
    int len = expressions.size();
    Type elementType = Type.pointer_type;

    LetExp let = null;
    Expression tupleExp;
    
    // if array is a complex expression, 
    // we have to evaluate it and store the result 
    // to avoid evaluating it several times.
    if (!(array instanceof ReferenceExp))
      {
	let = new LetExp(new Expression[]{array});
	let.outer = Statement.currentScopeExp;
	Declaration tupleDecl = let.addDeclaration
	  ("tupleRef", ArrayType.make(elementType));
	tupleExp = new ReferenceExp(tupleDecl);
      }
    else
      tupleExp = array;
    
    Expression[] stmts = new Expression[len];
    for (int i=0; i<len; i++)
      stmts[i] = ((bossa.syntax.Expression) expressions.get(i)).
	compileAssign(new ApplyExp(new gnu.kawa.reflect.ArrayGet(elementType),
				   new Expression[]{
				     tupleExp, 
				     intExp(i)}));
    
    if (let != null)
      {
	let.setBody(new BeginExp(stmts));
	return let;
      }
    else
      return new BeginExp(stmts);
  }

  private Expression intExp(int i)
  {
    return new QuoteExp(new Integer(i));
  }
  
  /****************************************************************
   * Misc.
   ****************************************************************/
  
  public String toString()
  {
    return Util.map("(", ", ", ")", expressions);
  }

  private List expressions;
}
