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

import bossa.util.*;

import mlsub.typing.*;
import mlsub.typing.Polytype;
import nice.tools.code.SpecialTypes;

/**
   Conditional expression (used in statements too).
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class IfExp extends Expression
{
  public IfExp(Expression condition, Expression thenExp, Expression elseExp)
  {
    if(elseExp==null)
      // Then without else
      elseExp = new VoidConstantExp();
    
    this.condition = condition;
    this.thenExp = thenExp;
    this.elseExp = elseExp;
  }

  /****************************************************************
   * Typing
   ****************************************************************/

  void computeType()
  {
    this.type = Polytype.union(thenExp.getType(), elseExp.getType());
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    gnu.expr.Expression thenCode, elseCode;
    thenCode = thenExp.generateCode();
    elseCode = elseExp.generateCode();
    
    if (elseCode.getType() == SpecialTypes.voidType)
      thenCode = voidify(thenCode);
    else if (thenCode.getType() == SpecialTypes.voidType)
      elseCode = voidify(elseCode);
    
    return new gnu.expr.SimpleIfExp(condition.generateCode(),
			      thenCode,
			      elseCode);
  }
  
  private static gnu.expr.Expression voidify(gnu.expr.Expression e)
  {
    if (e.getType().isVoid())
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
      String.valueOf(condition) +
      ")\n" +
      String.valueOf(thenExp) +
      "\nelse\n" +
      String.valueOf(elseExp) +
      "\nendif"
      ;
  }

  Expression condition,thenExp, elseExp;

  /** set to true if the execution of the branch never completes. */
  boolean thenUnreachable, elseUnreachable;
}
