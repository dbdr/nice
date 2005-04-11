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
    
    if (this.assertEnableCheck && args[0] != QuoteExp.falseExp)
	code.ifAssertionsDisabledGoto
             (((ClassExp)comp.topLambda).getAssertionEnabledField(), end);

    Branchable branchOp = args[0].getBranchable();
    if (branchOp != null)
      {
        branchOp.compileJump(comp, ((ApplyExp)args[0]).getArgs(), end);
      }
    else if (args[0] == QuoteExp.falseExp)
      {
        //always continue to throwing exception
      }
    else
      {
        args[0].compile(comp, Type.boolean_type);
        code.emitGotoIfIntNeZero(end); // The assertion is true.
      }

    code.emitNew(errorClass);
    code.emitDup();
    
    prepare();
    if (args.length == 1)
      {
	code.emitInvokeSpecial(errorInit);
      }
    else
      {
	args[1].compile(comp, Target.pushObject);
        if(args[1].getType().getName().equals(Type.string_type.getName())){
          code.emitInvokeSpecial(errorInitString);
        }else{
          code.emitInvokeVirtual(errorToString);
          code.emitInvokeSpecial(errorInitString);
        }
      }

    code.emitThrow();
    target.compileFromStack(comp, Type.void_type);

    end.define(code);
  }

  private static final ClassType 
    errorClass = ClassType.make("nice.lang.AssertionFailed");

  private static Method errorInit, errorInitString, errorToString;

  private static void prepare()
  {
    if (errorInit != null)
      return;

    errorInit = errorClass.addMethod
      ("<init>", Access.PUBLIC, new Type[]{}, Type.void_type);
    errorInitString = errorClass.addMethod
      ("<init>", Access.PUBLIC, new Type[]{Type.string_type}, Type.void_type);
    errorToString = Type.pointer_type.addMethod
      ("toString", Access.PUBLIC, new Type[]{}, Type.string_type);
  }

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
