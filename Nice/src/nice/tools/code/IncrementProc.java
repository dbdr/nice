/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : IncrementProc.java
// Created : Mon Jul 24 16:34:33 2000 by Daniel Bonniot
//$Modified: Mon Jul 24 18:57:39 2000 by Daniel Bonniot $

package nice.tools.code;

import gnu.expr.*;
import gnu.bytecode.*;

/**
 * Increment the value of an object field. Leaves the old value on the stack.
 * 
 * @author Daniel Bonniot
 */

public class IncrementProc extends gnu.mapping.Procedure1 implements Inlineable
{
  private Field field;
  private boolean increment;

  public IncrementProc (Field field, boolean increment)
  {
    this.field = field;
    this.increment = increment;
  }

  public Object apply1 (Object arg1)
  {
    throw new RuntimeException ("not implemented");
  }

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    ClassType ctype = field.getDeclaringClass();
    Expression[] args = exp.getArgs();

    boolean isLong = field.getType().getSize() > 4;
    PrimType type = isLong ? Type.long_type : Type.int_type;
    
    args[0].compile(comp, ctype);
    code.emitDup(ctype);
    code.emitGetField(field);
    // Place a copy of the old value before the two operands
    code.emitDup(isLong ? 2 : 1, 1);
    if(isLong)
      code.emitPushLong(1);
    else
      code.emitPushInt(1);
    if (increment)
      code.emitAdd(type);
    else
      code.emitSub(type);
    code.emitPutField(field);
    target.compileFromStack(comp, field.getType());
  }

  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return field.getType();
  }
}
