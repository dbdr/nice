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

import java.util.*;

/**
   General loop statement (used for 'for', 'while do', 'do while' ...)

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class LoopStmt extends Statement
{
  /**
   * Create a loop statement.
   *
   * @param whileExp a boolean condition expressing wether 
       the loop should continue.
   * @param loopBody the body of the loop.
   * @param iterationStatement a Statement that will be executed at the end
       of the body.
   * @param testAtTheEnd wether the test should be done before or after
       the first execution of the body.
   */
  public LoopStmt(Expression whileExp,
		  Statement loopBody,
		  Statement iterationStatements,
		  boolean testAtTheEnd)
  {
    this.whileExp = whileExp;
    this.loopBody = loopBody;
    this.iterationStatements = iterationStatements;

    if(testAtTheEnd)
      Internal.error("\"do\" not implemented");
  }

  public LoopStmt(Expression whileExp,
		  Statement body)
  {
    this(whileExp, body, null, false);
  }

  public LoopStmt(Expression whileExp,
		  Statement body, boolean testAtTheEnd)
  {
    this(whileExp, body, null, testAtTheEnd);
  }

  public LoopStmt(Expression whileExp,
		  Statement body,
		  Statement iterationStatements)
  {
    this(whileExp, body, iterationStatements, false);
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  gnu.expr.Expression generateCode()
  {
    gnu.expr.Expression test,iteration;
    if(whileExp==null)
      test = new gnu.expr.QuoteExp(Boolean.TRUE);
    else
      test = whileExp.compile();
    
    if(iterationStatements==null)
      iteration = gnu.expr.QuoteExp.voidExp;
    else
      iteration = iterationStatements.generateCode();
    
    return new gnu.expr.LoopExp
      (test, 
       loopBody != null ? loopBody.generateCode() : null, 
       iteration);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    return 
      "for(;" + whileExp + "; " + iterationStatements+ ")\n" + 
      loopBody == null ? ";" : loopBody.toString();
  }

  /****************************************************************
   * Fields
   ****************************************************************/

  Expression whileExp;
  Statement loopBody, iterationStatements;
}
