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
import gnu.bytecode.Type;
import gnu.bytecode.ClassType;
import gnu.bytecode.Method;
import gnu.bytecode.Attribute;
import gnu.bytecode.MiscAttr;
import nice.tools.code.Gen;
import nice.tools.typing.PrimitiveType;

/**
   A user defined constructor, in a source program.
 */

public class CustomConstructor extends UserOperator
{
  public static CustomConstructor make
    (LocatedString className, Constraint cst, FormalParameters params,
     Statement body)
  {
    return new CustomConstructor(className, cst, params, body);
  }

  CustomConstructor(LocatedString className, Constraint cst,
                    FormalParameters params, Statement body)
  {
    super(new LocatedString("<init>", className.location()), cst,
	  returnType(className, cst), params, Contract.noContract);

    this.className = className;
    this.body = body;
  }

  NiceClass classe;

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

  void addConstructorCallSymbol()
  {
    mlsub.typing.Polytype type = new mlsub.typing.Polytype
      (getType().getConstraint(),
       new mlsub.typing.FunType(getArgTypes(), PrimitiveType.voidType));
    classe.addConstructorCallSymbol(
	dispatch.createConstructorCallSymbol(this, name, type));
  }

  void resolve()
  {
    super.resolve();

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
    bossa.syntax.dispatch.resolveCCThis(body, this, classe);
    body = bossa.syntax.dispatch.analyseMethodBody
      (body, thisScope, thisTypeScope, getSymbols(), false);
  }

  void innerTypecheck() throws TypingEx
  {
    super.innerTypecheck();

    bossa.syntax.dispatch.typecheck(body);
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

  private Map map(TypeSymbol[] source, mlsub.typing.Monotype[] destination)
  {
    Map res = new HashMap(source.length);

    for (int i = 0; i < source.length; i++)
      res.put(source[i].toString(), Monotype.create(destination[i]));

    return res;
  }

  private boolean generatingCode = false;

  protected gnu.expr.Expression computeCode()
  {
    if (generatingCode)
      User.error(this, "recursive custom constructor calls not allowed");

    generatingCode = true;

    ConstructorExp lambda = Gen.createCustomConstructor
      ((ClassType) javaReturnType(), javaArgTypes(), getSymbols());

    Gen.setMethodBody(lambda, body.generateCode());
    classe.getClassExp().addMethod(lambda);

    generatingCode = false;

    // In the bytecode, we want to use the same type parameter names
    // as the class definition, even if the source of this custom constructor
    // renamed them locally.
    mlsub.typing.Constraint cst = getType().getConstraint();
    if (mlsub.typing.Constraint.hasBinders(cst))
      parameters.substitute
        (map(cst.binders(), classe.getTypeParameters()));

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
