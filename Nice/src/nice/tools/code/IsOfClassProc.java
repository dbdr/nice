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

public class IsOfClassProc extends Procedure1 implements Inlineable
{
  public IsOfClassProc(Type type, boolean possiblyNull)
  {
    this.type = type;
    this.possiblyNull = possiblyNull;
  }

  private Type type;
  private boolean possiblyNull;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    args[0].compile(comp, Target.pushObject);

    if (possiblyNull)
      {
        //first a check is needed for the case that the argument is null
        code.emitDup();
        code.emitIfNotNull();
      }

    code.emitInvokeVirtual(getClassMethod);
    code.emitInvokeVirtual(getNameMethod);
    code.emitPushString(type.getName());
    code.emitInvokeVirtual(equalsMethod);

    if (possiblyNull)
      {
        code.emitElse();
        code.emitPop(1);
        code.emitPushBoolean(false);
        code.emitFi();
      }

    target.compileFromStack(comp, Type.boolean_type);
  }

  private static final Method getClassMethod = 
    Type.pointer_type.getDeclaredMethod("getClass", 0);

  private static final Method getNameMethod = 
    ClassType.make("java.lang.Class").getDeclaredMethod("getName", 0);

  private static final Method equalsMethod = 
    Type.pointer_type.getDeclaredMethod("equals", 1);

  public Type getReturnType (Expression[] args)
  {
    return Type.boolean_type;
  }

  public Object apply1 (Object arg)
  {
    throw new Error("Not implemented");
  }
}
