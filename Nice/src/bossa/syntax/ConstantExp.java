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
//$Modified: Fri Jul 09 15:00:46 1999 by bonniot $
// Description : Abstract class for values of basic types

package bossa.syntax;

import bossa.util.*;

abstract public class ConstantExp extends Expression
{
  Expression resolve(VarScope scope, TypeScope ts)
  // Nothing to do, its already a value
  {
    return this;
  }
}
