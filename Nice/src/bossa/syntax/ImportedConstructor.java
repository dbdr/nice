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

import gnu.expr.*;
import gnu.bytecode.Type;
import gnu.bytecode.ClassType;
import gnu.bytecode.Method;
import gnu.bytecode.Attribute;
import gnu.bytecode.MiscAttr;

import java.util.*;
import bossa.util.User;

/**
   A constructor imported from a compiled package.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class ImportedConstructor extends Constructor
{
  public static ImportedConstructor load(NiceClass def, Method method)
  {
    if (! method.isConstructor())
      return null;

    MiscAttr attr = (MiscAttr) Attribute.get(method, "parameters");
    if (attr == null)
      return null;

    return new ImportedConstructor(def, method, attr);
  }

  ImportedConstructor(NiceClass def, Method method, MiscAttr attr)
  {
    super(def,
          Attribute.get(method, "default") != null,
          def.definition.location(),
          FormalParameters.readBytecodeAttribute
            (attr, JavaClasses.compilation.parser),
          def.definition.classConstraint == null ?
          null : def.definition.classConstraint.shallowClone(),
          returnType(def.definition));

    this.method = method;
    TypeConstructors.addConstructor(classe.definition.tc, this);
  }

  private static Monotype returnType(ClassDefinition def)
  {
    mlsub.typing.Monotype res = Monotype.sure
      (new mlsub.typing.MonotypeConstructor(def.tc, def.getTypeParameters()));
    return Monotype.create(res);
  }

  void doResolve()
  {
    // Make sure the type is computed first
    removeChild(getSymbol());
    getSymbol().doResolve();

    super.doResolve();

    addConstructorCallSymbol();
  }

  void resolve()
  {
    super.resolve();

    // Adding the constraint in the type scope. It can be useful for
    // the default values of the formal parameters
    // (e.g. an anonymous function refering to a type parameter).
    mlsub.typing.Constraint cst = getType().getConstraint();
    if (mlsub.typing.Constraint.hasBinders(cst))
      try {
        typeScope.addSymbols(cst.binders());
      } catch (TypeScope.DuplicateName ex) {
        User.error(this, "Double declaration of the same type parameter");
      }
  }

  protected gnu.expr.Expression computeCode()
  {
    int dummyArgs = method.arg_types.length - arity;
    return new QuoteExp(new InstantiateProc(method, dummyArgs));
  }

  gnu.expr.Expression getConstructorInvocation(boolean omitDefaults)
  {
    int dummyArgs = method.arg_types.length - arity;
    Method calledMethod = omitDefaults ? getMethodUsingDefaults() : method;
    return new QuoteExp(new InitializeProc(calledMethod, false, dummyArgs));
  }

  gnu.expr.Expression getInitializationCode(boolean implicitThis)
  {
    int dummyArgs = method.arg_types.length - arity;
    return new QuoteExp(new InitializeProc(method, implicitThis, dummyArgs));
  }

  private Method method;

  /**
      Return the Method for this constructor with the optional parameters
      left out.
  */
  private Method getMethodUsingDefaults()
  {
    Type[] fullArgTypes = method.arg_types;
    List argTypes = new LinkedList();

    for (int i = 0; i < parameters.size; i++)
      {
        if (parameters.hasDefaultValue(i))
          continue;

        argTypes.add(fullArgTypes[i]);
      }

    Type[] argTypesArray = (Type[]) argTypes.toArray
      (new Type[argTypes.size()]);

    return method.getDeclaringClass().getDeclaredMethod
      ("<init>", argTypesArray);
  }
}
