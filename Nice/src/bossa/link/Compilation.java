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
    LambdaExp lexp = m.getLambda();
    BlockExp block = (gnu.expr.BlockExp) lexp.body;
    
    // parameters of the alternative function are the same in each case, 
    // so we compute them just once
    int arity = m.getArity();
    Expression[] params = new Expression[arity];
    int rank = 0;
    for(Declaration param = lexp.firstDecl(); rank < arity; 
	param = param.nextDecl())
      params[rank++] = new ReferenceExp(param);

    block.setBody(dispatch(sortedAlternatives.iterator(),
			   m.getType().codomain(),
			   m.javaReturnType().isVoid(),
			   block,
			   params));
  }
  
  private static gnu.bytecode.Method newError =
    ClassType.make("java.lang.Error").getDeclaredMethod("<init>", 1);

  private static Expression dispatch(Iterator sortedAlternatives, 
				     mlsub.typing.Monotype returnType, 
				     boolean voidReturn,
				     final BlockExp block, 
				     Expression[] params)
  {
    if(!sortedAlternatives.hasNext())
      {
	// We produce code that should never be reached at run-time.

	Expression message = new QuoteExp("Message not understood");
	Expression exception = new ApplyExp(newError, 
					    new Expression[]{ message });
	Expression throwExp = 
	  new ApplyExp(nice.lang.inline.Throw.instance,
		       new Expression[]{exception});

	if (voidReturn)
	  return throwExp;
	else
	  return new BeginExp
	    (throwExp, nice.tools.code.Types.defaultValue(returnType));
      }

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
