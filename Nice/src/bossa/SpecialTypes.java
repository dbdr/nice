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

// File    : SpecialTypes.java
// Created : Mon Jan 17 14:19:30 2000 by bonniot
//$Modified: Mon Jan 24 19:30:45 2000 by Daniel Bonniot $

package bossa;

import bossa.util.*;
import gnu.bytecode.*;

import kawa.lang.SpecialType;

/**
 * Used to store types with automatic conversion.
 * 
 * @author bonniot
 */

public class SpecialTypes
{
  static public Type intType, booleanType, voidType, arrayType;

  static 
  {
    intType = new SpecialType
      ("int", 		"I", 4, java.lang.Boolean.TYPE);
    booleanType = new SpecialType
      ("boolean", 	"Z", 1, java.lang.Boolean.TYPE);
    voidType = new SpecialType
      ("void", 		"V", 0, java.lang.Void.TYPE);

    arrayType = new SpecialArray(Type.pointer_type);
  }

  static public Type makeArrayType(Type elements)
  {
    return new SpecialArray(elements);
  }
}
