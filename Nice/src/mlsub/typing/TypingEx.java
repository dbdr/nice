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

// File    : TypingEx.java
// Created : Tue Jul 20 12:06:53 1999 by bonniot
//$Modified: Wed May 31 11:09:29 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * Generic type exception.
 */
public class TypingEx extends Exception
{
  TypingEx(String msg)
  {
    super(msg);
  }
}

