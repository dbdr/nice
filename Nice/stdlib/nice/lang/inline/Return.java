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
  public static Return create (Type valueType)
  {
    if (valueType instanceof ObjectType)
      return returnObject;
    else
      return new Return(valueType);
  }
  
  public  static Return returnVoid   = new Return(null);
  private static Return returnObject = new Return(Type.pointer_type);

  private Return (Type valueType)
  {
    this.type = valueType;
  }
  
  private Type type;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    System.out.println("RETURN... " + code.getMethod().getReturnType().getSize() + " t=" + type);
    Expression[] args = exp.getArgs();
    if (args != null && args.length != 0)
      {
	if (false && type.isVoid())
	  {
	    System.out.println("ARG = ");
	    args[0].print(OutPort.outDefault());
	    OutPort.outDefault().flush();
	  }
      args[0].compile(comp, new StackTarget(type));
      }

    if (type == Type.void_type 
	&& code.getMethod().getReturnType().getSize() != 0)
      QuoteExp.voidExp.compile(comp, Target.pushObject);

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
