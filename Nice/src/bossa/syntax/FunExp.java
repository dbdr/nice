/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : FunExp.java
// Created : Mon Jul 12 15:09:50 1999 by bonniot
//$Modified: Thu May 25 14:27:02 2000 by Daniel Bonniot $
// Description : A functional expression

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class FunExp extends Expression implements Function
{
  public FunExp(Constraint cst, List formals, Statement body)
  {
    this.formals=addChildren(formals);
    this.constraint=cst;
    this.body = child(body);

    addChild(constraint);
  }

  void computeType()
  {
    try{
      constraint.assert();
    }
    catch(bossa.typing.TypingEx e){
      User.error(this,"functional expression is ill-typed");
    }
    
    Polytype returnType;

    if(body instanceof ReturnStmt)
      returnType = ((ReturnStmt) body).returnType();
    else if(body instanceof Block)
      {
	returnType = ((Block) body).getType();
	if(returnType==null)
	  User.error(this,
		     "Not implemented: the last statement of "+this+
		     "must be a return statement");
      }
    else
      {
	Internal.error(this,
		       "Body of lambda expression is not of known form");
	returnType=null;
      }
    
    type=new Polytype(Constraint.and(constraint,returnType.getConstraint()),
		      new FunType(MonoSymbol.getMonotype(formals),
				  returnType.getMonotype()));
  }

  public Monotype getReturnType()
  {
    return null;
  }
  
  void findJavaClasses()
  {
    List types = MonoSymbol.getMonotype(formals);
    Monotype.findJavaClasses(types, typeScope);
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

    blockExp = new gnu.expr.BlockExp();

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
      constraint
      + "fun ("
      + Util.map("",", ","",formals)
      + ") => "
      + body
      ;
  }
  
  Collection /* of MonoSymbol */ formals;
  Constraint constraint;
  Statement body;
}
