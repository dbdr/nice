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

package nice.tools.code;

import gnu.mapping.Procedure2;
import gnu.expr.*;
import gnu.bytecode.*;

/**
   Inlining of native integer operators.
*/
public class ComparisonOperators extends Procedure2 implements Inlineable
{
  private final static int
    error = 0,
    Eq = 1,
    Ge = 2,
    Le = 3,
    Lt = 4,
    Gt = 5,
    Ne = 6;

  public static ComparisonOperators create(String param)
  {
    char typeChar = param.charAt(0);
    Type argType;
    switch(typeChar)
      {
      case 'i': argType = Type.int_type; break;
      case 'j': argType = Type.long_type; break;
      case 'f': argType = Type.float_type; break;
      case 'd': argType = Type.double_type; break;
      default: 
	bossa.util.User.error("Unknown type in inlined numeric operator: " +
			      typeChar);
	argType = null;
      }

    param = param.substring(1);

    int kind = error;
    if ("Eq".equals(param))
      kind = Eq;
    else if ("Ge".equals(param))
      kind = Ge;
    else if ("Le".equals(param))
      kind = Le;
    else if ("Lt".equals(param))
      kind = Lt;
    else if ("Gt".equals(param))
      kind = Gt;
    else if ("Ne".equals(param))
      kind = Ne;
    else
      bossa.util.User.error("Unknown inlined numeric operator " + param);
    return new ComparisonOperators(kind, argType);
  }

  private ComparisonOperators(int kind, Type argType)
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
    case Eq:
      code.emitIfEq();
      break;
    case Le: 
      code.emitIfLe();
      break;
    case Ge: 
      code.emitIfGe();
      break;
    case Lt: 
      code.emitIfIntLt();
      break;
    case Gt:
      code.emitIfGt();
      break;
    case Ne:
      code.emitIfNEq();
      break;
    }
    
    code.emitPushBoolean(true);
    code.emitElse();
    code.emitPushBoolean(false);
    code.emitFi();

    target.compileFromStack(comp, retType);
  }

  private final Type argType;
  private final Type retType = Type.boolean_type;
  
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
