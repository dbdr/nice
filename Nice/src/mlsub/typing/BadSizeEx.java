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

// File    : InternalError.java
// Created : Wed May 31 10:42:34 2000 by Daniel Bonniot
//$Modified: Mon Jun 05 17:31:29 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * Error in sizes.
 * 
 * @author Daniel Bonniot
 */

public class BadSizeEx extends InternalError
{
  public BadSizeEx(int expected, int actual)
  {
    super(expected+" expected, "+ actual+" given");
    this.expected = expected;
    this.actual = actual;
  }

  public int expected, actual;
}
