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
//$Modified: Tue May 02 14:59:56 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
 * Call of an object constructor.
 */
public class NewExp extends CallExp
{
  public NewExp(TypeConstructor tc, List arguments)
  {
    super(null, arguments);
    this.tc = tc;
  }

  void findJavaClasses()
  {
    super.findJavaClasses();
    tc.resolve(typeScope);
  }
  
  void resolve()
  {
    super.resolve();
    tc = tc.resolve(typeScope);
    fun = new ExpressionRef // not necessary
      (new OverloadedSymbolExp(tc.getConstructors(),
			       new LocatedString("new "+tc.getName(),
// not tc.location() which is the location of the definition of the class
						 location()), 
			       null));
  }
  /*
  void computeType()
  {
    TypeParameters tp = new TypeParameters(tc.name,tc.variance);
    type = new Polytype(new Constraint(tp.content,null),
			new MonotypeConstructor(tc,tp,tc.location()));
  }
  */
  void typecheck()
  {
    if(!tc.instantiable())
      if(tc.constant())
	User.error(this,
		   tc+" is abstract, it can't be instantiated");
      else
	User.error(this,
		   tc+" is a type variable, it can't be instantiated");
    super.typecheck();
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/
  /*
  public gnu.expr.Expression compile()
  {
    gnu.bytecode.ClassType ct = (gnu.bytecode.ClassType) tc.getJavaType();
    
    return new gnu.expr.ApplyExp
      (new gnu.expr.QuoteExp(new gnu.expr.PrimProcedure
			     (ct,gnu.bytecode.Type.typeArray0)),
       new gnu.expr.Expression[0]);
  }
  */
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "new "+tc+"("+Util.map("", ", ", "", parameters)+")";
  }

  TypeConstructor tc;
}
