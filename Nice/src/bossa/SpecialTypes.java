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
//$Modified: Tue Jun 13 11:37:38 2000 by Daniel Bonniot $

package bossa;

import bossa.util.*;
import gnu.bytecode.*;

import kawa.standard.Scheme;

/**
 * Used to store types with automatic conversion.
 * 
 * @author bonniot
 */

public class SpecialTypes
{
  static public final Type intType, longType, byteType, charType, 
    shortType, floatType, doubleType, booleanType, voidType, arrayType;
  
  public static void init()
  {
    // if called, we know the static initializers are executed
  }
  
  static
  {
    intType = Scheme.intType;
    longType = Scheme.longType;
    charType = Scheme.charType;
    byteType = Scheme.byteType;
    shortType = Scheme.shortType;
    floatType = Scheme.floatType;
    doubleType = Scheme.doubleType;
    booleanType = Scheme.booleanType;
    voidType = gnu.bytecode.Type.void_type;

    arrayType = new SpecialArray(Type.pointer_type);

    Type.flushTypeChanges();
  }

  static public Type makeArrayType(Type elements)
  {
    return new SpecialArray(elements);
  }
}
