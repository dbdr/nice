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
   Continue a loop with its next iteration.

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

public class ContinueStmt extends Statement
{
  public static ContinueStmt make(LocatedString label)
  {
    return new ContinueStmt(label);
  }

  private ContinueStmt(LocatedString label)
  {
    this.label = label;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  gnu.expr.Expression generateCode()
  {
    return loop.code.new ContinueExp();
  }

  LocatedString label;
  LoopStmt loop;
}
