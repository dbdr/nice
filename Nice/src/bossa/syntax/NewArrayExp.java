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

package bossa.syntax;

import bossa.util.*;
import java.util.*;

import gnu.bytecode.*;

import nice.tools.typing.Types;

import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.MonotypeVar;
import mlsub.typing.Polytype;
import mlsub.typing.Constraint;
import mlsub.typing.AtomicConstraint;
import mlsub.typing.MonotypeLeqCst;
import mlsub.typing.TypeConstructorLeqCst;
import mlsub.typing.TypeConstructor;
import mlsub.typing.TypeSymbol;

/**
   Allocate a new instance of an Array type.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class NewArrayExp extends Expression
{
  public NewArrayExp(TypeIdent type, 
		     List /* of Expression */ knownDimensions, 
		     int unknownDimensions)
  {
    this.ident = type;
    this.knownDimensions = toArray(knownDimensions);
    this.unknownDimensions = unknownDimensions;
  }

  void resolveTC(TypeMap typeScope)
  {
    resolvedType = ident.resolveToTypeSymbol(typeScope);
  }
  
  private TypeSymbol resolvedType;

  void computeType()
  {
    Monotype monotype;
    Constraint cst = Constraint.True;

    TypeConstructor[] nullVars;
    boolean zero = findZero(knownDimensions);
    /* 
       If one of the dimensions is zero, then all unknown dimensions and
       the component type can be safely assumed to be non-optional.
       Because arrays are invariant, we give them a type polymorphic in the
       nullness marker.
    */
    if (zero)
      {
	nullVars = new TypeConstructor[unknownDimensions + 1];
	for (int i = 0; i < nullVars.length; i++)
	  nullVars[i] = new TypeConstructor("n" + i, 
					    PrimitiveType.maybeTC.variance, 
					    false, false);
      }
    else
      nullVars = null;

    MonotypeVar[] typeVars = null;

    // Whether the element are surely not null
    boolean sure = false;

    if (resolvedType instanceof MonotypeVar)
      {
	MonotypeVar res = (MonotypeVar) resolvedType;

        if (res.getKind() == mlsub.typing.NullnessKind.instance)
          monotype = Types.rawType(res.equivalent());
        else
          monotype = res;
      }
    else if (resolvedType == mlsub.typing.TopMonotype.instance)
      {
        monotype = mlsub.typing.TopMonotype.instance;
      }
    else
      {
	if (!(resolvedType instanceof TypeConstructor))
	  User.error(ident, ident + " should be a class");

	TypeConstructor tc = (TypeConstructor) resolvedType;

        typeVars = MonotypeVar.news(tc.arity());
	monotype = new MonotypeConstructor(tc, typeVars);

	if (Types.isPrimitive(tc))
          sure = true;
      }

    // Add the nullness marker to the element type.
    if (sure)
      monotype = bossa.syntax.Monotype.sure(monotype);
    else if (nullVars == null)
      monotype = bossa.syntax.Monotype.maybe(monotype);
    else
      {
        monotype = MonotypeConstructor.apply
          (nullVars[nullVars.length - 1], monotype);
      }

    if (nullVars != null || typeVars != null)
      {
        if (nullVars == null)
          cst = new Constraint(typeVars, null);
        else if (typeVars == null)
          cst = new Constraint(nullVars, null);
        else
          {
            TypeSymbol[] binders = new TypeSymbol[nullVars.length + typeVars.length];
            System.arraycopy(nullVars, 0, binders, 0, nullVars.length);
            System.arraycopy(typeVars, 0, binders, nullVars.length, typeVars.length);
            cst = new Constraint(binders, null);
          }
      }

    for (int i = 0; i < unknownDimensions; i++)
      {
	monotype = new MonotypeConstructor
	  (PrimitiveType.arrayTC, new Monotype[]{monotype});
	if (nullVars != null)
	  monotype = MonotypeConstructor.apply(nullVars[i], monotype);
	else
	  // Unknown dimensions are not initialized: give an option type.
	  monotype = bossa.syntax.Monotype.maybe(monotype);
      }

    // Known dimensions are initialized: give a sure type.
    for (int i = 0; i < knownDimensions.length; i++)
      monotype = bossa.syntax.Monotype.sure
	(MonotypeConstructor.apply(PrimitiveType.arrayTC, monotype));
    
    // set the Expression type
    type = new Polytype(cst, monotype);
  }

  private static boolean findZero(Expression[] exps)
  {
    for (int i = 0; i < exps.length; i++)
      if (exps[i].isZero())
	return true;
    return false;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    Type t = nice.tools.code.Types.javaType(type);
    
    return new gnu.expr.ApplyExp
      (new nice.tools.code.MultiArrayNewProc((ArrayType) t, 
					     knownDimensions.length),
       Expression.compile(knownDimensions));
  }
  
  public String toString()
  {
    StringBuffer res = new StringBuffer
      ("new " + ident + Util.map("[", "][", "]", knownDimensions));

    for(int i=0; i<unknownDimensions; i++)
      res.append("[]");
    
    return res.toString();
  }

  // We need to keep the ident for toString, which is used when dumping package interface.
  final TypeIdent ident;

  Expression[] knownDimensions;
  private int unknownDimensions;
}
