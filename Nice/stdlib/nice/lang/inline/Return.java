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
   Returns from a method, possibly with a value.

   The first argument is the returned value.

   @version $Date$
   @author Daniel Bonniot (Daniel.Bonniot@inria.fr)
 */

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

public class Return extends Procedure1 implements Inlineable
{
  public static Return create (String param)
  {
    return instance;
  }
  
  public static final Return instance = new Return();

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    Type type = code.getMethod().getReturnType();
    Expression[] args = exp.getArgs();

    if (args != null && args.length != 0)
      args[0].compile(comp, new StackTarget(type));

    code.emitReturn();
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
    return arg1;
  }
}
