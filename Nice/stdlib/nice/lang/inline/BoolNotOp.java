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

import gnu.mapping.Procedure1;
import gnu.expr.*;
import gnu.bytecode.*;

/**
   Inlining of native unary boolean operators.

   @author Daniel Bonniot
*/
public class BoolNotOp extends Procedure1 implements Inlineable, Branchable
{
  private BoolNotOp()
  {
  }

  public static BoolNotOp instance = new BoolNotOp();

  public static BoolNotOp create(String param)
  {
    return instance;
  }

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.boolean_type);
    Branchable branchOp = args[0].getBranchable();

    if (branchOp != null)
    {
      branchOp.compileIfNot(comp, ((ApplyExp)args[0]).getArgs());
      code.emitPushBoolean(true);
      code.emitElse();
      code.emitPushBoolean(false);
      code.emitFi();
    }
    else
    {
      args[0].compile(comp, stack);
      code.emitPushConstant(1, Type.int_type); 
      code.emitXOr();
    }
    target.compileFromStack(comp, retType);
  }

  public void compileJump (Compilation comp, Expression[] args, Label to)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.boolean_type);
    Branchable branchOp = args[0].getBranchable();

    if (branchOp != null)
    {
      branchOp.compileJumpNot(comp, ((ApplyExp)args[0]).getArgs(), to);
    }
    else
    {
      args[0].compile(comp, stack);
      code.emitGotoIfIntEqZero(to);
    }
  }

  public void compileJumpNot (Compilation comp, Expression[] args, Label to)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.boolean_type);
    Branchable branchOp = args[0].getBranchable();

    if (branchOp != null)
    {
      branchOp.compileJump(comp, ((ApplyExp)args[0]).getArgs(), to);
    }
    else
    {
      args[0].compile(comp, stack);
      code.emitGotoIfIntNeZero(to);
    }

  }

  public void compileIf (Compilation comp, Expression[] args)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.boolean_type);
    Branchable branchOp = args[0].getBranchable();
    if (branchOp != null)
    {
      branchOp.compileIfNot(comp, ((ApplyExp)args[0]).getArgs());
    }
    else
    {
      args[0].compile(comp, stack);
      code.emitIfIntZero();
    }

  }

  public void compileIfNot (Compilation comp, Expression[] args)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(Type.boolean_type);
    Branchable branchOp = args[0].getBranchable();

    if (branchOp != null)
    {
      branchOp.compileIf(comp, ((ApplyExp)args[0]).getArgs());
    }
    else
    {
      args[0].compile(comp, stack);
      code.emitIfIntNotZero();
    }

  }

  private static final Type retType = Type.boolean_type;
  
  public Type getReturnType (Expression[] args)
  {
    return retType;
  }

  // Interpretation

  public Object apply1 (Object arg)
  {
    throw new Error("Not implemented");
  }
}
