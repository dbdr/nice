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

// File    : LocalDeclarationStmt.java
// Created : Tue Jul 06 12:06:20 1999 by bonniot
//$Modified: Fri Sep 03 15:36:18 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;
import java.util.*;

/**
 * Declaration of a local variable
 * with an optional initial value
 */
public class LocalDeclarationStmt extends Statement
  implements Definition
{
  public LocalDeclarationStmt(LocatedString name, Type type, Expression value)
  {
    this.left=new PolySymbol(name,type);
    addSymbol(left);
    this.type=type;
    addChild(type);

    if(value!=null)
      this.value=expChild(value);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }
  
  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  void typecheck()
  {
    if(value==null) return;
    try{
      Typing.leq(value.getType(),left.getType());
    }
    catch(TypingEx e){
      User.error(this,"Typing error : "+left+" cannot be assigned value "+value+
		 " of type "+value.getType()+" : \n"+
		 e);
    }
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return type+" "+left+
      (value==null?"":" = "+value);
  }

  protected Type type;
  protected ExpressionRef value=null;
  // "name" after scoping
  VarSymbol left;
}
