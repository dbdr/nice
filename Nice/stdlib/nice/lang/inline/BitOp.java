/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.lang.inline;

/**
   Bit manipulation for numeric values.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

import gnu.mapping.Procedure;
import gnu.mapping.Procedure2;
import gnu.mapping.Procedure3;
import gnu.expr.*;
import gnu.bytecode.*;

public class BitOp 
{
  public static Procedure create(String param)
  {
    if (param.equals("get"))
      return get;
    else
      return set;
  }

  private static BitOp.Set set = new BitOp.Set();
  private static BitOp.Get get = new BitOp.Get();

  // Getting a bit
  private static class Get extends Procedure2 implements Inlineable
  {
    public void compile (ApplyExp exp, Compilation comp, Target target)
    {
      Expression[] args = exp.getArgs();
      CodeAttr code = comp.getCode();

      Type type0 = args[0].getType();
      boolean isLong = type0 == Type.long_type;

      Target t0 = Target.pushValue(type0);
      args[0].compile(comp, t0);

      if (isLong)
        code.emitPushLong(1);
      else
        code.emitPushInt(1);

      Target t1 = Target.pushValue(Type.int_type);
      args[1].compile(comp, t1);
    
      code.emitShl();

      // This is needed for types shorter than int.
      if (!isLong) 
        t0.compileFromStack(comp, Type.int_type);

      code.emitAnd();

      if (isLong)
        {
          code.emitPushLong(0);
          code.emitIfEq();
          code.emitPushBoolean(false);
          code.emitElse();
          code.emitPushBoolean(true);
          code.emitFi();
        }
      else
        {
          code.emitIfIntNotZero();
          code.emitPushBoolean(true);
          code.emitElse();
          code.emitPushBoolean(false);
          code.emitFi();
        }

      target.compileFromStack(comp, Type.boolean_type);
    }

    public Type getReturnType (Expression[] args)
    {
      return Type.boolean_type;
    }

    // Interpretation

    public Object apply2 (Object arg1, Object arg2)
    {
      throw new Error("Not implemented");
    }
  }

  // Setting a bit
  private static class Set extends Procedure3 implements bossa.syntax.Macro
  {
    public void compile (ApplyExp exp, Compilation comp, Target target)
    {
      Expression[] args = exp.getArgs();
      CodeAttr code = comp.getCode();

      Type type0 = args[0].getType();
      boolean isLong = type0 == Type.long_type;

      Target t0 = Target.pushValue(type0);
      args[0].compile(comp, t0);

      if (isLong)
        code.emitPushLong(1);
      else
        code.emitPushInt(1);

      Target t1 = Target.pushValue(Type.int_type);
      args[1].compile(comp, t1);
    
      code.emitShl();

      args[2].compile(comp, Target.pushValue(Type.boolean_type));

      code.emitIfIntNotZero();

      code.emitIOr();

      code.emitElse();

      if (isLong)
        code.emitPushLong(-1);
      else
        code.emitPushInt(-1);
      code.emitXOr();
      code.emitAnd();

      code.emitFi();

      t0.compileFromStack(comp, isLong ? Type.long_type : Type.int_type);

      ((ReferenceExp) args[0]).getBinding().compileStore(comp);
    }

    public Type getReturnType (Expression[] args)
    {
      return Type.void_type;
    }

    public void checkSpecialRequirements(bossa.syntax.Expression[] arguments)
    {
      if (! arguments[0].isAssignable())
        bossa.util.User.error(arguments[0], "This value cannot be modified.");
    }

    // Interpretation

    public Object apply3 (Object arg1, Object arg2, Object arg3)
    {
      throw new Error("Not implemented");
    }
  }
}
