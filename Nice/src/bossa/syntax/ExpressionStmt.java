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

// File    : ExpressionStmt.java
// Created : Mon Jul 05 17:23:36 1999 by bonniot
//$Modified: Mon Dec 06 15:39:48 1999 by bonniot $
// Description : Compute an expression an forget the value

package bossa.syntax;

import bossa.util.*;

public class ExpressionStmt extends Statement
{
  public ExpressionStmt(Expression exp)
  {
    this.exp=exp;
    addChild(exp);
  }
  
  void resolve()
  {
    removeChild(exp);
    exp = exp.resolveExp();
  }
  
  void typecheck()
  {
    exp = exp.noOverloading();
    
    exp.doTypecheck();
    
    // To force the typechking of the expression,
    // if it is done while computing its type
    // and it has not yet been done
    exp.getType();
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression generateCode()
  {
    return exp.compile();
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return exp.toString();
  }

  public Expression exp;
}
