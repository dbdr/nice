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
//$Modified: Tue Jul 25 12:23:30 2000 by Daniel Bonniot $

package nice.tools.code;

import gnu.expr.*;
import gnu.bytecode.*;

/**
   Increment the value of an object field. 
   Returns either the old or the new value.
   
   @author Daniel Bonniot
 */

public class IncrementProc extends gnu.mapping.Procedure1 implements Inlineable
{
  /**
     @param field the field to increment or decrement.
     @param increment true to increment, false to decrement
     @param returnOld if true, return value before modification,
     otherwise value after modification
   */
  public IncrementProc(Declaration field, boolean returnOld, boolean increment)
  {
    this.fieldDecl = field;
    this.returnOld = returnOld;
    this.increment = increment;
  }

  private Declaration fieldDecl;
  private boolean increment;
  private boolean returnOld;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Field field = this.fieldDecl.field;
    CodeAttr code = comp.getCode();
    ClassType ctype = field.getDeclaringClass();
    Expression[] args = exp.getArgs();

    // tells whether we want the value to be returned
    boolean ignore = target instanceof IgnoreTarget;

    boolean isLong = field.getType().getSize() > 4;
    PrimType type = isLong ? Type.long_type : Type.int_type;
    
    args[0].compile(comp, ctype);
    code.emitDup(ctype);
    code.emitGetField(field);
    
    if(!ignore && returnOld)
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

    if(!ignore && !returnOld)
      // Place a copy of the new value before the two operands
      code.emitDup(isLong ? 2 : 1, 1);

    code.emitPutField(field);

    if(!ignore)
      target.compileFromStack(comp, field.getType());
  }

  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return fieldDecl.getType();
  }

  public Object apply1 (Object arg1)
  {
    throw new RuntimeException ("not implemented");
  }
}
