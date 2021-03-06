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
   Inlining of native boolean operators.

   @author Daniel Bonniot
*/
public class BoolOp extends Procedure2 implements Inlineable
{
  private final static int
    And = 1,
    Or = 2,
    Xor = 3,
    Eq = 4;

  public static BoolOp create(String param)
  {
    int kind = 0;
    if ("&".equals(param))
      kind = And;
    else if ("|".equals(param))
      kind = Or;
    else if ("^".equals(param))
      kind = Xor;
    else if ("==".equals(param))
      kind = Eq;
    else
      bossa.util.User.error("Unknown inlined boolean operator " + param);
    return new BoolOp(kind);
  }

  private BoolOp (int kind)
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
    args[1].compile(comp, stack);

    switch(kind){
    case And: code.emitAnd(); break;
    case Or:  code.emitIOr(); break;
    case Xor:  code.emitXOr(); break;
    case Eq:  code.emitXOr();
      code.emitPushConstant(1, Type.int_type); 
      code.emitXOr();
      break;
    }
    
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
