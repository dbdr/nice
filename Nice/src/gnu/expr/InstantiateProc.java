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

package gnu.expr;

import gnu.bytecode.*;
import gnu.mapping.*;

/**
   Instantiate an object and call one of its constructors.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class InstantiateProc extends ProcedureN implements Inlineable
{
  public InstantiateProc (Method constructor)
  {
    this.constructor = constructor;
  }

  public InstantiateProc (ConstructorExp method)
  {
    this.method = method; 
  }

  private Method constructor;
  private ConstructorExp method;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    if (constructor == null)
      constructor = method.getMainMethod();

    gnu.bytecode.CodeAttr code = comp.getCode();
    Expression[] args = exp.getArgs();
    Type[] types = constructor.getParameterTypes();

    ClassType type = constructor.getDeclaringClass();
    code.emitNew(type);
    code.emitDup(type);

    for (int i = 0; i < args.length; i++)
      args[i].compile(comp, types[i]);

    // Add dummy arguments to match the bytecode constructor.
    if (method !=null)
      for (int i = 0; i < method.dummyArgs; i++)
        code.emitPushInt(0);

    code.emitInvokeSpecial(constructor);
    target.compileFromStack(comp, type);
  }

  public Type getReturnType (Expression[] args)
  {
    if (constructor != null)
      return constructor.getDeclaringClass();
    else
      return method.getClassType();
  }

  /****************************************************************
   * Interpretation
   ****************************************************************/

  public Object applyN (Object args[])
  {
    throw new Error("Not implemented");
  }
}
