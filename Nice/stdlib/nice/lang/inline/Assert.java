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

import gnu.mapping.ProcedureN;
import gnu.expr.*;
import gnu.bytecode.*;

/**
   Test a boolean assertion, and raise an error if it is false.

   Do no throw a java.lang.AssertionError, since that class is only 
   available in JDK 1.4 and later. Instead throw a nice.lang.AssertionFailed.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class Assert extends ProcedureN implements Inlineable
{
  public Assert(boolean assertEnableCheck)
  {
    this.assertEnableCheck = assertEnableCheck;
  }

  public static Assert create(String param)
  {
    if (param == null)
      return instance;
    return contractInstance;
  }
  
  private boolean assertEnableCheck;

  private static final Assert instance = new Assert(true);
  private static final Assert contractInstance = new Assert(false);

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();
    Label end = new Label(code);
    
    if (this.assertEnableCheck)
	code.ifAssertionsDisabledGoto
             (((ClassExp)comp.topLambda).getAssertionEnabledField(), end);

    args[0].compile(comp, Type.boolean_type);
    code.emitGotoIfIntNeZero(end); // The assertion is true.

    code.emitNew(errorClass);
    code.emitDup();
    
    if (args.length == 1)
      {
	code.emitInvokeSpecial(errorInit);
      }
    else
      {
	args[1].compile(comp, Target.pushObject);
	code.emitInvokeSpecial(errorInitString);
      }

    code.emitThrow();
    target.compileFromStack(comp, Type.void_type);

    end.define(code);
  }

  private static final ClassType 
    errorClass = ClassType.make("nice.lang.AssertionFailed");

  private static final Method 
    errorInit = errorClass.getDeclaredMethod("<init>", new Type[]{}),
    errorInitString = errorClass.getDeclaredMethod
      ("<init>", new Type[]{Type.string_type});

  public Type getReturnType (Expression[] args)
  {
    return Type.void_type;
  }

  // Interpretation

  public Object applyN (Object[] args)
  {
    throw new Error("Not implemented");
  }
}
