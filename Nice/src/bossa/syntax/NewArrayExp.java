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

import nice.tools.code.Types;

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
    Constraint cst;

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
    
    if (resolvedType instanceof MonotypeVar)
      {
	MonotypeVar res = (MonotypeVar) resolvedType;
	TypeConstructor tc = new TypeConstructor("nullness", PrimitiveType.maybeTC.variance, false, false);
	MonotypeVar raw = new MonotypeVar(res.getName()+"raw");
	MonotypeConstructor eq = MonotypeConstructor.apply(tc, raw);
	TypeSymbol[] vars;
	if (nullVars == null)
	  vars = new TypeSymbol[]{tc, raw};
	else
	  {
	    vars = new TypeSymbol[nullVars.length + 2];
	    System.arraycopy(nullVars, 0, vars, 2, nullVars.length);
	    vars[0] = tc;
	    vars[1] = raw;
	  }
	cst = new Constraint(vars, new AtomicConstraint[]{
	  new TypeConstructorLeqCst(tc, PrimitiveType.maybeTC),
	  new MonotypeLeqCst(eq, res),
	  new MonotypeLeqCst(res, eq)});

	if (nullVars != null)
	  monotype = MonotypeConstructor.apply(nullVars[nullVars.length - 1],
					       raw);
	else
	  monotype = bossa.syntax.Monotype.maybe(raw);
      }
    else
      {
	if (!(resolvedType instanceof TypeConstructor))
	  User.error(ident, ident + " should be a class");
    
	cst = Constraint.True;

	TypeConstructor tc = (TypeConstructor) resolvedType;
	monotype = new MonotypeConstructor(tc, MonotypeVar.news(tc.arity()));
	if (Types.isPrimitive(tc))
	  monotype = bossa.syntax.Monotype.sure(monotype);
	else if (nullVars != null)
	  {
	    monotype = MonotypeConstructor.apply(nullVars[nullVars.length - 1],
						 monotype);
	    cst = new Constraint(nullVars, null);
	  }
	else
	  monotype = bossa.syntax.Monotype.maybe(monotype);	  
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
    Type t = Types.javaType(type);
    
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
