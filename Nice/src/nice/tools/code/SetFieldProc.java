/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
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
   Modifies the value of an object's field and returns the new value.
   
   @version $Date$
   @author Daniel Bonniot
*/

public class SetFieldProc extends gnu.mapping.Procedure2 implements Inlineable
{
  private Declaration fieldDecl;

  public SetFieldProc (Declaration fieldDecl)
  {
    this.fieldDecl = fieldDecl; 
    if (fieldDecl == null)
      throw new NullPointerException();
  }

  public Object apply2 (Object arg1, Object arg2)
  {
    Field field = fieldDecl.field;
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
    Field field = fieldDecl.field;
    Type fieldType = field.getType();
    
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    // tells whether we want the value to be returned
    boolean ignore = target instanceof IgnoreTarget;

    ClassType ctype = field.getDeclaringClass();
    args[0].compile(comp, ctype);
    args[1].compile(comp, fieldType);

    if(!ignore)
    // Place a copy of the new value before the two operands
      code.emitDup(fieldType.getSize() > 4 ? 2 : 1, 1);

    code.emitPutField(field);

    if(!ignore)
      target.compileFromStack(comp, fieldType);
  }

  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return fieldDecl.getType();
  }
}
