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
   Terminate abruptly the nearest enclosing loop.

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

public class BreakStmt extends Statement
{
  public static BreakStmt instance = new BreakStmt();

  private BreakStmt() {}

  /****************************************************************
   * Code generation
   ****************************************************************/

  gnu.expr.Expression generateCode()
  {
    return new gnu.expr.ExitExp(LoopStmt.currentLoopBlock);
  }
}
