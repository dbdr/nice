/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2001                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.lang.inline;

/**
   Computes the length of an array.

   @version $Date$
   @author Daniel Bonniot (bonnio@users.sourceforge.net)
 */

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

public class ArrayLength extends Procedure1 implements Inlineable
{
  public static ArrayLength create(String param)
  {
    return instance;
  }
  
  public static final ArrayLength instance = new ArrayLength();

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    exp.getArgs()[0].compile(comp, Target.pushObject);

    /* 
       We try to use the 'arraylength'.
       This is always possible except if we access a special array 
       (nice.tools.code.SpecialArray) with unknown element type,
       since this one has the bytecode type java.lang.Object
    */
    ArrayType type = (ArrayType) code.topType();
    if (type.getComponentType() == Type.pointer_type)
      code.emitInvokeStatic(reflectGetLength);
    else
      code.emitArrayLength();

    target.compileFromStack(comp, Type.int_type);
  }

  private static Method reflectGetLength = 
    ClassType.make("java.lang.reflect.Array").getDeclaredMethod("getLength",1);

  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return Type.int_type;
  }

  /****************************************************************
   * Interpretation
   ****************************************************************/

  public Object apply1 (Object array)
  {
    return new Integer(java.lang.reflect.Array.getLength(array));
  }
}
