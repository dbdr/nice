/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 2000                             */
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
   Inlining of native reference operators.
*/
public class ReferenceOp extends Procedure2 implements Inlineable, Branchable
{
  private final static int
    Eq = 1,
    Ne = 2;

  public static ReferenceOp create(String param)
  {
    int kind = 0;
    if ("==".equals(param))
      kind = Eq;
    else if ("!=".equals(param))
      kind = Ne;
    else
      bossa.util.User.error("Unknown inlined boolean operator " + param);
    return new ReferenceOp(kind);
  }

  private ReferenceOp (int kind)
  {
    this.kind = kind;
  }

  private final int kind;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.pointer_type);
    Label _else = new Label(code);
    Label _end = new Label(code);

    args[0].compile(comp, stack);
    args[1].compile(comp, stack);

    if (kind == Eq)
      code.emitGotoIfNE(_else);
    else
      code.emitGotoIfEq(_else);

    code.emitPushBoolean(true);
    code.emitGoto(_end);
    code.popType(); //simulate 'else' otherwise gnu.bytecode don't like it
    _else.define(code);
    code.emitPushBoolean(false);
    _end.define(code);
        
    target.compileFromStack(comp, retType);
  }

  public void compileJump (Compilation comp, Expression[] args, Label to)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.pointer_type);

    args[0].compile(comp, stack);
    args[1].compile(comp, stack);

    if (kind == Eq)
      code.emitGotoIfEq(to);
    else
      code.emitGotoIfNE(to);
  }

  public void compileJumpNot (Compilation comp, Expression[] args, Label to)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.pointer_type);

    args[0].compile(comp, stack);
    args[1].compile(comp, stack);

    if (kind == Eq)
      code.emitGotoIfNE(to);
    else
      code.emitGotoIfEq(to);
  }

  private static final Type retType = Type.boolean_type;
  
  public Type getReturnType (Expression[] args)
  {
    return retType;
  }

  // Interpretation

  public Object apply2 (Object arg1, Object arg2)
  {
    throw new Error("Not implemented");
  }
}