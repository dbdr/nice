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
public class ShortCircuitOp extends Procedure2 implements Inlineable
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

    args[0].compile(comp, stack);
    code.emitIfIntNotZero();
    if (kind == And)
      {
	args[1].compile(comp, stack);
	code.emitElse();
	code.emitPushBoolean(false);
      }
    else
      {
	code.emitPushBoolean(true);
	code.emitElse();
	args[1].compile(comp, stack);
      }
    code.emitFi();
    target.compileFromStack(comp, retType);
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
