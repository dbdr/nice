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
   A synchronized block.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class SynchronizedStmt extends Statement
{
  public SynchronizedStmt(Expression object, Statement body)
  {
    this.object = object;
    this.body = body;
  }

  public gnu.expr.Expression generateCode()
  {
    /* Make sure that the synchronized statement is compiled
       with a void target by embedding it in a BeginExp. 
    */
    return new gnu.expr.BeginExp
      (new gnu.expr.SynchronizedExp(object.generateCode(), 
				    body.generateCode()),
       gnu.expr.QuoteExp.voidExp);
  }

  public String toString()
  {
    return "synchronized(" + object + ")" + body;
  }

  Expression object;
  Statement body;
}
