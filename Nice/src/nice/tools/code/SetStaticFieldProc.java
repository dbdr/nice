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

package nice.tools.code;

import gnu.expr.*;
import gnu.bytecode.*;

/**
   Modifies the value of a static class field and returns the new value.
   
   @version $Date$
   @author Daniel Bonniot
 */

public class SetStaticFieldProc extends gnu.mapping.Procedure1
implements Inlineable
{
  private Field field;

  public SetStaticFieldProc (Field field)
  {
    this.field = field;
    if (!field.getStaticFlag())
      throw new Error("Field " + field + " should be static");
  }

  public Object apply1 (Object arg)
  {
    try
      {
	java.lang.reflect.Field reflectField = field.getReflectField();
	reflectField.set(null, arg);
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
    return arg;
  }
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Type fieldType = field.getType();
    
    Expression arg = exp.getArgs()[0];
    CodeAttr code = comp.getCode();

    // tells whether we want the value to be returned
    boolean ignore = target instanceof IgnoreTarget;

    arg.compile(comp, fieldType);

    if(!ignore)
    // Place a copy of the new value before the two operands
      code.emitDup(fieldType.getSize() > 4 ? 2 : 1, 1);

    code.emitPutStatic(field);

    if(!ignore)
      target.compileFromStack(comp, fieldType);
  }

  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return field.getType();
  }
}
