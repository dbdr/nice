/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : NewArrayExp.java
// Created : Mon Aug 28 13:37:29 2000 by Daniel Bonniot
//$Modified: Thu Aug 31 16:29:39 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

import gnu.bytecode.*;

import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.MonotypeVar;
import mlsub.typing.Polytype;
import mlsub.typing.Constraint;

/**
   Allocate a new instance of an Array type.
   
   @author Daniel Bonniot
 */

public class NewArrayExp extends Expression
{
  public NewArrayExp(TypeIdent type, 
		     List /* of Expression */ knownDimensions, 
		     int unknownDimensions)
  {
    this.ti = type;

    knownDimensions = expChildren(knownDimensions);
    this.knownDimensions = (Expression[]) 
      knownDimensions.toArray(new Expression[knownDimensions.size()]);
    
    this.unknownDimensions = unknownDimensions;
  }

  // we cannot assume resolveTC is called from findJavaClasses
  // for instance, if this new exp belongs to a global variable definition
  // it is not the case.
  private void resolveTC()
  {
    if (tc != null)
      return;
    
    tc = ti.resolveToTC(typeScope);
    ti = null;

    if (!TypeConstructors.constant(tc))
      User.error(this,
		 tc + " should denote a known class");
  }
  
  void findJavaClasses()
  {
    super.findJavaClasses();
    ti.resolveToTC(typeScope);
  }
  
  void resolve()
  {
    super.resolve();
    resolveTC();
  }

  void computeType()
  {
    resolveTC();
    Monotype t = new MonotypeConstructor(tc, MonotypeVar.news(tc.arity()));
    
    for (int i=0; i<knownDimensions.length + unknownDimensions; i++)
      t = new MonotypeConstructor(ConstantExp.arrayTC, new Monotype[]{t});
    
    type = new Polytype(Constraint.True, t);
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
    Type t = nice.tools.code.Types.javaType(tc);
    int nbDimensions = knownDimensions.length + unknownDimensions;
    
    for (int i = 0; i < nbDimensions; i++)
      t = nice.tools.code.SpecialArray.create(t);
    
    return new gnu.expr.ApplyExp
      (new nice.tools.code.MultiArrayNewProc((ArrayType) t, 
					     knownDimensions.length),
       Expression.compile(knownDimensions));
  }
  
  public String toString()
  {
    StringBuffer res = new StringBuffer
      ("new " +
       (ti == null ? tc.toString() : ti.toString()) +
       " " +
       Util.map("[", "]", "]", knownDimensions));

    for(int i=0; i<unknownDimensions; i++)
      res.append("[]");
    
    return res.toString();
  }

  private TypeIdent ti;
  private mlsub.typing.TypeConstructor tc;

  private Expression[] knownDimensions;
  private int unknownDimensions;
}
