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

// File    : BadSizeEx.java
// Created : Wed Jul 21 17:23:36 1999 by bonniot
//$Modified: Wed Jul 28 21:29:12 1999 by bonniot $
// Description : Thrown when to lists do not have the same size

package bossa.syntax;

import bossa.util.*;

public class BadSizeEx extends bossa.engine.Unsatisfiable
{
  public BadSizeEx(int expected, int actual)
  {
    super(expected+" expected, "+actual+" actual");
    
    this.expected=expected;
    this.actual=actual;
  }

  int expected,actual;
}
