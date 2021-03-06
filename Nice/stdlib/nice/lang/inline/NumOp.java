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
   Inlining of native numeric types operators.

   @version $Date$
   @author Daniel Bonniot
*/
public class NumOp extends Procedure2 implements Inlineable
{
  private final static int
    error=  0,
    Sub  =  1,
    Add  =  2,
    Mul  =  3,
    Div  =  4,
    Rem  =  5, // remainder (modulus)
    And  =  7,
    IOr  =  8,
    XOr  =  9,
    Shl  = 10, // left shift (<<)
    Shr  = 11, // right shift (>>)
    uShr = 12; // unsigned right shift (>>>)
  
  
  public static NumOp create(String param)
  {
    PrimType type = Tools.numericType(param.charAt(0));
    if (type == null)
      bossa.util.User.error("Unknown type in inlined numeric operator: " +
			    param);

    param = param.substring(1);

    int kind = error;
    if ("Sub".equals(param))      kind = Sub;
    else if ("Add".equals(param)) kind = Add;
    else if ("Mul".equals(param)) kind = Mul;
    else if ("Div".equals(param)) kind = Div;
    else if ("Rem".equals(param)) kind = Rem;
    else if ("And".equals(param)) kind = And;
    else if ("IOr".equals(param)) kind = IOr;
    else if ("XOr".equals(param)) kind = XOr;
    else if ("Shl".equals(param)) kind = Shl;
    else if ("Shr".equals(param)) kind = Shr;
    else if ("uShr".equals(param))kind = uShr;
    else
      bossa.util.User.error("Unknown inlined numeric operator " + param);
    return new NumOp (kind, type);
  }

  private NumOp (int kind, PrimType type)
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
    
    if (kind >= Shl && kind <= uShr)
      {
	args[1].compile(comp, Tools.intTarget);

	switch(kind){
	case Shl: code.emitShl(); break;
	case Shr: code.emitShr(); break;
	case uShr: code.emitUshr(); break;
	}
      }
    else
      {
	args[1].compile(comp, stack);
	
	switch(kind){
	case Sub: code.emitSub(type); break;
	case Add: code.emitAdd(type); break;
	case Mul: code.emitMul();     break;
	case Div: code.emitDiv();     break;
	case Rem: code.emitRem();     break;
	case And: code.emitAnd();     break;
	case IOr: code.emitIOr();     break;
	case XOr: code.emitXOr();     break;
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
