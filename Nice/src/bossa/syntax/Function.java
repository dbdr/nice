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

// File    : Function.java
// Created : Fri Feb 04 11:53:59 2000 by Daniel Bonniot
//$Modified: Mon Jun 05 17:07:32 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * A function is either a method body or a lambda expression.
 * 
 * @author Daniel Bonniot
 */

interface Function
{
  /**
   * Returns the return type of the function,
   * or null if no typecheking must be done.
   */
  mlsub.typing.Monotype getReturnType();
  
  gnu.expr.BlockExp getBlock();
}
