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
public class ReferenceOp extends Procedure2 implements Branchable, bossa.syntax.Macro
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
    compile(args, comp, target);
  }

  void compile (Expression[] args, Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    Target stack = Target.pushObject;
    Label _else = new Label(code);
    Label _end = new Label(code);

    boolean prim0 = args[0].getType() instanceof PrimType;
    boolean prim1 = args[1].getType() instanceof PrimType;

    if (prim1 && ! prim0)
      {
        compilePrimitive(false, args[0], args[1], comp);
        return;
      }
    else if (prim0 && ! prim1)
      {
        compilePrimitive(true,  args[1], args[0], comp);
        return;
      }
    else if (args[0] instanceof QuoteExp &&
             ((QuoteExp)args[0]).getType() == Type.nullType)
    {
      args[1].compile(comp, stack);
      if (kind == Eq)
	code.emitGotoIfNotNull(_else);
      else
	code.emitGotoIfNull(_else);
    }
    else if (args[1] instanceof QuoteExp &&
       ((QuoteExp)args[1]).getType() == Type.nullType)
    {
      args[0].compile(comp, stack);
      if (kind == Eq)
	code.emitGotoIfNotNull(_else);
      else
	code.emitGotoIfNull(_else);
    }
    else
    {
      args[0].compile(comp, stack);
      args[1].compile(comp, stack);
      if (kind == Eq)
	code.emitGotoIfNE(_else);
      else
	code.emitGotoIfEq(_else);

    }
    code.emitPushBoolean(true);
    code.emitGoto(_end);
    code.popType(); // simulate 'else' otherwise gnu.bytecode doesn't like it
    _else.define(code);
    code.emitPushBoolean(false);
    _end.define(code);

    target.compileFromStack(comp, retType);
  }

  private void compilePrimitive
    (boolean primFirst, Expression nonPrimArg, Expression primArg,
     Compilation comp)
  {
    CodeAttr code = comp.getCode();
    Target stack = Target.pushObject;
    Target primTarget = new StackTarget(primArg.getType());
    boolean eq = kind == Eq;

    if (primFirst)
      primArg.compile(comp, primTarget);
    nonPrimArg.compile(comp, stack);
    code.emitDup();

    code.emitIfNotNull();
      primTarget.compileFromStack(comp, code.topType());
      if (! primFirst)
        primArg.compile(comp, primTarget);
      code.emitIfEq();
        code.emitPushBoolean(eq);
      code.emitElse();
        code.emitPushBoolean(!eq);
      code.emitFi();
    code.emitElse();
      code.emitPop(primFirst ? 2 : 1);
      code.emitPushBoolean(!eq);
    code.emitFi();
  }

  public void compileJump (Compilation comp, Expression[] args, Label to)
  {
    compileJump(comp, args, to, false);
  }

  public void compileJumpNot (Compilation comp, Expression[] args, Label to)
  {
    compileJump(comp, args, to, true);
  }

  private void compileJump (Compilation comp, Expression[] args, Label to,
                            boolean not)
  {
    CodeAttr code = comp.getCode();
    Target stack = Target.pushObject;

    boolean prim0 = args[0].getType() instanceof PrimType;
    boolean prim1 = args[1].getType() instanceof PrimType;

    if (prim1 && ! prim0 || prim0 && ! prim1)
      {
        compile(args, comp, StackTarget.getInstance(Type.boolean_type));
        if (not)
          code.emitGotoIfIntEqZero(to);
        else
          code.emitGotoIfIntNeZero(to);
      }

    else if (args[0] instanceof QuoteExp &&
       ((QuoteExp)args[0]).getType() == Type.nullType)
    {
      if (args[1].getType() instanceof PrimType)
        {
          if (not ^ kind == Ne)
            code.emitGoto(to);

          return;
        }

      args[1].compile(comp, stack);
      if (not ^ kind == Eq)
	code.emitGotoIfNull(to);
      else
	code.emitGotoIfNotNull(to);
    }
    else if (args[1] instanceof QuoteExp &&
       ((QuoteExp)args[1]).getType() == Type.nullType)
    {
      if (args[0].getType() instanceof PrimType)
        {
          if (not ^ kind == Ne)
            code.emitGoto(to);

          return;
        }

      args[0].compile(comp, stack);
      if (not ^ kind == Eq)
	code.emitGotoIfNull(to);
      else
	code.emitGotoIfNotNull(to);
    }
    else
    {
      args[0].compile(comp, stack);
      args[1].compile(comp, stack);
      if (not ^ kind == Eq)
	code.emitGotoIfEq(to);
      else
	code.emitGotoIfNE(to);
    }
  }

  private static final Type retType = Type.boolean_type;

  public Type getReturnType (Expression[] args)
  {
    return retType;
  }

  public void checkSpecialRequirements(bossa.syntax.Expression[] arguments)
  {
    bossa.syntax.Expression exp = null;
    if (arguments[0].isNull())
      exp = arguments[1];
    else if (arguments[1].isNull())
      exp = arguments[0];

    if (exp != null && nice.tools.typing.Types.isSure(exp.getType().getMonotype()))
      bossa.util.User.warning(exp, "Comparing a non-null value with null");
  }

  // Interpretation

  public Object apply2 (Object arg1, Object arg2)
  {
    throw new Error("Not implemented");
  }
}
