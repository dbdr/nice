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

/**
   A statement made by the evaluation of an expression.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class StatementExp extends Expression
{
  public StatementExp(Statement s)
  {
    this.statement = s;
  }

  public String toString()
  {
    return String.valueOf(statement);
  }

  public gnu.expr.Expression compile()
  {
    if (statement == null)
      return gnu.expr.QuoteExp.voidExp;
    else
      return new gnu.expr.BeginExp
	(statement.generateCode(), gnu.expr.QuoteExp.voidExp);
  }
  
  void computeType()
  {
    type = PrimitiveType.voidPolytype;
  }
  
  Statement statement;
}
