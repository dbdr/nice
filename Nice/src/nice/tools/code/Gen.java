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

  public static Expression instanceOfExp(Expression value, Type ct)
  {
    return Inline.inline(nice.lang.inline.Instanceof.instance, value,
		new QuoteExp(ct));
  }
  
  public static Expression isOfClass(Expression value, Type ct)
  {
    return Inline.inline(new IsOfClassProc(ct), value);
  }

  public static Expression isNullExp(Expression value)
  {
    return Inline.inline(nice.lang.inline.ReferenceOp.create("=="), value,
  		QuoteExp.nullExp);
  }

  public static Expression referenceEqualsExp(Expression value1,
			Expression value2)
  {
    return Inline.inline(nice.lang.inline.ReferenceOp.create("=="), value1,
  		value2);
  }

  public static Expression boolNotExp(Expression value)
  {
    return Inline.inline(nice.lang.inline.BoolNotOp.instance, value);
  }

  public static Expression integerComparison(String kind, Expression value1,
						long value2)
  {
    char type = value1.getType().getSignature().charAt(0);
    if (type == 'J')
      return Inline.inline(nice.lang.inline.CompOp.create("l"+kind), value1,
	new gnu.expr.QuoteExp(new Long(value2), gnu.bytecode.Type.long_type));

    if (type == 'B' || type == 'S' || type == 'I' || type == 'C')
      return Inline.inline(nice.lang.inline.CompOp.create("i"+kind), value1,
	new gnu.expr.QuoteExp(new Long(value2), gnu.bytecode.Type.int_type));

    throw bossa.util.Internal.error("not an integer type");
  }

  public static Expression shortCircuitAnd(Expression value1, Expression value2)
  {
    return Inline.inline(nice.lang.inline.ShortCircuitOp.create("&&"), value1,
			value2);
  }

  public static Expression stringEquals(String value1, Expression value2)
  {
    return new ApplyExp
      (equals, 
       new Expression[] { new QuoteExp(value1, Type.string_type), value2 });
  }

  private static final Expression equals = new QuoteExp
    (new PrimProcedure(Type.pointer_type.getDeclaredMethod("equals", 1)));

  /**
     Create a lambda expression to generate code for the method.

     @param args can be null if there are no arguments
  */
  public static LambdaExp createMethod(String bytecodeName,
				       Type[] argTypes,
				       Type retType,
				       MonoSymbol[] args)
  {
    return createMethod(bytecodeName, argTypes, retType, args, true, false);
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
    return createMethod(bytecodeName, argTypes, retType, args, toplevel, false);
  }

  /**
     Create a lambda expression to generate code for the method.

     @param args can be null if there are no arguments
     @param member true iff this method is a non-static member of
                   the class in argTypes[0]
     @param toplevel If the method can be called from foreign code.
                     This forces its generation even if it is 
		     apparently never called.
  **/
  public static LambdaExp createMethod(String bytecodeName,
				       Type[] argTypes,
				       Type retType,
				       MonoSymbol[] args,
				       boolean toplevel,
				       boolean member)
  {
    LambdaExp res = new LambdaExp();
    createMethod(res, bytecodeName, argTypes, retType, args, toplevel, member, false);
    return res;
  }

  public static ConstructorExp createConstructor
    (Declaration thisDecl, Type[] argTypes, MonoSymbol[] args)
  {
    ConstructorExp res = new ConstructorExp(thisDecl);
    createMethod(res, "<init>", argTypes, Type.void_type, args, 
                 true, false, true);
    return res;
  }

  /**
     Create a lambda expression to generate code for the method.

     @param args can be null if there are no arguments
     @param member true iff this method is a non-static member of
                   the class in argTypes[0]
     @param toplevel If the method can be called from foreign code.
                     This forces its generation even if it is 
		     apparently never called.
  **/
  private static void createMethod
    (LambdaExp lexp, 
     String bytecodeName,
     Type[] argTypes,
     Type retType,
     MonoSymbol[] args,
     boolean toplevel,
     boolean member,
     boolean constructor)
  {
    bytecodeName = nice.tools.code.Strings.escape(bytecodeName);
    int arity = args == null ? 0 : args.length;

    lexp.setReturnType(retType);
    lexp.setName(bytecodeName);
    lexp.min_args = lexp.max_args = member ? arity - 1 : arity;
    lexp.forceGeneration();
    if (toplevel)
      lexp.setCanCall(true);
    if (member)
      lexp.setClassMethod(true);

    // Parameters
    for(int n = 0; n < arity; n++)
      {
	boolean isThis = member && n == 0;
	String parameterName = args[n].getName() == null 
	  ? "anonymous_" + n 
	  : args[n].getName().toString();

	gnu.expr.Declaration d;
	if (isThis)
	  d = new Declaration(parameterName);
	else
	  d = lexp.addDeclaration(parameterName);
	if (argTypes != null)
	  d.setType(argTypes[n]);
	d.noteValue(null);
	args[n].setDeclaration(d, isThis);
      }
  }

  /**
     Create a lambda expression to generate code for the method.

     @param args can be null if there are no arguments
     @param member true iff this method is a non-static member of
                   the class in argTypes[0]
     @param toplevel If the method can be called from foreign code.
                     This forces its generation even if it is 
		     apparently never called.
  **/
  public static LambdaExp createMemberMethod
    (String bytecodeName,
     Type receiver,
     Type[] argTypes,
     Type retType,
     Expression[] params)
  {
    LambdaExp lexp = new LambdaExp();
    bytecodeName = nice.tools.code.Strings.escape(bytecodeName);
    int arity = 1 + (argTypes == null ? 0 : argTypes.length);

    lexp.setReturnType(retType);
    lexp.setName(bytecodeName);
    lexp.min_args = lexp.max_args = arity - 1;
    lexp.forceGeneration();
    lexp.setCanCall(true);
    lexp.setClassMethod(true);

    // Parameters
    for(int n = 0; n < arity; n++)
      {
	boolean isThis = n == 0;
	String parameterName = "anonymous_" + n;

	gnu.expr.Declaration d;
	if (isThis)
	  {
	    d = new Declaration(parameterName);
	    d.context = lexp;
	    d.setType(receiver);
	    params[n] = new ThisExp(d);
	  }
	else
	  {
	    d = lexp.addDeclaration(parameterName);
	    d.setType(argTypes[n - 1]);
	    params[n] = new ReferenceExp(d);
	  }
	d.noteValue(null);
	d.setCanRead(true);
	d.setCanWrite(true);
        
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
    return Inline.inline(nice.lang.inline.Return.instance);
  }

  public static Expression returnValue(Expression value)
  {
    return Inline.inline(nice.lang.inline.Return.instance, value);
  }

  public static void store(Compilation comp, Expression destination,
                           Target target)
  {
    if (destination instanceof ReferenceExp)
      {
        ((ReferenceExp) destination).getBinding().compileStore(comp);
        return;
      }

    // Destination must be a field.
    gnu.bytecode.CodeAttr code = comp.getCode();

    ApplyExp apply = (ApplyExp) destination;
    GetFieldProc fieldProc = (GetFieldProc)
      ((QuoteExp) apply.getFunction()).getValue();
    Field field = fieldProc.getField();

    apply.getArgs()[0].compile(comp, field.getDeclaringClass());
    code.emitSwap();
    code.emitPutField(field);
  }

  /** Creates a LambdaExp that applies a Procedure
   * to the appropriate number of arguments.
   *
   * This is usefull to generate an Expression from a given Procedure.
   */
  public static LambdaExp wrapInLambda(gnu.mapping.Procedure proc)
  {
    int numArgs = proc.minArgs();

    LambdaExp lambda = new LambdaExp();
    lambda.min_args = lambda.max_args = numArgs;

    Expression[] args = new Expression[numArgs];
    for (int i = 0; i < numArgs; i++)
      {
        // The parameters have type Object, since calls to anonymous functions
        // have never precise types.
        // Besides, work would be needed to know the types in the case
        // of inlined methods (macros).
	Declaration decl = lambda.addDeclaration("param__" + i, 
                                                 Type.pointer_type);
	
	args[i] = new ReferenceExp(decl);
      }
    
    lambda.body = new ApplyExp(proc, args);
    return lambda;
  }  
}
