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
    Target stack = Target.pushObject;

    args[0].compile(comp, stack);
    code.emitDup();
    code.emitIfNull();
    code.emitPop(1);
    args[1].compile(comp, stack);
    code.emitElse();
    code.emitFi();
    target.compileFromStack(comp, Type.pointer_type);
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
