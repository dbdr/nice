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
import gnu.bytecode.Type;
import gnu.bytecode.ClassType;
import gnu.expr.Expression;

import nice.tools.code.Gen;

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
    
    // parameters of the alternative function are the same in each case, 
    // so we compute them just once
    int arity = m.getArity();
    Expression[] params = new Expression[arity];
    int rank = 0;
    for(Declaration param = lexp.firstDecl(); rank < arity; 
	param = param.nextDecl())
      params[rank++] = new ReferenceExp(param);

    Gen.setMethodBody
      (lexp, 
       dispatch(sortedAlternatives.iterator(),
		m.javaReturnType(), m.javaReturnType().isVoid(), params));
  }
  
  private static gnu.bytecode.Method newError =
    ClassType.make("java.lang.Error").getDeclaredMethod("<init>", 1);

  private static Expression dispatch(Iterator sortedAlternatives, 
				     Type returnType, 
				     boolean voidReturn,
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

	return throwExp;
      }

    Alternative alt = (Alternative) sortedAlternatives.next();
    Expression matchCase = new ApplyExp(alt.methodExp(), params);

    if(voidReturn)
      matchCase = new BeginExp(matchCase, Gen.returnVoid());
    else
      matchCase = Gen.returnValue(matchCase);
    
    boolean optimize = true;

    if(optimize && !sortedAlternatives.hasNext())
      return matchCase;
    else
      return gnu.expr.IfExp.make
	(alt.matchTest(params), 
	 matchCase, 
	 dispatch(sortedAlternatives, returnType, voidReturn, params));
  }
}
