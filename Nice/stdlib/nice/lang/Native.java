/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.lang;

/**
   Basic functions, some targeted for inlining some time in the future...
*/
public final class Native
{
  public static Object object(Object o) { return o; }  
    
  public static boolean eq (Object o1, Object o2) { return o1 == o2; }
  public static boolean neq(Object o1, Object o2) { return o1 != o2; }
  
  public static boolean eq (boolean b1, boolean b2) { return b1 == b2; }
  public static boolean neq(boolean b1, boolean b2) { return b1 != b2; }

  public static boolean eq (char c1, char c2) { return c1 == c2; }
  public static boolean neq(char c1, char c2) { return c1 != c2; }
  
  // Operations on "polymorphic" arrays
  // Arrays of unknown component type are of type Object
  // Methods in java.lang.reflect.Array allow to handle them.

  /** Return a new copy with newSize elements */
  public static Object resize(Object from, int newSize)
  {
    int copyLength = java.lang.reflect.Array.getLength(from);
    if (newSize < copyLength)
      copyLength = newSize;
    
    Object res = java.lang.reflect.Array.newInstance
      (from.getClass().getComponentType(), newSize);
    java.lang.System.arraycopy(from, 0, res, 0, copyLength);

    return res;
  }

  public static int intValue(double d) { return (int) d; }
}
