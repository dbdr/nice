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
//$Modified: Wed Aug 18 16:08:08 1999 by bonniot $
// Description : Affectation

package bossa.syntax;

import bossa.util.*;
import bossa.parser.ParseException;
import bossa.typing.*;

public class AffectationStmt extends Statement
{
  public AffectationStmt(Expression to,
			 Expression value) throws ParseException
  {
    if(!(to instanceof FieldExp) && !(to instanceof IdentExp))
      throw new ParseException("Affectation should be done to a field");

    this.to=new ExpressionRef(to);
    this.value=new ExpressionRef(value);
    addChild(this.to);
    addChild(this.value);
  }

  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  void typecheck()
  {
    User.error(!to.isAssignable(),to+" cannot be assigned a value");
    try{
      Typing.leq(value.getType(),to.getType());
    }
    catch(TypingEx t){
      User.error(this,"Typing error : "+to+" cannot be assigned value "+value);
    }

  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return to+" = "+value;
  }

  protected Expression to,value;
}
