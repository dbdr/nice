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

package nice.tools.code;

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

public class IsNullProc extends Procedure1 implements Inlineable
{
  private IsNullProc()
  {
  }

  public static IsNullProc instance = new IsNullProc();

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    args[0].compile(comp, Target.pushObject);
    code.emitIfNull();
    code.emitPushInt(1);
    code.emitElse();
    code.emitPushInt(0);
    code.emitFi();
    target.compileFromStack(comp, Type.boolean_type);
  }

  public Type getReturnType (Expression[] args)
  {
    return Type.boolean_type;
  }

  public Object apply1 (Object arg)
  {
    if (arg == null)
      return Boolean.TRUE;
    else
      return Boolean.FALSE;
  }
}
