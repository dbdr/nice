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
   Inlining of native short-circuit operators.

   @author Daniel Bonniot
*/
public class ShortCircuitOp extends Procedure2 implements Inlineable, Branchable
{
  private final static int
    And = 2,
    Or = 3;

  public static ShortCircuitOp create(String param)
  {
    int kind;
    if ("&&".equals(param))
      kind = And;
    else if ("||".equals(param))
      kind = Or;
    else
      throw bossa.util.User.error("Unknown inlined short-circuit operator " + param);
    return new ShortCircuitOp(kind);
  }

  private ShortCircuitOp (int kind)
  {
    this.kind = kind;
  }

  private final int kind;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.boolean_type);

    Branchable branchOp = args[0].getBranchable();
    Branchable branchOp2 = args[1].getBranchable();
    Label _else = new Label(code);
    Label _end = new Label(code);

    if (branchOp != null)
    {
      Expression[] brArgs = ((ApplyExp)args[0]).getArgs();
      if (kind == And)
	branchOp.compileJumpNot(comp, brArgs, _else);
      else
	branchOp.compileJump(comp, brArgs, _else);
    }
    else
    {
      args[0].compile(comp, stack);
      if (kind == And)
	code.emitGotoIfIntEqZero(_else);
      else
	code.emitGotoIfIntNeZero(_else);
    }
    if (branchOp2 != null)
    {
      Expression[] brArgs = ((ApplyExp)args[1]).getArgs();
      if (kind == And)
	branchOp2.compileJumpNot(comp, brArgs, _else);
      else
	branchOp2.compileJump(comp, brArgs, _else);

      code.emitPushBoolean(kind == And);
    }
    else
      args[1].compile(comp, stack);

    code.emitGoto(_end);
    code.popType(); //simulate 'else' otherwise gnu.bytecode don't like it
    _else.define(code);
    code.emitPushBoolean(kind != And);
    _end.define(code);

    target.compileFromStack(comp, retType);
  }

  public void compileJump (Compilation comp, Expression[] args, Label to)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.boolean_type);

    Branchable branchOp = args[0].getBranchable();
    Branchable branchOp2 = args[1].getBranchable();
    Label _end = new Label(code);

    if (branchOp != null)
    {
      Expression[] brArgs = ((ApplyExp)args[0]).getArgs();
      if (kind == And)
	branchOp.compileJumpNot(comp, brArgs, _end);
      else
	branchOp.compileJump(comp, brArgs, to);
    }
    else
    {
      args[0].compile(comp, stack);
      if (kind == And)
	code.emitGotoIfIntEqZero(_end);
      else
	code.emitGotoIfIntNeZero(to);
    }
    if (branchOp2 != null)
    {
      Expression[] brArgs = ((ApplyExp)args[1]).getArgs();
      branchOp2.compileJump(comp, brArgs, to);
    }
    else
    {
      args[1].compile(comp, stack);
      code.emitGotoIfIntNeZero(to);
    }
    _end.define(code);
  }

  public void compileJumpNot (Compilation comp, Expression[] args, Label to)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.boolean_type);

    Branchable branchOp = args[0].getBranchable();
    Branchable branchOp2 = args[1].getBranchable();
    Label _end = new Label(code);

    if (branchOp != null)
    {
      Expression[] brArgs = ((ApplyExp)args[0]).getArgs();
      if (kind == And)
	branchOp.compileJumpNot(comp, brArgs, to);
      else
	branchOp.compileJump(comp, brArgs, _end);
    }
    else
    {
      args[0].compile(comp, stack);
      if (kind == And)
	code.emitGotoIfIntEqZero(to);
      else
	code.emitGotoIfIntNeZero(_end);
    }
    if (branchOp2 != null)
    {
      Expression[] brArgs = ((ApplyExp)args[1]).getArgs();
      branchOp2.compileJumpNot(comp, brArgs, to);
    }
    else
    {
      args[1].compile(comp, stack);
      code.emitGotoIfIntEqZero(to);
    }
    _end.define(code);
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
