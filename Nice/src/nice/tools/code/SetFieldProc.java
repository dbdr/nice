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

// File    : SetFieldProc.java
// Created : Mon Jul 24 16:34:33 2000 by Daniel Bonniot
//$Modified: Thu Sep 07 17:37:47 2000 by Daniel Bonniot $

package nice.tools.code;

import gnu.expr.*;
import gnu.bytecode.*;

/**
 * Modifies the value of an object's field and returns the new value.
 * 
 * @author Daniel Bonniot
 */

public class SetFieldProc extends gnu.mapping.Procedure2 implements Inlineable
{
  private Field field;

  public SetFieldProc (Field field)
  {
    this.field = field; 
  }

  public Object apply2 (Object arg1, Object arg2)
  {
    try
      {
	java.lang.reflect.Field reflectField = field.getReflectField();
	arg2 = field.getType().coerceFromObject(arg2);
	reflectField.set(arg1, arg2);
      }
    catch (NoSuchFieldException ex)
      {
	throw new RuntimeException ("no such field " + field.getSourceName()
				    + " in " + 
				    field.getDeclaringClass().getName());
      }
    catch (IllegalAccessException ex)
      {
	throw new RuntimeException("illegal access for field "
				   + field.getSourceName());
      }
    return arg2;
  }
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Type fieldType = field.getType();
    
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    // tells whether we want the value to be returned
    boolean ignore = target instanceof IgnoreTarget;
    boolean isStatic = field.getStaticFlag();

    int n = 0;
    if (!isStatic)
      {
	ClassType ctype = field.getDeclaringClass();
	args[n++].compile(comp, ctype);
      }
    args[n].compile(comp, fieldType);

    if(!ignore)
    // Place a copy of the new value before the two operands
      code.emitDup(fieldType.getSize() > 4 ? 2 : 1, 1);

    if (isStatic)
      code.emitPutStatic(field);
    else
      code.emitPutField(field);

    if(!ignore)
      target.compileFromStack(comp, fieldType);
  }

  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return field.getType();
  }
}
