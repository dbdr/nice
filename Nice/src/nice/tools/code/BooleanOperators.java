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

import gnu.mapping.ProcedureN;
import gnu.expr.*;
import gnu.bytecode.*;

/**
   Inlining of native boolean operators.

   @author Daniel Bonniot
*/
public class BooleanOperators extends ProcedureN implements Inlineable
{
  private final static int
    error = 0,
    Not = 1,
    And = 2,
    Or = 3;

  public static BooleanOperators create(String param)
  {
    int kind = error;
    if ("!".equals(param))
      kind = Not;
    else if ("&".equals(param))
      kind = And;
    else if ("|".equals(param))
      kind = Or;
    else
      bossa.util.User.error("Unknown inlined boolean operator " + param);
    return new BooleanOperators(kind);
  }

  private BooleanOperators(int kind)
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
    if (kind != Not)
      args[1].compile(comp, stack);

    switch(kind){
    case Not: 
      code.emitPushConstant(1, Type.int_type); 
      code.emitXOr();
      break;
    case And: code.emitAnd(); break;
    case Or:  code.emitIOr(); break;
    }
    
    target.compileFromStack(comp, retType);
  }

  private final Type retType = Type.boolean_type;
  
  public Type getReturnType (Expression[] args)
  {
    return retType;
  }

  // Interpretation

  public Object applyN (Object[] args)
  {
    throw new Error("Not implemented");
  }
}
