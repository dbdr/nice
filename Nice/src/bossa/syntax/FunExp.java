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
//$Modified: Tue Jan 25 10:49:59 2000 by Daniel Bonniot $
// Description : A functional expression

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class FunExp extends Expression
{
  public FunExp(Constraint cst, List formals, List body)
  {
    this.formals=addChildren(formals);
    this.constraint=cst;
    this.body=new Block(body);

    addChild(constraint);
    addChild(this.body);
  }

  void computeType()
  {
    try{
      constraint.assert();
    }
    catch(bossa.typing.TypingEx e){
      User.error(this,"functional expression is ill-typed");
    }
    
    Polytype returnType=body.getType();
    
    if(returnType==null)
      User.error(this,"The last statement of "+this+
		 "must be a return statement");
 
    type=new Polytype(Constraint.and(constraint,returnType.getConstraint()),
		      new FunType(MonoSymbol.getMonotype(formals),
				  returnType.getMonotype()));
  }

  void findJavaClasses()
  {
    List types = MonoSymbol.getMonotype(formals);
    Monotype.findJavaClasses(types, typeScope);
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    gnu.expr.LambdaExp res = new gnu.expr.LambdaExp();

    gnu.expr.BlockExp block = new gnu.expr.BlockExp();
    gnu.expr.BlockExp savedBlock = Statement.currentMethodBlock;
    Statement.currentMethodBlock=block;

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
			     // Since a applyN method will be produced,
			     // we must forget about the types... :-(
			     gnu.bytecode.Type.pointer_type);

	decl.setParameter(true);	
	s.setDeclaration(decl);
      }
    
    res.body = block;
    block.setBody(body.generateCode());

    Statement.currentMethodBlock = savedBlock;
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
  Block body;
}
