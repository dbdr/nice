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

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

/**
   Get the value of an object's field.

   @author Daniel Bonniot
 */

public class GetFieldProc extends Procedure1 implements Inlineable
{
  public GetFieldProc (Declaration fieldDecl)
  {
    this.fieldDecl = fieldDecl;

    // Fail fast.
    if (fieldDecl == null)
      throw new NullPointerException();
  }

  private Declaration fieldDecl;

  Field getField() { return fieldDecl.field; }

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Field field = fieldDecl.field;
    boolean isStatic = field.getStaticFlag();
    CodeAttr code = comp.getCode();

    if (!isStatic)
      {
	ClassType ctype = field.getDeclaringClass();
	exp.getArgs()[0].compile(comp, ctype);
	code.emitGetField(field);
      }
    else
      code.emitGetStatic(field);

    target.compileFromStack(comp, field.getType());
  }

  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return fieldDecl.getType();
  }

  // Interpretation

  public Object apply1 (Object arg1)
  {
    Field field = fieldDecl.field;
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
