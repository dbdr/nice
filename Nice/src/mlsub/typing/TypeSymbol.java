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

// File    : TypeSymbol.java
// Created : Sat Jun 03 15:43:17 2000 by Daniel Bonniot
//$Modified: Tue Jun 06 17:01:20 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * A type symbol.
 *
 * This is what a type identifier can resolve to.
 * 
 * @author Daniel Bonniot
 */

public interface TypeSymbol
{
  TypeSymbol cloneTypeSymbol();
}
