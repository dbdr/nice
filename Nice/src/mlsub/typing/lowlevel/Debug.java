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

// File    : Debug.java
// Created : Wed May 31 10:45:16 2000 by Daniel Bonniot
//$Modified: Wed May 31 10:53:52 2000 by Daniel Bonniot $

package mlsub.typing.lowlevel;

/**
 * Printing debug messages.
 * 
 * @author Daniel Bonniot
 */

public abstract class Debug
{
  public static void println(String s)
  {
    System.err.println(s);
  }
}
