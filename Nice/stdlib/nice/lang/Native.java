/****************************************************************************
 *                                 N I C E                                  *
 *              A high-level object-oriented research language              *
 *                         (c) Daniel Bonniot 2003                          *
 *                                                                          *
 *  This package is free software; you can redistribute it and/or modify    *
 *  it under the terms of the GNU General Public License as published by    *
 *  Free Software Foundation; either version 2 of the License, or (at your  *
 *  option) any later version.                                              *
 *                                                                          *
 *  As a special exception, the copyright holders of this library give you  *
 *  permission to link this library with independent modules to produce an  *
 *  executable, regardless of the license terms of these independent        *
 *  modules, and to copy and distribute the resulting executable under      *
 *  terms of your choice.                                                   *
 ****************************************************************************/

package nice.lang;

/**
   Basic functions, some targeted for inlining some time in the future...
*/
public final class Native
{
  public static Object object(Object o) { return o; }  

  // Operations on "polymorphic" arrays
  // Arrays of unknown component type are of type Object
  // Methods in java.lang.reflect.Array allow to handle them.

  /** Return a new copy with newSize elements */
  public static Object resize(Object from, int newSize)
  {
    int copyLength = java.lang.reflect.Array.getLength(from);

    if (copyLength == newSize)
      return from;

    if (newSize < copyLength)
      copyLength = newSize;
    
    Object res = java.lang.reflect.Array.newInstance
      (from.getClass().getComponentType(), newSize);
    java.lang.System.arraycopy(from, 0, res, 0, copyLength);

    return res;
  }
}
