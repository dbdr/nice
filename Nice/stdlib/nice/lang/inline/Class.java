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
   Create a Class value from its name.

   @version $Date$
   @author Daniel Bonniot (Daniel.Bonniot@inria.fr)
 */

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

public class Class extends Procedure1 implements Inlineable
{
  public static Class create(String param)
  {
    return instance;
  }

  private Method forName = ClassType.make("java.lang.Class").getDeclaredMethod("forName", 1);

  public final static Class instance = new Class();
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    Type type = (Type) ((QuoteExp) exp.getArgs()[0]).getValue();
    String representation = type instanceof ArrayType ? 
      type.getSignature().replace('/', '.') : 
      type.getName();
    new QuoteExp(representation).compile(comp, Target.pushObject);

    code.emitInvokeStatic(forName);
  }

  public Type getReturnType (Expression[] args)
  {
    return Type.neverReturnsType;
  }

  /****************************************************************
   * Interpretation
   ****************************************************************/

  public Object apply1 (Object arg1)
  {
    // Throw it if it is not a checked exception
    if (arg1 instanceof Throwable)
      throw ((RuntimeException) arg1);
    else if (arg1 instanceof Error)
      throw ((Error) arg1);
    // Otherwise wrap it
    else
      throw (new RuntimeException(arg1.toString()));
  }
}
