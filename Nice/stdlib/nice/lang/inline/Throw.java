/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2001                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.lang.inline;

/**
   Throws its first argument as exception.

   The first argument must be a subtype of nice.lang.Throwable
   or the bytecode generated will be incorrect.

   @version $Date$
   @author Per Bothner
   @author Daniel Bonniot (Daniel.Bonniot@inria.fr)
 */

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

public class Throw extends Procedure1 implements Inlineable
{
  public static Throw create(String param)
  {
    return instance;
  }
  
  public final static Throw instance = new Throw();
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    exp.getArgs()[0].compile(comp, Target.pushObject);
    code.emitThrow();
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
