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

import java.util.*;
import bossa.util.*;

import mlsub.typing.Polytype;
import mlsub.typing.Monotype;
import mlsub.typing.FunType;
import mlsub.typing.Constraint;

/**
   A functional abstraction expression.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class FunExp extends Expression implements Function
{
  public FunExp(bossa.syntax.Constraint cst, List formals, Statement body)
  {
    this.formals = new MonoSymbol[formals.size()];
    for (int i = 0; i < this.formals.length; i++)
    {
      MonoSymbol m = (MonoSymbol) formals.get(i);
      this.formals[i] = m;
    }
    
    this.constraint = cst;
    this.body = body;
  }

  FunExp(bossa.syntax.Constraint cst, MonoSymbol[] formals, Statement body)
  {
    this.formals = formals;
    this.constraint = cst;
    this.body = body;
  }

  public void checkReturnedType(mlsub.typing.Polytype returned)
    throws Function.WrongReturnType
  {
    if (inferredReturnType == null)
      inferredReturnType = returned;
    else
      inferredReturnType = Polytype.union(inferredReturnType, returned);

    if (type != null)
      Internal.error("Return statement discovered after computation of the type");
  }

  private boolean alwaysReturns;
  void setAlwaysReturns(boolean value) { alwaysReturns = value; }

  void computeType()
  {
    if (inferredReturnType == null)
      // There is not return statement in the function.
      inferredReturnType = PrimitiveType.voidPolytype;
    else
      if (! alwaysReturns && 
	  ! nice.tools.code.Types.isVoid(inferredReturnType))
	throw User.error(this, "Missing return statement");

    Monotype t = new FunType(MonoSymbol.getMonotype(formals), 
			     inferredReturnType.getMonotype());
    type = new Polytype
      (Constraint.and(cst, inferredReturnType.getConstraint()),
       bossa.syntax.Monotype.sure(t));
  }

  private Polytype inferredReturnType;
  Polytype inferredReturnType()
  {
    getType();
    return inferredReturnType;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    gnu.expr.LambdaExp res = nice.tools.code.Gen.createMethod
      (null, 
       nice.tools.code.Types.javaType(MonoSymbol.getMonotype(formals)), 
       nice.tools.code.Types.javaType(inferredReturnType()), 
       formals, false);
    nice.tools.code.Gen.setMethodBody(res, body.generateCode());
    return res;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    return 
      (constraint == null ? mlsub.typing.Constraint.toString(cst) : "")
      + "fun ("
      + Util.map("",", ","",formals)
      + ") => "
      + body
      ;
  }
  
  MonoSymbol[] formals;
  bossa.syntax.Constraint constraint;
  Constraint cst;
  Statement body;
}
