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

import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.MonotypeVar;
import mlsub.typing.Polytype;
import mlsub.typing.Constraint;
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
    this.ti = type;
    this.knownDimensions = expTabChildren(knownDimensions);
    this.unknownDimensions = unknownDimensions;
  }

  void resolveTC(TypeMap typeScope)
  {
    if (tc != null)
      return;
    
    TypeSymbol ts = ti.resolveToTypeSymbol(typeScope);
    Monotype monotype;
    
    if (ts instanceof MonotypeVar)
      monotype = (MonotypeVar) ts;
    else
      {
	if (!(ts instanceof TypeConstructor))
	  User.error(ti, ti + " should be a class");
    
	tc = (TypeConstructor) ts;
	monotype = new MonotypeConstructor(tc, MonotypeVar.news(tc.arity()));
      }
    
    for (int i = 0; i<knownDimensions.length + unknownDimensions; i++)
      monotype = new MonotypeConstructor
	(ConstantExp.arrayTC, new Monotype[]{monotype});
    
    // set the Expression type
    type = new Polytype(Constraint.True, monotype);

    ti = null;
  }
  
  void computeType()
  {
  }
  
  void typecheck()
  {
    for (int i=0; i<knownDimensions.length; i++)
      try{
	mlsub.typing.Typing.leq(knownDimensions[i].getType(),
				ConstantExp.intPolytype);
      }
      catch(mlsub.typing.TypingEx e){
	User.error(knownDimensions[i],
		   knownDimensions[i] + " should be an integer");
      }
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
      ("new " +
       (ti != null ? ti.toString() : 
	tc != null ? tc.toString() : type.toString()) +
       Util.map("[", "]", "]", knownDimensions));

    for(int i=0; i<unknownDimensions; i++)
      res.append("[]");
    
    return res.toString();
  }

  TypeIdent ti;
  TypeConstructor tc;

  Expression[] knownDimensions;
  private int unknownDimensions;
}
