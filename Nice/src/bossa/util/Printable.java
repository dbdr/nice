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

// File    : Printable.java
// Created : Wed Oct 13 14:52:35 1999 by bonniot
//$Modified: Wed Jul 26 14:28:09 2000 by Daniel Bonniot $

package bossa.util;

/**
 * Interface of classes with a parametrized toString function.
 * 
 * @author bonniot
 */

public interface Printable
{
  /** toString parameters  */
  static final int inConstraint = 0;
  static final int detailed = 1;
  
  public String toString(int param);
}
