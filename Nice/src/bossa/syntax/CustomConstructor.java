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
    (LocatedString className, Constraint cst, FormalParameters params, 
     Block body)
  {
    return new SourceCustomConstructor(className, cst, params, body);
  }

  CustomConstructor(LocatedString className, Constraint cst, 
                    FormalParameters params)
  {
    super(new LocatedString("<init>", className.location()), cst, 
	  returnType(className, cst), params, Contract.noContract);
  }

  CustomConstructor(NiceClass def, FormalParameters parameters)
  {
    super(new LocatedString("<init>", def.definition.location()), 
          def.definition.classConstraint == null ? 
          null : def.definition.classConstraint.shallowClone(), 
	  returnType(def.definition), parameters, 
          Contract.noContract);
    classe = def;
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
            return getInitializationCode(true);
          }
        });
  }

  private static Monotype returnType(LocatedString className, Constraint cst)
  {
    TypeIdent classe = new TypeIdent(className);
    classe.nullness = Monotype.sure;
    
    if (cst == Constraint.True)
      return classe;

    TypeSymbol[] syms = cst.getBinderArray();
    Monotype[] params = new Monotype[syms.length];

    for (int i = 0; i < syms.length; i++)
      {
        if (! (syms[i] instanceof mlsub.typing.MonotypeVar))
          User.error(classe, syms[i] + " is not a type");
        params[i] = Monotype.create((MonotypeVar) syms[i]);
      }

    Monotype res = new MonotypeConstructor
      (classe, new TypeParameters(params), classe.location());
    res.nullness = Monotype.sure;
    return res;
  }

  private static Monotype returnType(ClassDefinition def)
  {
    mlsub.typing.Monotype res = Monotype.sure
      (new mlsub.typing.MonotypeConstructor(def.tc, def.getTypeParameters()));
    return Monotype.create(res);
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

  abstract gnu.expr.Expression getInitializationCode(boolean implicitThis);

  NiceClass classe;

  /****************************************************************
   * A custom constructor defined in a source program.
   ****************************************************************/

  static class SourceCustomConstructor extends CustomConstructor
  {
    SourceCustomConstructor(LocatedString className, Constraint cst,
                            FormalParameters params, Block body)
    {
      super(className, cst, params);

      this.className = className;
      this.body = body;
    }

    void resolve()
    {
      TypeConstructor tc = Node.getGlobalTypeScope().globalLookup(className);
      TypeConstructors.addConstructor(tc, this);
      classe = NiceClass.get(tc);

      if (classe == null)
        User.error(this, 
                   "It is impossible to add a constructor to class " + tc);

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

    private Map map(TypeSymbol[] source, mlsub.typing.Monotype[] destination)
    {
      Map res = new HashMap(source.length);

      for (int i = 0; i < source.length; i++)
        res.put(source[i].toString(), Monotype.create(destination[i]));

      return res;
    }

    protected gnu.expr.Expression computeCode()
    {
      ConstructorExp lambda = Gen.createCustomConstructor
        ((ClassType) javaReturnType(), javaArgTypes(), getSymbols());

      Gen.setMethodBody(lambda, body.generateCode());
      classe.getClassExp().addMethod(lambda);
      
      // In the bytecode, we want to use the same type parameter names
      // as the class definition, even if the source of this custom constructor
      // renamed them locally.
      mlsub.typing.Constraint cst = getType().getConstraint();
      if (mlsub.typing.Constraint.hasBinders(cst))
        parameters.substitute
          (map(cst.binders(), classe.definition.getTypeParameters()));

      lambda.addBytecodeAttribute(parameters.asBytecodeAttribute());
      initializationCode = 
        new QuoteExp(new InitializeProc(lambda));
      initializationCodeImplicitThis = 
        new QuoteExp(new InitializeProc(lambda, true));

      return new QuoteExp(new InstantiateProc(lambda));
    }

    gnu.expr.Expression getConstructorInvocation(boolean omitDefaults)
    {
      getCode();
      return initializationCode;
    }

    gnu.expr.Expression getInitializationCode(boolean implicitThis)
    {
      getCode();
      if (implicitThis)
        return initializationCodeImplicitThis;
      else
        return initializationCode;
    }

    LocatedString className;
    Statement body;
    gnu.expr.Expression initializationCode;
    gnu.expr.Expression initializationCodeImplicitThis;
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
      TypeConstructors.addConstructor(classe.definition.tc, this);
    }

    void resolve()
    {
      addConstructorCallSymbol();
    }

    protected gnu.expr.Expression computeCode()
    {
      int dummyArgs = method.arg_types.length - arity;
      return new QuoteExp(new InstantiateProc(method, dummyArgs));
    }

    gnu.expr.Expression getConstructorInvocation(boolean omitDefaults)
    {
      int dummyArgs = method.arg_types.length - arity;
      return new QuoteExp(new InitializeProc(method, false, dummyArgs));
    }

    gnu.expr.Expression getInitializationCode(boolean implicitThis)
    {
      int dummyArgs = method.arg_types.length - arity;
      return new QuoteExp(new InitializeProc(method, implicitThis, dummyArgs));
    }

    private Method method;
  }
}
