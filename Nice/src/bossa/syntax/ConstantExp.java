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
//$Modified: Tue Jul 13 15:07:17 1999 by bonniot $
// Description : Abstract class for values of basic types

package bossa.syntax;

import bossa.util.*;

abstract public class ConstantExp extends Expression
{
  Expression resolve(VarScope scope, TypeScope ts)
  {
    TypeSymbol s=ts.lookup(new IdentType(new LocatedString(className,Location.nowhere()),null));

    Internal.error(s==null,
		   "Base class "+className+
		   " was not found in the standard library");
    Internal.error(!(s instanceof ClassDefinition),
		   "Base class "+className+
		   " is not a class !");

    //TODO    type=new ClassType((ClassDefinition)s);
    // Nothing to do, its already a value
    return this;
  }

  Polytype getType()
  {
    return type;
  }

  String className="[not defined yet]";
  private Polytype type;
}
