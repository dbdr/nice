/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import java.util.*;
import mlsub.typing.*;
import gnu.expr.*;
import gnu.bytecode.ClassType;
import gnu.bytecode.Method;
import gnu.bytecode.Attribute;
import gnu.bytecode.MiscAttr;
import nice.tools.code.Gen;

/**
   A user defined constructor.
 */

public abstract class CustomConstructor extends UserOperator
{
  public static CustomConstructor make
    (LocatedString className, FormalParameters params, Block body)
  {
    return new SourceCustomConstructor(className, params, body);
  }

  CustomConstructor(LocatedString className, FormalParameters params)
  {
    super(new LocatedString("<init>", className.location()), Constraint.True, 
	  returnType(className), params, Contract.noContract);
  }

  void addConstructorCallSymbol()
  {
    mlsub.typing.Polytype type = new mlsub.typing.Polytype
      (getType().getConstraint(), 
       new mlsub.typing.FunType(getArgTypes(), PrimitiveType.voidType));
    classe.addConstructorCallSymbol
      (new MethodDeclaration.Symbol(name, type) {
          gnu.expr.Expression compileInCallPosition()
          {
            return getInitializationCode();
          }
        });
  }

  CustomConstructor(NiceClass def, FormalParameters parameters)
  {
    this(def.definition.getName(), parameters);
    classe = def;
  }

  private static Monotype returnType(LocatedString className)
  {
    Monotype res = new TypeIdent(className);
    res.nullness = Monotype.sure;
    return res;
  }

  public void printInterface(java.io.PrintWriter s)
  {
    // Constructors are not printed, they are loaded from the bytecode.
  }

  public void compile()
  {
    // Make sure the constructor is generated.
    getCode();
  }

  abstract gnu.expr.Expression getInitializationCode();

  NiceClass classe;

  /****************************************************************
   * A custom constructor defined in a source program.
   ****************************************************************/

  static class SourceCustomConstructor extends CustomConstructor
  {
    SourceCustomConstructor(LocatedString className, FormalParameters params, 
                            Block body)
    {
      super(className, params);

      this.className = className;
      this.body = body;
    }

    void resolve()
    {
      TypeConstructor tc = Node.getGlobalTypeScope().globalLookup(className);
      TypeConstructors.addConstructor(tc, this);
      classe = NiceClass.get(tc);

      addConstructorCallSymbol();

      // Save the scopes, since we need them later, but they get null'ed.
      thisScope = scope;
      thisTypeScope = typeScope;
    }

    private VarScope thisScope;
    private TypeScope thisTypeScope;

    void resolveBody()
    {
      resolveThis((Block) body);
      body = bossa.syntax.dispatch.analyse
        (body, thisScope, thisTypeScope, false);
    }

    private void resolveThis(Block block)
    {
      Statement last = block.statements[block.statements.length - 1];
      if (last instanceof Block)
        {
          resolveThis((Block) last);
          return;
        }

      try {
        CallExp call = (CallExp) ((ExpressionStmt) last).exp;
        IdentExp ident = (IdentExp) call.function;
        if (! call.function.toString().equals("this"))
          User.error(this, 
                     "The last statement must be a call to 'this' constructor");

        Location loc = ident.location();
        call.function = new OverloadedSymbolExp
          (classe.getConstructorCallSymbols(), FormalParameters.thisName);
        call.function.setLocation(loc);
      }
      catch(ClassCastException ex) {
        User.error(this, 
                   "The last statement must be a call to 'this' constructor");
      }
    }

    void innerTypecheck() throws TypingEx
    {
      super.innerTypecheck();

      bossa.syntax.dispatch.typecheck(body);
    }

    protected gnu.expr.Expression computeCode()
    {
      ConstructorExp lambda = Gen.createCustomConstructor
        ((ClassType) javaReturnType(), javaArgTypes(), getSymbols());

      Gen.setMethodBody(lambda, body.generateCode());
      classe.getClassExp().addMethod(lambda);
      lambda.addBytecodeAttribute(parameters.asBytecodeAttribute());
      initializationCode = new QuoteExp(new InitializeProc(lambda, true));

      return new QuoteExp(new InstantiateProc(lambda));
    }

    gnu.expr.Expression getInitializationCode()
    {
      getCode();
      return initializationCode;
    }

    LocatedString className;
    Statement body;
    gnu.expr.Expression initializationCode;
  }

  /****************************************************************
   * Loading compiled custom constructors.
   ****************************************************************/

  public static CustomConstructor load(NiceClass def, Method method)
  {
    if (! method.isConstructor())
      return null;

    MiscAttr attr = (MiscAttr) Attribute.get(method, "parameters");
    if (attr == null)
      return null;

    return new ImportedCustomConstructor(def, method, attr);
  }

  static class ImportedCustomConstructor extends CustomConstructor
  {
    ImportedCustomConstructor(NiceClass def, Method method, MiscAttr attr)
    {
      super(def, FormalParameters.readBytecodeAttribute(attr));
      this.method = method;
    }

    void resolve()
    {
      TypeConstructors.addConstructor(classe.definition.tc, this);
      addConstructorCallSymbol();
    }

    protected gnu.expr.Expression computeCode()
    {
      int dummyArgs = method.arg_types.length - arity;
      return new QuoteExp(new InstantiateProc(method, dummyArgs));
    }

    gnu.expr.Expression getInitializationCode()
    {
      int dummyArgs = method.arg_types.length - arity;
      return new QuoteExp(new InitializeProc(method, true, dummyArgs));
    }

    private Method method;
  }
}
