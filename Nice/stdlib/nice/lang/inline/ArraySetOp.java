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
    arrayTarget = new StackTarget(nice.tools.code.SpecialTypes.array(type));
  }

  private final Type type;
  private final StackTarget arrayTarget;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();
    
    args[0].compile(comp, arrayTarget);
    boolean bytecodeArray = Tools.monomorphicArray(code.topType());
    args[1].compile(comp, Tools.intTarget);
    args[2].compile(comp, type);
    
    if (bytecodeArray)
      code.emitArrayStore(type);
    else
      code.emitInvokeStatic(reflectGet);
  }

  private static Method reflectGet = 
    ClassType.make("java.lang.reflect.Array").getDeclaredMethod("set", 3);

  public Type getReturnType (Expression[] args)
  {
    return Type.void_type;
  }

  // Interpretation

  public Object apply3 (Object arg1, Object arg2, Object arg3)
  {
    java.lang.reflect.Array.set(arg1, ((Number) arg2).intValue(), 
				type.coerceFromObject(arg3));
    return gnu.mapping.Values.empty;
  }
}
