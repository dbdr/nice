/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.lang.inline;

/**
   Tests if a value belongs to a class.

   The second parameter should be a QuoteExp wrapping a 
   <code>gnu.bytecode.Type</code>.

   @version $Date$
   @author Daniel Bonniot (Daniel.Bonniot@inria.fr)
 */

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

public class Instanceof extends Procedure2 implements Inlineable
{
  private final boolean option;

  public static Instanceof create(String param)
  {
    if ("option".equals(param))
      return optionInstance;
    
    return instance;
  }

  private Instanceof(boolean option)
  {
    this.option = option;
  }

  public final static Instanceof instance = new Instanceof(false);
  public final static Instanceof optionInstance = new Instanceof(true);
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    Expression value = args[0];
    Expression typeExp = args[1];

    if (typeExp instanceof QuoteExp &&
        ((QuoteExp) typeExp).getValue() instanceof Type)
      compile(value, (Type) ((QuoteExp) typeExp).getValue(), comp, exp);
    else
      compile(value, typeExp, comp);

    target.compileFromStack(comp, Type.boolean_type);
  }

  private void compile(Expression value, Type type, Compilation comp,
                       Expression applyExp)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();

    // instanceof on boolean can make sense
    if (type == Type.boolean_type)
      type = Type.boolean_ctype;

    if (type instanceof PrimType)
      throw new bossa.util.UserError
	(applyExp, "instanceof cannot be used with primitive types");

    value.compile(comp, Target.pushObject);

    if (type == nice.tools.code.SpecialArray.wrappedType() &&
        code.topType().isArray())
      {
        /* If we want to test if the value is 'instanceof Array', and 
           we know statically that it is an array, we just need to make
           sure it is not null.
        */
        if (option)
          code.emitPushBoolean(true);
        else
          {
            code.emitIfNull();
            code.emitPushBoolean(false);
            code.emitElse();
            code.emitPushBoolean(true);
            code.emitFi();
          }
      }
    else
      {
        if (option)
          {
            code.emitDup();
            code.emitIfNull();
            code.emitPop(1);
            code.emitPushBoolean(true);
            code.emitElse();
           }   
 
        code.emitInstanceof(type);

        if (option)
          code.emitFi();
      }
  }

  private void compile(Expression value, Expression type, Compilation comp)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();

    if (option)
      {
        code.emitDup();
        code.emitIfNull();
        code.emitPop(1);
        code.emitPushBoolean(true);
        code.emitElse();
      }   

    type.compile(comp, Target.pushObject);
    value.compile(comp, Target.pushObject);
    code.emitInvoke
      (ClassType.make("java.lang.Class").getDeclaredMethod("isInstance", 1));

    if (option)
      code.emitFi();
  }

  public Type getReturnType (Expression[] args)
  {
    return Type.boolean_type;
  }

  /****************************************************************
   * Interpretation
   ****************************************************************/

  public Object apply2 (Object arg1, Object arg2)
  {
    throw new Error("Not implemented");
  }
}
