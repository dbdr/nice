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
//$Modified: Fri May 26 18:12:48 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

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
      {
	throwableTC = JavaTypeConstructor.make
	  ("java.lang.Throwable", gnu.bytecode.Type.throwable_type);
      }
    return throwableTC;
  }
	
  private static Polytype throwableType;
  static Polytype throwableType()
  {
    if(throwableType==null)
      {
	throwableType = new Polytype(new MonotypeConstructor
	  (throwableTC() ,null, Location.nowhere()));
      }
    return throwableType;
  }
  
  public void typecheck()
  {
    try{
      bossa.typing.Typing.leq(exn.getType(), throwableType());
    }
    catch(bossa.typing.TypingEx e){
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
