/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import mlsub.typing.Polytype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.TypeConstructor;
import mlsub.typing.MonotypeVar;

/**
   A type used as an expression.

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

public class TypeConstantExp extends ConstantExp
{
  public TypeConstantExp(LocatedString name)
  {
    super(null, null, name, name.toString(), name.location());
  }

  gnu.bytecode.ClassType staticClass()
  {
    // If this is a '<name>.class' expression, do not consider it as a
    // qualified prefix.
    if (isExpression)
      return null;

    return (gnu.bytecode.ClassType) value;
  }

  void setRepresentedType(Polytype type, gnu.bytecode.Type bytecodeType)
  {
    this.value = bytecodeType;

    this.representedType = type.getMonotype();

    this.type = new Polytype
      (type.getConstraint(),
       Monotype.sure
       (new MonotypeConstructor
        (PrimitiveType.classTC,
         new mlsub.typing.Monotype[]{ type.getMonotype() })));
  }

  protected gnu.expr.Expression compile()
  {
    if (isLiteral)
      return super.compile();

    gnu.bytecode.Type type = (gnu.bytecode.Type) value;
    String representation = type instanceof gnu.bytecode.ArrayType ?
      type.getSignature().replace('/', '.') :
      type.getName();

    return new gnu.expr.ApplyExp
      (forName,
       new gnu.expr.Expression[]{
         new gnu.expr.QuoteExp(representation)});
  }

  static final gnu.bytecode.Method forName =
    gnu.bytecode.ClassType.make("java.lang.Class").getDeclaredMethod("forName", 1);

  TypeConstructor getTC()
  {
    return nice.tools.typing.Types.rawType(representedType).head();
  }

  mlsub.typing.Monotype representedType;

  public boolean isExpression = false;
  public boolean isLiteral = false;

  static Polytype universalPolytype(TypeConstructor tc, boolean sure)
  {
    MonotypeVar[] vars = MonotypeVar.news(tc.arity());
    mlsub.typing.Monotype type = new MonotypeConstructor(tc, vars);
    return new Polytype
      (vars == null ? null : new mlsub.typing.Constraint(vars, null),
       sure ? Monotype.sure(type) : Monotype.maybe(type));
  }

  /****************************************************************
   * Type literals
   ****************************************************************/

  /**
     @return an Expression representing ident as a type or package literal.
  */
  static Expression create(LocatedString ident)
  {
    return create(null, ident);
  }

  /**
     @return an Expression representing [root].[name]
  */
  static Expression create(PackageExp root, LocatedString name)
  {
    String fullName = name.toString();
    if (root != null)
      fullName = root.name.append(".").append(fullName).toString();

    TypeConstructor tc =
      Node.getGlobalTypeScope().globalLookup(fullName, name.location());

    if(tc != null)
      {
	gnu.bytecode.Type type = nice.tools.code.Types.javaType(tc);

	// type might not be a class
	// for instance if the ident was "int"
	if (type instanceof gnu.bytecode.ClassType)
	  {
	    TypeConstantExp res = new TypeConstantExp(name);
            res.setRepresentedType(universalPolytype(tc, true), type);
	    res.setLocation(root == null ? name.location() : root.location());
	    return res;
	  }
      }

    if (root != null)
      // name has been appended to root's name
      return root;

    root = new PackageExp(fullName);
    root.setLocation(name.location());
    return root;
  }
}
