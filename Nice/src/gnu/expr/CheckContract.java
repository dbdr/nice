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

package gnu.expr;

import gnu.bytecode.*;

/**
   Check that a precondition holds, and that the body
   leads to the postcondition to hold.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class CheckContract extends Expression
{
  public CheckContract (Expression[] pre, Expression[] post, Expression body)
  {
    this.pre = pre;
    this.post = post;
    this.body = body;
  }

  public static final Expression result = new ResultExp();

  private Expression[] pre, post;
  private Expression body;

  public void compile (Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    ClassExp currentClass = (ClassExp)comp.topLambda;
    code.preparePostcondition(currentClass.getAssertionEnabledField());

    for (int i = 0; i < pre.length; i++)
      pre[i].compileWithPosition(comp, Target.Ignore);

    body.compileWithPosition(comp, target);
    
    code.startPostcondition();
    for (int i = 0; i < post.length; i++)
      post[i].compileWithPosition(comp, Target.Ignore);
    
    code.endPostcondition();
  }

  public void print (gnu.mapping.OutPort ps)
  {
    ps.print("(Check ...)");
  }

  private static class ResultExp extends Expression
  {
    public void compile (Compilation comp, Target target)
    {
      CodeAttr code = comp.getCode();
      code.loadResult();
    }

    public void print (gnu.mapping.OutPort ps)
    {
      ps.print("(Result)");
    }
  }
}