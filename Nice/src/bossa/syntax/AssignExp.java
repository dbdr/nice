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

// File    : AssignExp.java
// Created : Mon Jul 05 15:49:27 1999 by bonniot
//$Modified: Tue Mar 14 14:09:18 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.parser.ParseException;
import bossa.typing.*;

/**
 * Assignment.
 */
public class AssignExp extends Expression
{
  public AssignExp(Expression to,
		   Expression value)
  {
    this.to=expChild(to);
    this.value=expChild(value);
  }

  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  static void checkAssignment(Polytype left, ExpressionRef right)
    throws TypingEx
  {
    right.resolveOverloading(left);
    Typing.leq(right.getType(),left);
  }
  
  void typecheck()
  {
    to=to.noOverloading();

    if(!to.isAssignable())
      User.error(this,to+" cannot be assigned a value");

    try{
      checkAssignment(to.getType(),value);
    }
    catch(TypingEx t){
      User.error(this,
		 "Typing error : "+to+
		 " cannot be assigned value "+value+
		 "\nof type "+value.getType(),
		 "\n"+t.getMessage());
    }
  }

  void computeType()
  {
    this.type = value.getType();
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    to=to.noOverloading();

    gnu.expr.Expression[] val = new gnu.expr.Expression[1];    
    val[0] = value.generateCode();    
    gnu.expr.LetExp let = new gnu.expr.LetExp(val);
    let.outer=Statement.currentScopeExp;
    
    gnu.expr.Declaration tmp = let.addDeclaration("tmp",val[0].getType());
    tmp.setCanRead(true);    
    gnu.expr.Expression tmpExp = new gnu.expr.ReferenceExp(tmp);

    gnu.expr.Expression[] exps = new gnu.expr.Expression[2];
    exps[0] = to.compileAssign(tmpExp);
    exps[0].setLine(to.location().getLine());
    exps[1] = tmpExp;
    
    let.setBody(new gnu.expr.BeginExp(exps));
    return let;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return to+" = "+value;
  }

  private Expression to;
  private ExpressionRef value;
}
