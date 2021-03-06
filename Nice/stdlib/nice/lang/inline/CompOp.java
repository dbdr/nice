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
   Inlining of native numeric types comparison operators.

   @author Daniel Bonniot
*/
public class CompOp extends Procedure2 implements Branchable, bossa.syntax.Macro
{
  private final static int
    error = 0,
    Eq = 1,
    Ge = 2,
    Le = 3,
    Lt = 4,
    Gt = 5,
    Ne = 6;

  public static CompOp create(String param)
  {
    PrimType argType = Tools.numericType(param.charAt(0));
    if (argType == null)
      bossa.util.User.error("Unknown type in inlined comparison operator: " +
			    param);

    param = param.substring(1);

    int kind = error;
    if ("Eq".equals(param))      kind = Eq;
    else if ("Ge".equals(param)) kind = Ge;
    else if ("Le".equals(param)) kind = Le;
    else if ("Lt".equals(param)) kind = Lt;
    else if ("Gt".equals(param)) kind = Gt;
    else if ("Ne".equals(param)) kind = Ne;
    else
      bossa.util.User.error("Unknown inlined numeric operator " + param);
    return new CompOp (kind, argType);
  }

  private CompOp (int kind, PrimType argType)
  {
    this.kind = kind;
    this.argType = argType;
  }

  private final int kind;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(argType);

    args[0].compile(comp, stack);
    args[1].compile(comp, stack);

    switch(kind){
    case Eq: code.emitIfEq(); break;
    case Le: code.emitIfLe(); break;
    case Ge: code.emitIfGe(); break;
    case Lt: code.emitIfLt(); break;
    case Gt: code.emitIfGt(); break;
    case Ne: code.emitIfNEq(); break;
    }
    
    code.emitPushBoolean(true);
    code.emitElse();
    code.emitPushBoolean(false);
    code.emitFi();

    target.compileFromStack(comp, retType);
  }

  /** 
      Jump to label <code>to</code> if the comparison is true.
  */
  public void compileJump (Compilation comp, Expression[] args, Label to)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(argType);

    args[0].compile(comp, stack);
    args[1].compile(comp, stack);

    switch(kind){
    case Eq: code.emitGotoIfEq(to); break;
    case Le: code.emitGotoIfLe(to); break;
    case Ge: code.emitGotoIfGe(to); break;
    case Lt: code.emitGotoIfLt(to); break;
    case Gt: code.emitGotoIfGt(to); break;
    case Ne: code.emitGotoIfNE(to); break;
    }    
  }

  /** 
      Jump to label <code>to</code> if the comparison is false.
  */
  public void compileJumpNot (Compilation comp, Expression[] args, Label to)
  {
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(argType);

    args[0].compile(comp, stack);
    args[1].compile(comp, stack);

    switch(kind){
    case Eq: code.emitGotoIfNE(to); break;
    case Le: code.emitGotoIfGt(to); break;
    case Ge: code.emitGotoIfLt(to); break;
    case Lt: code.emitGotoIfGe(to); break;
    case Gt: code.emitGotoIfLe(to); break;
    case Ne: code.emitGotoIfEq(to); break;
    }    
  }

  private final PrimType argType;
  private final Type retType = Type.boolean_type;
  
  public Type getReturnType (Expression[] args)
  {
    return retType;
  }

  public void checkSpecialRequirements(bossa.syntax.Expression[] arguments)
  {
    /* TODO: make this work again
    bossa.syntax.ConstantExp literalexp = null;
    bossa.syntax.Expression otherexp = null;
    if (arguments[0] instanceof bossa.syntax.ConstantExp &&
        ! (arguments[1] instanceof bossa.syntax.ConstantExp))
      {
        literalexp = (bossa.syntax.ConstantExp)arguments[0];
        otherexp = arguments[1];
      }
    else if (arguments[1] instanceof bossa.syntax.ConstantExp &&
       	     ! (arguments[0] instanceof bossa.syntax.ConstantExp))
      {
        literalexp = (bossa.syntax.ConstantExp)arguments[1];
        otherexp = arguments[0];
      }
   
    if (literalexp != null)
      {	
	mlsub.typing.TypeConstructor tc = nice.tools.typing.Types.equivalent(otherexp.getType().getMonotype()).head();
	if (mlsub.typing.Typing.testRigidLeq(tc, literalexp.tc) &&
	   ! (mlsub.typing.Typing.testRigidLeq(literalexp.tc, tc)))
          bossa.util.User.warning(otherexp, "Comparing a value with a constant outside the range of that value");
      }
    */ 
  }

  // Interpretation

  public Object apply2 (Object arg1, Object arg2)
  {
    throw new Error("Not implemented");
  }
}
