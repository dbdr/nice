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
   Inlining of native numeric types operators.

   @version $Date$
   @author Daniel Bonniot
*/
public class NumericalOperators extends Procedure2 implements Inlineable
{
  private final static int
    error = 0,
    Sub = 1,
    Add = 2,
    Mul = 3,
    Div = 4,
    Rem = 5, // remainder (modulus)
    Neg = 6; // negation (unary -)
  

  public static NumericalOperators create(String param)
  {
    char typeChar = param.charAt(0);
    PrimType type;
    switch(typeChar)
      {
      case 'i': type = Type.int_type; break;
      case 'j': type = Type.long_type; break;
      case 'f': type = Type.float_type; break;
      case 'd': type = Type.double_type; break;
      default: 
	bossa.util.User.error("Unknown type in inlined numeric operator: " +
			      typeChar);
	type = null;
      }

    param = param.substring(1);

    int kind = error;
    if ("Sub".equals(param))
      kind = Sub;
    else if ("Add".equals(param))
      kind = Add;
    else if ("Mul".equals(param))
      kind = Mul;
    else if ("Div".equals(param))
      kind = Div;
    else if ("Rem".equals(param))
      kind = Rem;
    else if ("Neg".equals(param))
      kind = Neg;
    else
      bossa.util.User.error("Unknown inlined numeric operator " + param);
    return new NumericalOperators(kind, type);
  }

  private NumericalOperators(int kind, PrimType type)
  {
    this.kind = kind;
    this.type = type;
  }

  private final PrimType type;
  private final int kind;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();
    Target stack = new StackTarget(type);
    
    if (kind == Neg)
      {
	args[0].compile(comp, stack);
	code.emitNeg();
      }
    else
      {
	args[0].compile(comp, stack);
	args[1].compile(comp, stack);
	
	switch(kind){
	case Sub: code.emitSub(type); break;
	case Add: code.emitAdd(type); break;
	case Mul: code.emitMul();     break;
	case Div: code.emitDiv();     break;
	case Rem: code.emitRem();     break;
	}
      }
    
    target.compileFromStack(comp, type);
  }

  public Type getReturnType (Expression[] args)
  {
    return type;
  }

  // Interpretation

  public Object apply2 (Object arg1, Object arg2)
  {
    throw new Error("Not implemented");
  }
}
