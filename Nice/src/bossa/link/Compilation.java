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
import bossa.util.User;

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

    Expression body = dispatch
      (sortedAlternatives.iterator(), 
       m.javaReturnType(), m.javaReturnType().isVoid(), params);

    if (m.isMain())
      body = beautifyUncaughtExceptions(body);

    Gen.setMethodBody(lexp, m.getContract().compile(body));
  }
  
  private static gnu.bytecode.Method newError;
  static 
  {
    ClassType error = ClassType.make("java.lang.Error");
    newError = error.getDeclaredMethod("<init>", new Type[]{Type.string_type});
  }

  private static Expression dispatch(Iterator sortedAlternatives, 
				     Type returnType, 
				     boolean voidReturn,
				     Expression[] params)
  {
    if(!sortedAlternatives.hasNext())
      {
	// We produce code that should never be reached at run-time.

	Expression message = new QuoteExp("Message not understood");
	Expression exception = new ApplyExp(new InstantiateProc(newError), 
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
      return gnu.expr.SimpleIfExp.make
	(alt.matchTest(params, false), 
	 matchCase, 
	 dispatch(sortedAlternatives, returnType, voidReturn, params));
  }

  private static Expression beautifyUncaughtExceptions(Expression body)
  {
    TryExp res = new TryExp(body, null);

    CatchClause c = new CatchClause("uncaughtException", Type.throwable_type);
    res.setCatchClauses(c);

    gnu.bytecode.Method print = ClassType.make("nice.lang.dispatch").
      addMethod("printStackTraceWithSourceInfo", 
                Access.PUBLIC | Access.STATIC,
                new Type[]{ Type.throwable_type },
                Type.void_type);

    c.setBody
      (new ApplyExp
       (new PrimProcedure(print),
        new Expression[]{ new ReferenceExp(c.getDeclaration()) }));

    return res;
  }

  /****************************************************************
   * Java Methods
   ****************************************************************/

  static void compile(JavaMethod m, 
		      Stack sortedAlternatives, 
		      bossa.modules.Package module)
  {
    int arity = m.getArity();

    while (sortedAlternatives.size() > 0) 
      {
	// We pick a class, and compile all implementations whose
	// first argument is at that class.
	Iterator i = sortedAlternatives.iterator();
	Alternative a = (Alternative) i.next();
	NiceClass c = declaringClass(m, a);
	i.remove();

	List l = new LinkedList();
	l.add(a);
	while (i.hasNext())
	  {
	    a = (Alternative) i.next();
	    if (declaringClass(m, a) == c)
	      {
		l.add(a);
		i.remove();
	      }
	  }

	Expression[] params = new Expression[arity];
	LambdaExp lambda = 
	  Gen.createMemberMethod
	    (m.getName().toString(), 
	     c.getClassExp().getType(), 
	     m.javaArgTypes(),
	     m.javaReturnType(),
	     params);

	c.addJavaMethod(lambda);
	Expression body = dispatchJavaMethod
	  (l.iterator(), m.javaReturnType(), m.javaReturnType().isVoid(), 
	   params, (ClassType) c.getClassExp().getType(), m);
	Gen.setMethodBody(lambda, body);
      }
  }

  private static NiceClass declaringClass(JavaMethod m, Alternative alt)
  {
    mlsub.typing.TypeConstructor firstArgument = alt.getPatterns()[0].tc;
    ClassDefinition def = ClassDefinition.get(firstArgument);

    if (def == null || ! (def.getImplementation() instanceof NiceClass))
      throw User.error(alt, 
		       m + " is a native method.\n" + 
		       "It can not be overriden because the first argument" +
		       (firstArgument == null 
			? "" 
			: " " + firstArgument.toString())
		       + " is not a class defined in Nice");

    return (NiceClass) def.getImplementation();
  }

  private static Expression dispatchJavaMethod
    (Iterator sortedAlternatives, 
     Type returnType, 
     boolean voidReturn,
     Expression[] params,
     ClassType c, JavaMethod m)
  {
    if (!sortedAlternatives.hasNext())
      {
	// Call super.
	ClassType superClass = c.getSuperclass();
	gnu.bytecode.Method superMethod = superClass.getMethod
	  (m.getName().toString(), m.javaArgTypes(), true);
	if (superMethod != null)
	  return new ApplyExp
	    (new QuoteExp(PrimProcedure.specialCall(superMethod)), params);
	
	// We produce code that should never be reached at run-time.

	Expression message = new QuoteExp("Message not understood");
	Expression exception = new ApplyExp(new InstantiateProc(newError), 
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
    
    return gnu.expr.SimpleIfExp.make
      (alt.matchTest(params, /* skip first */ true), 
       matchCase, 
       dispatchJavaMethod(sortedAlternatives, returnType, voidReturn, 
			  params, c, m));
  }
}
