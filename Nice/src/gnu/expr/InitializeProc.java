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
   Call one of the constructors of a class.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class InitializeProc extends ProcedureN implements Inlineable
{
  public InitializeProc (Method constructor)
  {
    this.constructor = constructor;
  }

  public InitializeProc (ConstructorExp method)
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

    args[0].compile(comp, Target.pushObject);
    for (int i = 1; i < args.length; i++)
      args[i].compile(comp, types[i - 1]);

    code.emitInvokeSpecial(constructor);
  }

  public Type getReturnType (Expression[] args)
  {
    return Type.void_type;
  }

  /****************************************************************
   * Interpretation
   ****************************************************************/

  public Object applyN (Object args[])
  {
    throw new Error("Not implemented");
  }
}
