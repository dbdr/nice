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
    this(constructor, false);
  }

  /**
     @param implicitThis true if a 'this' argument should be added
       during the call.
  */
  public InitializeProc (Method constructor, boolean implicitThis)
  {
    this.constructor = constructor;
    this.implicitThis = implicitThis;
  }

  public InitializeProc (ConstructorExp method)
  {
    this(method, false);
  }

  /**
     @param implicitThis true if a 'this' argument should be added
       during the call.
  */
  public InitializeProc (ConstructorExp method, boolean implicitThis)
  {
    this.method = method;
    this.implicitThis = implicitThis;
  }

  private Method constructor;
  private ConstructorExp method;
  private boolean implicitThis;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    if (constructor == null)
      constructor = method.getMainMethod();

    gnu.bytecode.CodeAttr code = comp.getCode();
    Expression[] args = exp.getArgs();
    Type[] types = constructor.getParameterTypes();

    int arg = 0;
    int type = 0;
    if (implicitThis)
      code.emitPushThis();
    else
      args[arg++].compile(comp, Target.pushObject);
    for (; arg < args.length; arg++)
      args[arg].compile(comp, types[type++]);

    // Add dummy arguments to match the bytecode constructor.
    if (method != null)
      for (int i = 0; i < method.dummyArgs; i++)
        code.emitPushInt(0);

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
