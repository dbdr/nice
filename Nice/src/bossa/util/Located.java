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

// File    : Located.java
// Created : Tue Jul 20 14:50:17 1999 by bonniot
//$Modified: Fri Jul 23 11:52:22 1999 by bonniot $

package bossa.util;

import bossa.util.*;

/**
 * Interface for objects that have a location in a source file
 *
 * @see Location
 */
public interface Located
{
  public Location location();
}
