/****************************************************************************
 *                                 N I C E                                  *
 *              A high-level object-oriented research language              *
 *                         (c) Daniel Bonniot 2004                          *
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
   Class used to wrap native int arrays when considered as part of the 
   collection hierarchy.
*/
public final class rawIntArray extends rawArray
{
  rawIntArray(int[] arr)
  {
    super(arr);
    this.arr = arr;
  }
  
  private final int[] arr;

  /****************************************************************
   * Implementation of java.util.List
   ****************************************************************/

  public final int size ()
  {
    return arr.length;
  }

  public final Object get (int index)
  {
    return new Integer(arr[index]);
  }

  public final Object set (int index, Object element)
  {
    int old = arr[index];
    arr[index] = ((Number)element).intValue(); 
    return new Integer(old);
  }

}