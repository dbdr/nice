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
//$Modified: Thu Aug 19 13:09:34 1999 by bonniot $
// Description : Abstract class for values of basic types

package bossa.syntax;

import bossa.util.*;

abstract public class ConstantExp extends Expression
{
  void resolve()
  {
    TypeSymbol s=typeScope.lookup(new LocatedString(className,Location.nowhere()));

    Internal.error(s==null,
		   "Base class "+className+
		   " was not found in the standard library");
    Internal.error(!(s instanceof TypeConstructor),
		   "Base class "+className+
		   " is not a class !");

    type=new Polytype(new MonotypeConstructor(((TypeConstructor) s),null,
					      null));
  }

  Type getType()
  {
    return type;
  }

  String className="[not defined yet]";
  private Polytype type;
}
