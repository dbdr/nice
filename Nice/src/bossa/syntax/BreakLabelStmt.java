/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

/**
   Terminate abruptly an enclosing statement.

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

public class BreakLabelStmt extends Statement
{
  public BreakLabelStmt(LocatedString label)
  {
    this.label = label;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  gnu.expr.Expression generateCode()
  {
    return new gnu.expr.ExitExp(statement.block);
  }

  LocatedString label;
  LabeledStmt statement;
}
