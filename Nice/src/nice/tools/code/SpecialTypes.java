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

package nice.tools.code;

import bossa.util.*;
import gnu.bytecode.*;

/**
   Used to store types with automatic conversion.
   
   @version $Date$
   @author bonniot
*/

public class SpecialTypes
{
  static public final Type intType, longType, byteType, charType, 
    shortType, floatType, doubleType, booleanType, voidType;
  
  public static void init()
  {
    // if called, we know the static initializers are executed
  }
  
  static
  {
    byteType = Type.byte_type;
    shortType = Type.short_type;
    intType = Type.int_type;
    longType = Type.long_type;

    charType = Type.char_type;

    booleanType = Type.boolean_type;
    voidType = Type.void_type;

    floatType = Type.float_type;
    doubleType = Type.double_type;

    //Type.flushTypeChanges();
  }

  static public Type array(Type elements)
  {
    if (elements == Type.pointer_type)
      return SpecialArray.unknownTypeArray();
    
    return SpecialArray.create(elements);
  }
}
