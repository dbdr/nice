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

// File    : AssignStmt.java
// Created : Mon Jul 05 15:49:27 1999 by bonniot
//$Modified: Sat Dec 04 14:17:36 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.parser.ParseException;
import bossa.typing.*;

/**
 * Assignment.
 */
public class AssignStmt extends Statement
{
  public AssignStmt(Expression to,
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
      User.error(this,"Typing error : "+to+
		 " cannot be assigned value "+value);
    }
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression generateCode()
  {
    return to.compileAssign(value);
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
