/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import mlsub.typing.*;
import mlsub.typing.Constraint;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.AtomicConstraint;

import java.util.*;

import nice.tools.code.Types;
import nice.tools.code.*;

import gnu.expr.Declaration;
import gnu.expr.QuoteExp;

/**
   Abstract syntax for a class definition.

 */
public abstract class NiceClass extends ClassImplementation
{
  abstract public LocatedString getName();

  abstract ClassDefinition getDefinition();

  abstract public boolean isInterface();

  static NiceClass get(TypeConstructor tc)
  {
    ClassDefinition res = ClassDefinition.get(tc);
    if (res != null && res.implementation instanceof NiceClass)
      return ((NiceClass) res.implementation);
    else
      return null;
  }

  public static NiceClass get(mlsub.typing.Monotype type)
  {
    if (! nice.tools.typing.Types.isSure(type))
      return null;

    return get(nice.tools.typing.Types.constructor(type));
  }

  abstract void addConstructorCallSymbol(MethodDeclaration.Symbol sym);

  abstract public void addField(MonoSymbol sym, Expression value, 
     boolean isFinal, boolean isTransient, boolean isVolatile, String docString);

  abstract gnu.expr.Expression getInitializer();

  abstract public gnu.expr.ClassExp createClassExp();

  abstract public gnu.expr.ClassExp getClassExp();

  abstract void precompile();

  /** This native method is redefined for this Nice class. */
  abstract public gnu.expr.Expression addJavaMethod(gnu.expr.LambdaExp method);

  /**
     Returns an expression to call a super method from outside a class method.

     This is needed because the JVM restricts call to a specific implementation
     to occur inside a method of the same class. So this generates a stub class
     method that calls the desired super method, and return a reference to this
     stub.
  */
  abstract gnu.expr.Expression callSuperMethod(gnu.bytecode.Method superMethod);

  abstract public Definition importMethod(gnu.bytecode.Method method);

  abstract public mlsub.typing.Monotype[] getTypeParameters();
}
