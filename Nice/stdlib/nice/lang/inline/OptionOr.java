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

package nice.lang.inline;

import gnu.mapping.Procedure2;
import gnu.expr.*;
import gnu.bytecode.*;
import nice.tools.code.EnsureTypeProc;

/**
   e1 || e2
   Evaluate e1, and return it if it is not null (without evaluating e2).
   Otherwise evaluate e2 and return it.

   @author Daniel Bonniot
*/
public class OptionOr extends Procedure2 implements Inlineable
{
  public static OptionOr create(String param)
  {
    return instance;
  }

  private static OptionOr instance = new OptionOr();

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    // We cannot use target for the first argument, since its value maybe
    // be null, while the target may not.
    args[0].compile(comp, Target.pushObject);
    code.emitDup();
    code.emitIfNotNull();
    target.compileFromStack(comp, code.topType());
    code.emitElse();
    code.emitPop(1);
    args[1].compile(comp, target);
    code.emitFi();
  }

  public Type getReturnType (Expression[] args)
  {
    return Type.pointer_type;
  }

  // Interpretation

  public Object apply2 (Object arg1, Object arg2)
  {
    throw new Error("Not implemented");
  }
}
