/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.code;

import gnu.bytecode.*;
import gnu.expr.*;
import bossa.syntax.MonoSymbol;

/**
   Code generation functions.
*/
public class Gen
{
  /**
     Create a lambda expression to generate code for the method.
     @param args can be null if there are no parameters.
  */
  public static LambdaExp createMethod(Method method, MonoSymbol[] args)
  {
    Type[] argTypes = method.getParameterTypes();
    Type retType = method.getReturnType();
    int arity = args == null ? 0 : args.length;

    BlockExp blockExp = new gnu.expr.BlockExp(retType);

    gnu.expr.LambdaExp lexp = new gnu.expr.LambdaExp(blockExp);
    lexp.setName(method.getName());
    lexp.setPrimMethod(method);
    lexp.min_args = lexp.max_args = arity;

    // Parameters
    for(int n = 0; n < arity; n++)
      {
	String parameterName = args[n].getName() == null 
	  ? "anonymous_" + n 
	  : args[n].getName().toString();

	gnu.expr.Declaration d = lexp.addDeclaration(parameterName);
	d.setParameter(true);
	d.setType(argTypes[n]);
	d.noteValue(null);
	args[n].setDeclaration(d);
      }

    return lexp;
  }
}
