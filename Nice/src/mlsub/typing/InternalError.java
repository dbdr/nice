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
//$Modified: Wed May 31 11:12:58 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * Unexpected error.
 *
 * Denotes a bug in the package, or a misusage of it.
 * 
 * @author Daniel Bonniot
 */

public class InternalError extends Error
{
  public InternalError(String msg)
  {
    super(msg);
  }
}
