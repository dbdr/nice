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
//$Modified: Thu Dec 02 12:01:14 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * Abstract class for constant values of basic types.
 *
 * Numeric types come from {@link gnu.math gnu.math}.
 */
abstract public class ConstantExp extends Expression
{
  void resolve()
  {
    TypeSymbol s=typeScope.lookup(new LocatedString(className,Location.nowhere()));
    
    if(s==null)
      Internal.error("Base class "+className+
		     " was not found in the standard library");

    if(!(s instanceof TypeConstructor))
      Internal.error("Base class "+className+
		     " is not a class !");
    
    TypeConstructor tc=(TypeConstructor) s;
    type=new Polytype(new MonotypeConstructor(tc,null,null));
  }

  void computeType()
  {
    //Already done in resolve()
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    return new gnu.expr.QuoteExp(value);
  }
  
  protected String className = "undefined class name";
  
  protected Object value;

  public String toString()
  {
    return value.toString();
  }
}
