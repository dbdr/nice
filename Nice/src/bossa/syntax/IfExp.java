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
//$Modified: Mon Dec 06 15:20:30 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

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
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    condition.noOverloading();
    return new gnu.expr.IfExp(condition.compile(),thenExp.compile(),elseExp.compile());
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
      "endif"
      ;
  }

  private Expression condition,thenExp, elseExp;
}
