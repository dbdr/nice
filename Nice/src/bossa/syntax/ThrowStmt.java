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

// File    : ThrowStmt.java
// Created : Thu May 25 17:33:52 2000 by Daniel Bonniot
//$Modified: Thu Jun 08 15:45:08 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

import mlsub.typing.*;
import mlsub.typing.Polytype;
import mlsub.typing.MonotypeConstructor;

/**
 * Throw statement.
 * 
 * @author Daniel Bonniot
 */

public class ThrowStmt extends Statement
{
  public ThrowStmt(Expression e)
  {
    this.exn = expChild(e);
  }

  private static TypeConstructor throwableTC;
  static TypeConstructor throwableTC()
  {
    if(throwableTC==null)
      throwableTC = JavaClasses.make
	("java.lang.Throwable", gnu.bytecode.Type.throwable_type);

    return throwableTC;
  }
	
  private static Polytype throwableType;
  static Polytype throwableType()
  {
    if(throwableType==null)
      {
	throwableType = new Polytype
	  (mlsub.typing.Constraint.True, 
	   new MonotypeConstructor(throwableTC() ,null));
      }
    return throwableType;
  }
  
  public void typecheck()
  {
    try{
      Typing.leq(exn.getType(), throwableType());
    }
    catch(TypingEx e){
      User.error(exn,
		 exn+" is not throwable",
		 e);
    }
  }
  
  public gnu.expr.Expression generateCode()
  {
    return new gnu.expr.ThrowExp(exn.generateCode());
  }
  
  public String toString()
  {
    return "throw "+exn;
  }

  private Expression exn;
}
