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

// File    : AffectationStmt.java
// Created : Mon Jul 05 15:49:27 1999 by bonniot
//$Modified: Mon Oct 25 13:12:40 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.parser.ParseException;
import bossa.typing.*;

/**
 * Assignment.
 */
public class AffectationStmt extends Statement
{
  public AffectationStmt(Expression to,
			 Expression value) 
  {
    this.to=expChild(to);
    this.value=expChild(value);
  }

  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  void typecheck()
  {
    User.error(!to.isAssignable(),to+" cannot be assigned a value");
    try{
      value.resolveOverloading(to.getType());
      Typing.leq(value.getType(),to.getType());
    }
    catch(TypingEx t){
      User.error(this,"Typing error : "+to+
		 " cannot be assigned value "+value);
    }
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return to+" = "+value;
  }

  private Expression to,value;
}
