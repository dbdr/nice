/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.lang;

import java.lang.reflect.Array;
import java.util.*;

/**
   Class used to wrap native arrays when considered as part of the 
   collection hierarchy.

   @version: $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public final class rawArray extends java.util.AbstractList
{
  private rawArray(Object value)
  {
    this.value = value;
  }
  
  public Object value;
  public Object value()
  {
    return value;
  }
  
  public static rawArray make(Object value)
  {
    if (value == null)
      return null;
    return new rawArray(value);
  }

  /****************************************************************
   * Implementation of java.util.List
   ****************************************************************/

  public int size ()
  {
    return Array.getLength(value);
  }

  public Object get (int index)
  {
    return Array.get(value, index);
  }

  public Object set (int index, Object element)
  {
    Object res = get(index);
    Array.set(value, index, element);
    return res;
  }

  // CONVERSIONS

  public static boolean[] convert_boolean(Object[] array)
  {
    if (array == null)
      return null;

    boolean[] res = new boolean[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	res[i] = ((Boolean) o).booleanValue();
      }
    
    return res;
  }

  public static char[] convert_char(Object[] array)
  {
    if (array == null)
      return null;

    char[] res = new char[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	res[i] = ((Character) o).charValue();
      }
    
    return res;
  }

  public static byte[] convert_byte(Object[] array)
  {
    if (array == null)
      return null;

    byte[] res = new byte[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	res[i] = ((Number) o).byteValue();
      }
    
    return res;
  }

  public static short[] convert_short(Object[] array)
  {
    if (array == null)
      return null;

    short[] res = new short[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	res[i] = ((Number) o).shortValue();
      }
    
    return res;
  }

  public static int[] convert_int(Object[] array)
  {
    if (array == null)
      return null;

    int[] res = new int[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	try{
	  res[i] = ((Number) o).intValue();
	}
	catch(ClassCastException e){
	  res[i] = ((Character) o).charValue();
	}
      }
    
    return res;
  }

  public static long[] convert_long(Object[] array)
  {
    if (array == null)
      return null;

    long[] res = new long[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	try{
	  res[i] = ((Number) o).longValue();
	}
	catch(ClassCastException e){
	  res[i] = ((Character) o).charValue();
	}
      }
    
    return res;
  }

  public static float[] convert_float(Object[] array)
  {
    if (array == null)
      return null;

    float[] res = new float[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	try{
	  res[i] = ((Number) o).floatValue();
	}
	catch(ClassCastException e){
	  res[i] = ((Character) o).charValue();
	}
      }
    
    return res;
  }

  public static double[] convert_double(Object[] array)
  {
    if (array == null)
      return null;

    double[] res = new double[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	try{
	  res[i] = ((Number) o).doubleValue();
	}
	catch(ClassCastException e){
	  res[i] = ((Character) o).charValue();
	}
      }
    
    return res;
  }

  /****************************************************************
   * Conversion from generic Object array
   ****************************************************************/

  public static boolean[] gconvert_boolean(Object array)
  {
    if (array == null)
      return null;

    int len = Array.getLength(array);
    boolean[] res = new boolean[len];
    for (int i = len; --i >= 0;)
      {
	Object o = Array.get(array, i);
	res[i] = ((Boolean) o).booleanValue();
      }
    
    return res;
  }

  public static char[] gconvert_char(Object array)
  {
    if (array == null)
      return null;

    int len = Array.getLength(array);
    char[] res = new char[len];
    for (int i = len; --i >= 0;)
      {
	Object o = Array.get(array, i);
	res[i] = ((Character) o).charValue();
      }
    
    return res;
  }

  public static byte[] gconvert_byte(Object array)
  {
    if (array == null)
      return null;

    int len = Array.getLength(array);
    byte[] res = new byte[len];
    for (int i = len; --i >= 0;)
      {
	Object o = Array.get(array, i);
	res[i] = ((Number) o).byteValue();
      }
    
    return res;
  }

  public static short[] gconvert_short(Object array)
  {
    if (array == null)
      return null;

    int len = Array.getLength(array);
    short[] res = new short[len];
    for (int i = len; --i >= 0;)
      {
	Object o = Array.get(array, i);
	res[i] = ((Number) o).shortValue();
      }
    
    return res;
  }

  public static int[] gconvert_int(Object array)
  {
    if (array == null)
      return null;

    int len = Array.getLength(array);
    int[] res = new int[len];
    for (int i = len; --i >= 0;)
      {
	Object o = Array.get(array, i);
	try{
	  res[i] = ((Number) o).intValue();
	}
	catch(ClassCastException e){
	  res[i] = ((Character) o).charValue();
	}
      }
    
    return res;
  }

  public static long[] gconvert_long(Object array)
  {
    if (array == null)
      return null;

    int len = Array.getLength(array);
    long[] res = new long[len];
    for (int i = len; --i >= 0;)
      {
	Object o = Array.get(array, i);
	try{
	  res[i] = ((Number) o).longValue();
	}
	catch(ClassCastException e){
	  res[i] = ((Character) o).charValue();
	}
      }
    
    return res;
  }

  public static float[] gconvert_float(Object array)
  {
    if (array == null)
      return null;

    int len = Array.getLength(array);
    float[] res = new float[len];
    for (int i = len; --i >= 0;)
      {
	Object o = Array.get(array, i);
	try{
	  res[i] = ((Number) o).floatValue();
	}
	catch(ClassCastException e){
	  res[i] = ((Character) o).charValue();
	}
      }
    
    return res;
  }

  public static double[] gconvert_double(Object array)
  {
    if (array == null)
      return null;

    int len = Array.getLength(array);
    double[] res = new double[len];
    for (int i = len; --i >= 0;)
      {
	Object o = Array.get(array, i);
	try{
	  res[i] = ((Number) o).doubleValue();
	}
	catch(ClassCastException e){
	  res[i] = ((Character) o).charValue();
	}
      }
    
    return res;
  }

  /**
     Return a T[] array, 
     where T is given by componentClass (T should not be primitive),
     holding the same elements as <code>array</code>.
  */
  public static Object[] gconvert(Object array, String componentClass)
  {
    if (array == null)
      return null;

    try{
      int len = Array.getLength(array);
      Object[] res = (Object[]) java.lang.reflect.Array.newInstance
	(Class.forName(componentClass), len);

      // If the components are primitive arrays, we must recursively
      // convert them.
      boolean primitiveArray = 
	componentClass.charAt(0) == '[' &&
	componentClass.charAt(1) != 'L';

      for (int i = len; --i >= 0;) {
	Object value = Array.get(array, i);

	if (primitiveArray)
	  value = convertPrimitive(value, componentClass.substring(1));

	Array.set(res, i, value);
      }

      return res;
    }
    catch(ClassNotFoundException e){
      throw new Error("Could not find class " + componentClass +
		      " during array conversion");
    }
  }

  private static Object convertPrimitive (Object array, String component)
  {
    switch (component.charAt(0)) {
      case 'Z': return gconvert_boolean(array);
      case 'B': return gconvert_byte(array);
      case 'S': return gconvert_short(array);
      case 'I': return gconvert_int(array);
      case 'L': return gconvert_long(array);
      case 'F': return gconvert_float(array);
      case 'D': return gconvert_double(array);
      default : throw new Error("Unexpected error in array conversion");
    }
  }
}
