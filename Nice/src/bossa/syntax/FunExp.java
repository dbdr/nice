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
    this.formals=addChildren(formals);
    this.constraint=cst;
    this.body = child(body);
  }

  void resolve()
  {
    cst = constraint.resolveToLowlevel();
    constraint = null;
  }
  
  void computeType()
  {
    try{
      Constraint.assert(cst);
    }
    catch(mlsub.typing.TypingEx e){
      User.error(this,"functional expression is ill-typed");
    }
    
    if(body instanceof ReturnStmt)
      inferredReturnType = ((ReturnStmt) body).returnType();
    else if(body instanceof Block)
      {
	inferredReturnType = ((Block) body).getType();
	if (inferredReturnType == null)
	  User.error(this,
		     "Not implemented: the last statement of "+this+
		     "must be a return statement");
      }
    else
      {
	Internal.error(this,
		       "Body of lambda expression is not of known form");
	inferredReturnType = null;
      }
    
    type = new Polytype
      (Constraint.and(cst, inferredReturnType.getConstraint()),
       new FunType(MonoSymbol.getMonotype(formals), inferredReturnType.getMonotype()));
  }

  private Polytype inferredReturnType;
  Polytype inferredReturnType()
  {
    getType();
    return inferredReturnType;
  }

  public Monotype getReturnType()
  {
    return null;
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  private gnu.expr.BlockExp blockExp;
  public gnu.expr.BlockExp getBlock() { return blockExp; }

  public gnu.expr.Expression compile()
  {
    //if(Debug.codeGeneration)
    //Debug.println("Compiling "+this);
    
    gnu.expr.LambdaExp res = new gnu.expr.LambdaExp();

    blockExp = new gnu.expr.BlockExp
      (nice.tools.code.Types.javaType(getType()));

    res.min_args = res.max_args = formals.size();
    
    res.setCanRead(true);
    res.outer = Statement.currentScopeExp;
    Statement.currentScopeExp = res;       // push
    
    for(Iterator i=formals.iterator();
	i.hasNext();)
      {
	MonoSymbol s = (MonoSymbol) i.next();
	
	gnu.expr.Declaration decl = 
	  res.addDeclaration(s.name.toString(), 
			     //s.getMonotype().getJavaType()
			     // Since a applyN method will be produced,
			     // we must forget about the types... :-(
			     gnu.bytecode.Type.pointer_type
			     );

	decl.setParameter(true);
	decl.noteValue(null);
	s.setDeclaration(decl);
      }
    
    res.body = blockExp;
    blockExp.setBody(body.generateCode());

    Statement.currentScopeExp = res.outer; // pop
    
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
  
  List /* of MonoSymbol */ formals;
  bossa.syntax.Constraint constraint;
  Constraint cst;
  Statement body;
}
