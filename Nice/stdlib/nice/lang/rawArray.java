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
   Class used to wrap native arrays when considered as part of the 
   collection hierarchy.

   @version: $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public final class rawArray implements nice.lang.Sequence
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
}
