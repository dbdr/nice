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

import gnu.expr.*;
import gnu.bytecode.*;

public class EqualsMethodProc extends gnu.mapping.Procedure2 implements Inlineable
{

  public EqualsMethodProc () {}

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    args[0].compile(comp, Target.pushObject);
    args[1].compile(comp, Target.pushObject);
    code.emitInvokeVirtual(equalsMethod);
    target.compileFromStack(comp, Type.boolean_type);
  }

  private static final Method equalsMethod = 
    Type.pointer_type.getDeclaredMethod("equals", 1);

  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return Type.boolean_type;
  }

  public Object apply2 (Object arg1, Object arg2)
  {
    throw new Error("Not implemented");
  }

}
