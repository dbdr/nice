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

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

/**
   Get the value of an object's field.

   @author Daniel Bonniot
 */

public class GetFieldProc extends Procedure1 implements Inlineable
{
  public GetFieldProc (Field field)
  {
    this.field = field;
  }

  private Field field;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    ClassType ctype = field.getDeclaringClass();
    Class refClass = ctype.getReflectClass();

    exp.getArgs()[0].compile(comp, ctype);
    CodeAttr code = comp.getCode();
    code.emitGetField(field);
    
    target.compileFromStack(comp, field.getType());
  }

  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return field.getType();
  }

  // Interpretation

  public Object apply1 (Object arg1)
  {
    try
      {
	java.lang.reflect.Field reflectField = field.getReflectField();
	return reflectField.get(arg1);
      }
    catch (NoSuchFieldException ex)
      {
	throw new RuntimeException ("no such field " + field.getSourceName()
				    + " in " + field.getDeclaringClass());
      }
    catch (IllegalAccessException ex)
      {
	throw new RuntimeException("illegal access for field "
				   + field.getSourceName());
      }
  }
}
