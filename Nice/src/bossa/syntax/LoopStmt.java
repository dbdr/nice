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
  public static LoopStmt forLoop
    (Expression test, Statement update, Statement body)
  {
    return new LoopStmt(test, body, update, true);
  }

  public static LoopStmt whileLoop(Expression test, Statement body)
  {
    return new LoopStmt(test, body, null, true);
  }

  public static LoopStmt doLoop(Expression test, Statement body)
  {
    return new LoopStmt(test, body, null, false);
  }

  public static Statement forInLoop(Monotype vartype, LocatedString var, Location loc, Expression container, Statement body)
  {
    Monotype itertype;
    LocatedString iter;
    Expression getiter,iterexp,cond,getvar;
    Statement loop,init,assign;

    List tparams = new ArrayList(1);
    tparams.add(vartype);	
    itertype = new MonotypeConstructor(new TypeIdent(new LocatedString("ForInIterator", loc)),
			new TypeParameters(tparams), loc);
    itertype.nullness = Monotype.sure;
    getiter = CallExp.create(new IdentExp(new LocatedString("forInIterator", loc)), container); 
    iter = new LocatedString("for_in_iter", loc);
    init = new Block.LocalVariable(iter, itertype, true, getiter);
    iterexp = new IdentExp(iter);
    cond = CallExp.create(new IdentExp(new LocatedString("next", loc)), iterexp);
    getvar = CallExp.create(new IdentExp(new LocatedString("current", loc)), iterexp);
    assign = new Block.LocalVariable(var, vartype, false, getvar);
    List loopbody = new LinkedList();
    loopbody.add(assign);
    loopbody.add(body);
    loop = LoopStmt.whileLoop(cond, new Block(loopbody));
    List l = new LinkedList();
    l.add(init);
    l.add(loop);
    return new Block(l);
  
  }

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
  private LoopStmt(Expression whileExp,
		   Statement loopBody,
		   Statement iterationStatements,
		   boolean testFirst)
  {
    this.whileExp = whileExp;
    this.loopBody = loopBody;
    this.iterationStatements = iterationStatements;
    this.testFirst = testFirst;
  }

  boolean isTestFirst() { return testFirst; }

  /****************************************************************
   * Code generation
   ****************************************************************/

  gnu.expr.LoopExp code;
  
  static gnu.expr.BlockExp currentLoopBlock;

  void createBlock()
  {
    mustCreateBlock = true;
  }

  /**
     Returns true iff there exists a break statement that target this loop.
     This means that the loop can complete abruptly because of that break.
  */
  boolean isBreakTarget() { return mustCreateBlock; }

  private boolean mustCreateBlock = false;

  gnu.expr.Expression generateCode()
  {
    gnu.expr.Expression test, iteration, res;

    if (whileExp == null)
      test = new gnu.expr.QuoteExp(Boolean.TRUE);
    else
      test = whileExp.generateCode();
    
    if (iterationStatements == null)
      iteration = gnu.expr.QuoteExp.voidExp;
    else
      iteration = iterationStatements.generateCode();

    code = new gnu.expr.LoopExp(test, iteration, testFirst);

    gnu.expr.BlockExp savedBlock = currentLoopBlock;
    if (mustCreateBlock)
      res = currentLoopBlock = new gnu.expr.BlockExp(code);
    else
      res = code;

    code.setBody(loopBody != null ? loopBody.generateCode() : null);

    code = null;
    currentLoopBlock = savedBlock;
    return res;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    return 
      "for(;" + whileExp + "; " + iterationStatements+ ")\n" + 
      (loopBody == null ? ";" : loopBody.toString());
  }

  /****************************************************************
   * Fields
   ****************************************************************/

  Expression whileExp;
  Statement loopBody, iterationStatements;
  private boolean testFirst;
}
