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

// File    : ReturnStmt.java
// Created : Mon Jul 05 17:21:40 1999 by bonniot
//$Modified: Tue Feb 29 19:22:39 2000 by Daniel Bonniot $
// Description : return in a function or method

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;

public class ReturnStmt extends Statement
{
  public ReturnStmt(Expression value)
  {
    this.value=expChild(value);
  }

  Polytype returnType()
  {
    return value.getType();
  }
  
  void typecheck()
  {
    includingFunction = Node.currentFunction;
    
    Monotype declaredRetType = includingFunction.getReturnType();
    if(declaredRetType==null)
      return;
    
    try{
      Typing.leq(returnType(),new Polytype(declaredRetType));
    }
    catch(TypingEx e){
      User.error(this,
		 "Return type "+returnType()+" is not correct",
		 " :"+e);
    }
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression generateCode()
  {
    return new gnu.expr.ExitExp(value.generateCode(),
				includingFunction.getBlock());
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    return "return "+value;
  }
  
  protected Expression value;
  private Function includingFunction;
}
