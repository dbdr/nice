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
     Code that returns true if either expression is true.
     Evaluates both expressions.
  */ 
  public static Expression or(Expression e1, Expression e2)
  {
    return new ApplyExp(orProc, new Expression[]{ e1, e2 });
  }
  
  /** 
      Procedure to emit <code>or</code>. 
      Shared since it is not parametrized.
  */
  private static final Expression orProc = 
    new gnu.expr.QuoteExp(new nice.tools.code.OrProc());

  public static Expression instanceOfExp(Expression value, Type ct)
  {
    // If matching against primitive types, 
    // we know the runtime type will be the corresponding object class.
    if (ct instanceof PrimType)
      ct = Types.equivalentObjectType(ct);
    
    return Inline.inline(new InstanceOfProc(ct), value);
  }
  
  public static Expression isOfClass(Expression value, Type ct)
  {
    return Inline.inline(new IsOfClassProc(ct), value);
  }

  /**
     Create a lambda expression to generate code for the method.

     @param can be null if there are no arguments
  */
  public static LambdaExp createMethod(String bytecodeName,
				       Type[] argTypes,
				       Type retType,
				       MonoSymbol[] args)
  {
    return createMethod(bytecodeName, argTypes, retType, args, true);
  }

  /**
     @param toplevel If the method can be called from foreign code.
                     This forces its generation even if it is 
		     apparently never called.
  **/
  public static LambdaExp createMethod(String bytecodeName,
				       Type[] argTypes,
				       Type retType,
				       MonoSymbol[] args,
				       boolean toplevel)
  {
    bytecodeName = nice.tools.code.Strings.escape(bytecodeName);
    int arity = args == null ? 0 : args.length;

    gnu.expr.LambdaExp lexp = new gnu.expr.LambdaExp();
    lexp.setReturnType(retType);
    lexp.setName(bytecodeName);
    lexp.min_args = lexp.max_args = arity;
    lexp.forceGeneration();
    if (toplevel)
      lexp.setCanCall(true);

    // Parameters
    for(int n = 0; n < arity; n++)
      {
	String parameterName = args[n].getName() == null 
	  ? "anonymous_" + n 
	  : args[n].getName().toString();

	gnu.expr.Declaration d = lexp.addDeclaration(parameterName);
	if (argTypes != null)
	  d.setType(argTypes[n]);
	d.noteValue(null);
	args[n].setDeclaration(d);
      }

    return lexp;
  }

  public static void setMethodBody(LambdaExp method, Expression body)
  {
    method.body = body;
  }

  /**
     @return an expression that references the lambda expression.
  */
  public static ReferenceExp referenceTo(LambdaExp lambda)
  {
    Declaration decl = new Declaration(lambda.getName());
    decl.noteValue(lambda);
    decl.setFlag(Declaration.IS_CONSTANT|Declaration.STATIC_SPECIFIED);
    decl.setProcedureDecl(true);
    return new ReferenceExp(decl);
  }

  /**
     @return the lambda expression referenced.
  */
  public static LambdaExp dereference(Expression ref)
  {
    return (LambdaExp) ((ReferenceExp) ref).getBinding().getValue();
  }

  public static Expression returnVoid()
  {
    return nice.tools.code.Inline.inline(nice.lang.inline.Return.returnVoid);
  }

  public static Expression returnValue(Expression value, Type type)
  {
    return nice.tools.code.Inline.inline(nice.lang.inline.Return.create(type),
					 value);
  }
}
