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
//$Modified: Fri Nov 05 17:30:14 1999 by bonniot $
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
    
    User.error(returnType==null,"The last statement of "+this+
	       "must be a return statement");
 
    type=new Polytype(Constraint.and(constraint,returnType.getConstraint()),
		      new FunType(MonoSymbol.getMonotype(formals),
				  returnType.getMonotype()));
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    return new gnu.expr.LambdaExp(body.compile());
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
  
  Collection /* of FieldSymbol */ formals;
  Constraint constraint;
  Block body;
}
