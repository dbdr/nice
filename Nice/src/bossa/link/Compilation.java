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

package bossa.link;

import bossa.syntax.*;

import gnu.expr.*;
import gnu.bytecode.Access;
import gnu.bytecode.ClassType;
import gnu.expr.Expression;


import java.util.*;

/**
   Compilation of the dispatch functions.
   
   @version $Date$
   @author Daniel Bonniot
 */

public final class Compilation
{
  static void compile(NiceMethod m, 
		      Stack sortedAlternatives, 
		      bossa.modules.Package module)
  {
    BlockExp block = new BlockExp(m.javaReturnType());
    LambdaExp lexp = new LambdaExp(block);
    
    // parameters of the alternative function are the same in each case, 
    // so we compute them just once
    gnu.bytecode.Type[] types = m.javaArgTypes();
    Expression[] params = new Expression[types.length];
    lexp.min_args = lexp.max_args = types.length;
    
    for(int n = 0; n<types.length; n++)
      {
	Declaration param = lexp.addDeclaration("parameter_"+n, types[n]);
	param.setParameter(true);
	params[n] = new ReferenceExp(param);
      }

    block.setBody(dispatch(sortedAlternatives.iterator(),
			   m.getType().codomain(),
			   m.javaReturnType().isVoid(),
			   block,
			   params));

    lexp.setPrimMethod(m.getDispatchPrimMethod());
    
    module.compileDispatchMethod(lexp);
  }
  
  private static Expression dispatch(Iterator sortedAlternatives, 
				     mlsub.typing.Monotype returnType, 
				     boolean voidReturn,
				     final BlockExp block, 
				     Expression[] params)
  {
    if(!sortedAlternatives.hasNext())
      if (voidReturn)
	return new ThrowExp(new QuoteExp(new Error("Message not understood")));
      else
	return new BeginExp
	  (new ThrowExp(new QuoteExp(new Error("Message not understood"))),
	   nice.tools.code.Types.defaultValue(returnType));
    
    Alternative a = (Alternative) sortedAlternatives.next();
    Expression matchTest = a.matchTest(params);

    Expression matchCase = new ApplyExp(a.methodExp(), params);
    if(!voidReturn)
      matchCase = new ExitExp(matchCase, block);
    
    boolean optimize = false;

    if(optimize && !sortedAlternatives.hasNext())
      return matchCase;
    else
      return new gnu.expr.IfExp
	(matchTest, matchCase, 
	 dispatch(sortedAlternatives, returnType, voidReturn, block, params));
  }
}
