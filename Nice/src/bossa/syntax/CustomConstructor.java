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
import nice.tools.code.Gen;

/**
   A user defined constructor.
 */

public class CustomConstructor extends UserOperator
{
  public CustomConstructor(LocatedString className, FormalParameters params, Block body)
  {
    super(new LocatedString("<init>", className.location()), Constraint.True, 
	  returnType(className), params, Contract.noContract);

    this.className = className;
    this.body = body;
  }

  private static Monotype returnType(LocatedString className)
  {
    Monotype res = new TypeIdent(className);
    res.nullness = Monotype.sure;
    return res;
  }

  void resolve()
  {
    TypeConstructor tc = Node.getGlobalTypeScope().globalLookup(className);
    TypeConstructors.addConstructor(tc, this);
    classe = NiceClass.get(tc);

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

  public void printInterface(java.io.PrintWriter s)
  {
    s.print("new " + className + "(" + parameters + ");\n");
  }

  public void compile()
  {
    // Make sure the constructor is generated.
    getCode();
  }

  protected gnu.expr.Expression computeCode()
  {
    if (classe.definition.inInterfaceFile())
      {
        return new QuoteExp(classe.getClassExp().getClassType().getDeclaredMethod
          ("<init>", javaArgTypes()));
      }

    ConstructorExp lambda = Gen.createCustomConstructor
      ((ClassType) javaReturnType(), javaArgTypes(), getSymbols());

    Gen.setMethodBody(lambda, body.generateCode());
    classe.getClassExp().addMethod(lambda);

    return new QuoteExp(new InstantiateProc(lambda));
  }

  LocatedString className;
  Statement body;
  NiceClass classe;
}
