/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : Inline.java
// Created : Tue Jul 25 12:26:52 2000 by Daniel Bonniot

package nice.lang;

/**
   Class used to wrap native arrays when considered as part of the 
   collection hierarchy.

   @version: $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public final class rawArray implements nice.lang$Sequence
{
  private rawArray(Object value)
  {
    this.value = value;
  }
  
  public static rawArray make(Object value)
  {
    if (value == null)
      return null;
    return new rawArray(value);
  }
  
  /** Return a new copy with newSize elements */
  public static Object resize(Object from, int newSize)
  {
    int copyLength = java.lang.reflect.Array.getLength(from);
    if(newSize<copyLength)
      copyLength=newSize;
    
    Object res = java.lang.reflect.Array.newInstance(from.getClass().getComponentType(), newSize);
    java.lang.System.arraycopy(from,0,res,0,copyLength);

    return res;
  }

  public Object value;
  public Object value()
  {
    return value;
  }
  
  // CONVERSIONS

  public static boolean[] convert_boolean(Object[] array)
  {
    boolean[] res = new boolean[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	if (o != null)
	  res[i] = ((Boolean) o).booleanValue();
      }
    
    return res;
  }

  public static char[] convert_char(Object[] array)
  {
    char[] res = new char[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	if (o != null)
	  res[i] = ((Character) o).charValue();
      }
    
    return res;
  }

  public static byte[] convert_byte(Object[] array)
  {
    byte[] res = new byte[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	if (o != null)
	  res[i] = ((Number) o).byteValue();
      }
    
    return res;
  }

  public static short[] convert_short(Object[] array)
  {
    short[] res = new short[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	if (o != null)
	  res[i] = ((Number) o).shortValue();
      }
    
    return res;
  }

  public static int[] convert_int(Object[] array)
  {
    int[] res = new int[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	if (o != null)
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
    long[] res = new long[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	if (o != null)
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
    float[] res = new float[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	if (o != null)
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
    double[] res = new double[array.length];
    for (int i = array.length; --i >= 0;)
      {
	Object o = array[i];
	if (o != null)
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
     holding the same elements than array.
  */
  public static Object convert(Object[] array, String componentClass)
  {
    try{
      Object res = java.lang.reflect.Array.newInstance
	(Class.forName(componentClass), array.length);
      java.lang.System.arraycopy(array,0,res,0,array.length);
      return res;
    }
    catch(ClassNotFoundException e){
      throw new Error("Could not find class " + componentClass +
		      " during array conversion");
    }
  }  
}
