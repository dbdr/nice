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
   A statement anotated by a label (used by break and continue).

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

public class LabeledStmt extends Statement
{
  public LabeledStmt(LocatedString label, Statement statement)
  {
    this.label = label;
    this.statement = statement;
  }

  /** @param loop the loop this label targets, if different from statement. */
  public LabeledStmt(LocatedString label, Statement statement, LoopStmt loop)
  {
    this(label, statement);
    this.loop = loop;
  }

  private LocatedString label;
  private Statement statement;
  private LoopStmt loop;

  public String name() { return label.toString(); }
  public LocatedString getLabel() { return label; }
  public Statement getStatement() { return statement; }

  /** @return the loop targeted by this label, or null. */
  LoopStmt getLoop() 
  { 
    if (loop != null) 
      return loop; 
    if (statement instanceof LoopStmt)
      return (LoopStmt) statement;
    return null;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  gnu.expr.BlockExp block;
  
  public gnu.expr.Expression generateCode()
  {
    if (statement == null)
      return gnu.expr.QuoteExp.voidExp;

    gnu.expr.BlockExp res = block = new gnu.expr.BlockExp();
    block.setBody(statement.generateCode());
    block = null;
    return res;
  } 
}
