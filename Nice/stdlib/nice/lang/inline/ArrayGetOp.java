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

import gnu.mapping.Procedure2;
import gnu.bytecode.*;
import gnu.expr.*;

/**
   Inlining of array read access.

   @version $Date$
   @author Daniel Bonniot
*/
public class ArrayGetOp
extends Procedure2 implements Inlineable
{
  public static ArrayGetOp create(String param)
  {
    Type type = Tools.type(param.charAt(0));
    if (type == null)
      bossa.util.User.error("Unknown type in array read acces operator: " +
			    param);

    return new ArrayGetOp(type);
  }

  private ArrayGetOp(Type type)
  {
    this.type = type;
    arrayTarget = new StackTarget(ArrayType.make(type));
  }

  private final Type type;
  private final StackTarget arrayTarget;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();
    
    args[0].compile(comp, arrayTarget);
    args[1].compile(comp, Tools.intTarget);
    
    code.emitArrayLoad(type);
    
    target.compileFromStack(comp, type);
  }

  public Type getReturnType (Expression[] args)
  {
    return type;
  }

  // Interpretation

  public Object apply2 (Object arg1, Object arg2)
  {
    return java.lang.reflect.Array.get(arg1, ((Number) arg2).intValue());
  }
}
