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

package nice.lang.inline;

import gnu.mapping.Procedure3;
import gnu.bytecode.*;
import gnu.expr.*;

/**
   Inlining of array write access.

   @version $Date$
   @author Daniel Bonniot
*/
public class ArraySetOp
extends Procedure3 implements Inlineable
{
  public static ArraySetOp create(String param)
  {
    Type type = Tools.type(param.charAt(0));
    if (type == null)
      bossa.util.User.error("Unknown type in array write acces operator: " +
			    param);

    return new ArraySetOp(type);
  }

  public ArraySetOp(Type type)
  {
    this.type = type;
  }

  private final Type type;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();
    boolean needReturn = !(target instanceof IgnoreTarget);

    args[0].compile(comp, Target.pushObject);
    boolean bytecodeArray = Tools.monomorphicArray(code.topType());
    args[1].compile(comp, Tools.intTarget);

    Type componentType = getComponentType(args[0].getType());

    args[2].compile(comp, componentType);
    
    if (needReturn)
      code.emitDup(componentType.getSize() > 4 ? 2 : 1, 2);

    if (bytecodeArray)
      code.emitArrayStore(componentType);
    else
      code.emitInvokeStatic(reflectSet);

    if (needReturn)
      target.compileFromStack(comp, componentType);
  }

  private static Method reflectSet = 
    ClassType.make("nice.lang.rawArray").getDeclaredMethod("Array_set", 3);

  public Type getReturnType (Expression[] args)
  {
    return getComponentType(args[0].getType());
  }

  private Type getComponentType(Type array)
  {
    // Try to get bytecode type information from the target array.
    if (this.type == Type.pointer_type && array instanceof ArrayType)
      return ((ArrayType) array).getComponentType();

    return this.type;
  }

  // Interpretation

  public Object apply3 (Object arg1, Object arg2, Object arg3)
  {
    java.lang.reflect.Array.set(arg1, ((Number) arg2).intValue(), 
				type.coerceFromObject(arg3));
    return gnu.mapping.Values.empty;
  }
}
