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
  public static Instanceof create(String param)
  {
    return instance;
  }

  public final static Instanceof instance = new Instanceof();
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    Expression[] args = exp.getArgs();
    Expression value = args[0];
    Type type = (Type) ((QuoteExp) args[1]).getValue();

    if (type instanceof PrimType)
      throw new bossa.util.UserError
	(exp, "instanceof cannot be used with primitive types");

    value.compile(comp, Target.pushObject);
    code.emitInstanceof(type);
    target.compileFromStack(comp, Type.boolean_type);
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
