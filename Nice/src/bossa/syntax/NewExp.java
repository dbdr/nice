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

// File    : NewExp.java
// Created : Thu Jul 08 17:15:15 1999 by bonniot
//$Modified: Tue May 02 11:27:53 2000 by Daniel Bonniot $
// Description : Allocation of a new object

package bossa.syntax;

import bossa.util.*;

public class NewExp extends Expression
{
  public NewExp(TypeConstructor tc)
  {
    this.tc = tc;
  }

  void findJavaClasses()
  {
    tc.resolve(typeScope);
  }
  
  void resolve()
  {
    tc = tc.resolve(typeScope);
  }
  
  void computeType()
  {  
    TypeParameters tp = new TypeParameters(tc.name,tc.variance);
    type = new Polytype(new Constraint(tp.content,null),
			new MonotypeConstructor(tc,tp,tc.location()));
  }

  void typecheck()
  {
    if(!tc.instantiable())
      if(tc.constant())
	User.error(this,
		   tc+" is abstract, it can't be instantiated");
      else
	User.error(this,
		   tc+" is a type variable, it can't be instantiated");
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    gnu.bytecode.ClassType ct = (gnu.bytecode.ClassType) tc.getJavaType();
    
    return new gnu.expr.ApplyExp
      (new gnu.expr.QuoteExp(new gnu.expr.PrimProcedure
			     (ct,gnu.bytecode.Type.typeArray0)),
       new gnu.expr.Expression[0]);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "new "+tc;
  }

  TypeConstructor tc;
}
