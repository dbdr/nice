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

// File    : ConstantExp.java
// Created : Thu Jul 08 15:36:40 1999 by bonniot
//$Modified: Tue Nov 09 17:50:15 1999 by bonniot $
// Description : Abstract class for values of basic types

package bossa.syntax;

import bossa.util.*;

abstract public class ConstantExp extends Expression
{
  void resolve()
  {
    TypeSymbol s=typeScope.lookup(new LocatedString(className,Location.nowhere()));
    
    if(s==null)
      s=JavaTypeConstructor.make(new LocatedString(className,Location.nowhere()));
    
    Internal.error(s==null,
		   "Base class "+className+
		   " was not found in the standard library");
    Internal.error(!(s instanceof TypeConstructor),
		   "Base class "+className+
		   " is not a class !");
    TypeConstructor tc=(TypeConstructor) s;
    type=new Polytype(new MonotypeConstructor(tc,null,null));
  }

  void computeType()
  {
    //Already done in constructor
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    return new gnu.expr.QuoteExp(value);
  }
  
  String className="[not defined yet]";

  protected Object value;
}
