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

// File    : IfExp.java
// Created : Mon Dec 06 12:01:51 1999 by bonniot
//$Modified: Tue Jun 06 15:00:11 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

import mlsub.typing.*;
import mlsub.typing.Polytype;

/**
 * Conditional expression (and statement).
 * 
 * @author bonniot
 */

public class IfExp extends Expression
{
  public IfExp(Expression condition, Expression thenExp, Expression elseExp)
  {
    if(elseExp==null)
      // Then without else
      elseExp = new VoidConstantExp();
    
    this.condition = expChild(condition);
    this.thenExp = expChild(thenExp);
    this.elseExp = expChild(elseExp);
  }

  /****************************************************************
   * Typing
   ****************************************************************/

  void computeType()
  {
    this.type = Polytype.union(thenExp.getType(),elseExp.getType());
  }
  
  void typecheck()
  {
    condition.resolveOverloading(ConstantExp.boolPolytype);
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    gnu.expr.Expression thenCode, elseCode;
    thenCode = thenExp.generateCode();
    elseCode = elseExp.generateCode();
    
    if(elseCode.getType()==bossa.SpecialTypes.voidType)
      thenCode = voidify(thenCode);
    else if(thenCode.getType()==bossa.SpecialTypes.voidType)
      elseCode = voidify(elseCode);
    
    return new gnu.expr.IfExp(condition.generateCode(),
			      thenCode,
			      elseCode);
  }
  
  private static gnu.expr.Expression voidify(gnu.expr.Expression e)
  {
    if(e.getType()==bossa.SpecialTypes.voidType)
      return e;
    
    return new gnu.expr.BeginExp(e, gnu.expr.QuoteExp.voidExp);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return 
      "if(" +
      condition +
      ")\n" +
      thenExp +
      "\nelse\n" +
      elseExp +
      "\nendif"
      ;
  }

  private Expression condition,thenExp, elseExp;
}
