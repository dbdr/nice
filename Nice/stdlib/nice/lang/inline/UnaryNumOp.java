/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.lang.inline;

/**
   Inlining of native unary numeric operators.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
*/

import gnu.mapping.Procedure1;
import gnu.expr.*;
import gnu.bytecode.*;

public class UnaryNumOp extends Procedure1 implements Inlineable
{
  private final static int
    error = 0,
    Neg   = 1, // negation (unary -)
    Comp  = 2; // bitwise complement (~)
  
  public static Procedure1 create(String param)
  {
    PrimType type = Tools.numericType(param.charAt(0));
    if (type == null)
      bossa.util.User.error("Unknown type in inlined numeric operator: " +
			    param);

    param = param.substring(1);

    int kind = error;
    if ("Neg".equals(param)) kind = Neg;
    else if ("Comp".equals(param))kind = Comp;
    else
      bossa.util.User.error("Unknown inlined unary numeric operator " + param);
    return new UnaryNumOp (kind, type);
  }

  private UnaryNumOp (int kind, PrimType type)
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
    args[0].compile(comp, stack);
    
    if (kind == Neg)
      {
	code.emitNeg();
      }
    else if (kind == Comp) // Bitwise complement
      {
	// ~x == (x xor -1)
	if (type == Type.long_type)
	  code.emitPushLong(-1);
	else
	  code.emitPushInt(-1);
	code.emitXOr();
      }
    else
      throw new Error();

    target.compileFromStack(comp, type);
  }

  public Type getReturnType (Expression[] args)
  {
    return type;
  }

  // Interpretation

  public Object apply1 (Object arg)
  {
    throw new Error("Not implemented");
  }
}
