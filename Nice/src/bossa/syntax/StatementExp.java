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

// File    : StatementExp.java
// Created : Tue Jan 25 17:00:28 2000 by Daniel Bonniot
//$Modified: Tue Jun 06 13:59:18 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * A statement made by the evaluation of an expression.
 * 
 * @author Daniel Bonniot
 */

public class StatementExp extends Expression
{
  public StatementExp(Statement s)
  {
    this.statement = s;
    addChild(this.statement);
  }

  public String toString()
  {
    return statement.toString();
  }

  public gnu.expr.Expression compile()
  {
    return new gnu.expr.BeginExp
      (statement.generateCode(), gnu.expr.QuoteExp.voidExp);
  }
  
  void computeType()
  {
    type = ConstantExp.voidPolytype;
  }
  
  private Statement statement;
}
