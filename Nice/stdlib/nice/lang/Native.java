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
  
  // Primitive type arrays
  public static void set(long   [] array, int index, long value) { array[index] = value; }
  public static void set(int    [] array, int index, int value) { array[index] = value; }
  public static void set(short  [] array, int index, short value) { array[index] = value; }
  public static void set(byte   [] array, int index, byte value) { array[index] = value; }
  public static void set(char   [] array, int index, char value) { array[index] = value; }
  public static void set(double [] array, int index, double value) { array[index] = value; }
  public static void set(float  [] array, int index, float value) { array[index] = value; }
  public static void set(boolean[] array, int index, boolean value) { array[index] = value; }

  public static long    get(long   [] array, int index){ return array[index]; }
  public static int     get(int    [] array, int index){ return array[index]; }
  public static short   get(short  [] array, int index){ return array[index]; }
  public static byte    get(byte   [] array, int index){ return array[index]; }
  public static char    get(char   [] array, int index){ return array[index]; }
  public static double  get(double [] array, int index){ return array[index]; }
  public static float   get(float  [] array, int index){ return array[index]; }
  public static boolean get(boolean[] array, int index){ return array[index]; }

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
}
