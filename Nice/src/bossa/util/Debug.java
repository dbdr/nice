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
// Created : Thu Jul 22 15:37:02 1999 by bonniot
//$Modified: Thu Jul 22 15:38:12 1999 by bonniot $

package bossa.util;

import bossa.util.*;

/**
 * Static class for debug output
 * 
 * @author bonniot
 */

public abstract class Debug
{
  public static void println(String msg)
  {
    System.out.println(msg);
  }
}
