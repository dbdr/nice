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

// File    : Variance.java
// Created : Fri Jul 23 12:15:46 1999 by bonniot
//$Modified: Fri Jul 23 12:20:02 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * Variance of a type constructor
 * 
 * @author bonniot
 */

public class Variance
{
  public Variance(int n)
  {
    this.size=n;
  }

  int size;
}
