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
   Compute an expression an forget the value.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/   
public class ExpressionStmt extends Statement
{
  public ExpressionStmt(Expression exp)
  {
    this.exp = exp;
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression generateCode()
  {
    return exp.generateCode();
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return exp.toString();
  }

  Expression exp;
}
