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

    if (resolvedType instanceof MonotypeVar)
      {
	MonotypeVar res = (MonotypeVar) resolvedType;
	TypeConstructor tc = new TypeConstructor("nullness", PrimitiveType.maybeTC.variance, false, false);
	MonotypeVar raw = new MonotypeVar(res.getName()+"raw");
	MonotypeConstructor eq = new MonotypeConstructor(tc, new Monotype[]{raw});
	cst = new Constraint(new TypeSymbol[]{tc, raw},
			     new AtomicConstraint[]{
			       new TypeConstructorLeqCst(tc, PrimitiveType.maybeTC),
			       new MonotypeLeqCst(eq, res),
			       new MonotypeLeqCst(res, eq)});
	monotype = bossa.syntax.Monotype.maybe(raw);
      }
    else
      {
	if (!(resolvedType instanceof TypeConstructor))
	  User.error(ident, ident + " should be a class");
    
	TypeConstructor tc = (TypeConstructor) resolvedType;
	monotype = new MonotypeConstructor(tc, MonotypeVar.news(tc.arity()));
	if (Types.isPrimitive(tc))
	  monotype = bossa.syntax.Monotype.sure(monotype);
	else
	  monotype = bossa.syntax.Monotype.maybe(monotype);	  

	cst = Constraint.True;
      }
    
    for (int i = 0; i<knownDimensions.length + unknownDimensions; i++)
      monotype = bossa.syntax.Monotype.sure(new MonotypeConstructor
	(PrimitiveType.arrayTC, new Monotype[]{monotype}));
    
    // set the Expression type
    type = new Polytype(cst, monotype);
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
      ("new " + ident + Util.map("[", "]", "]", knownDimensions));

    for(int i=0; i<unknownDimensions; i++)
      res.append("[]");
    
    return res.toString();
  }

  // We need to keep the ident for toString, which is used when dumping package interface.
  final TypeIdent ident;

  Expression[] knownDimensions;
  private int unknownDimensions;
}
