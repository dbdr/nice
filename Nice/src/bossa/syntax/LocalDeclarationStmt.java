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
//$Modified: Fri Jul 23 11:59:43 1999 by bonniot $
// Description : Declaration of a local variable
//   with optional initial value

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;

public class LocalDeclarationStmt extends Statement
{
  public LocalDeclarationStmt(LocatedString name, Type type, Expression value)
  {
    this.name=name;
    this.type=type;
    this.value=value;
  }

  void resolveScope()
  {
    type.buildScope(typeScope);
    type.resolve();
    left=scope.lookup(name);
    if(value!=null)
      value=value.resolve(scope,typeScope);
  }

  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  void typecheck()
  {
    if(value!=null){
      try{
	Typing.leq(value.getType(),left.getType());
      }
      catch(TypingEx t){
	User.error(this,left+" cannot be assigned value "+value+" : "+
		   t.getMessage());
      }
    }
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return type+" "+name+
      (value==null?"":" = "+value);
  }

  protected LocatedString name;
  protected Type type;
  protected Expression value;
  // "name" after scoping
  VarSymbol left;
}
