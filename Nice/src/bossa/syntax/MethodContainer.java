/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : MethodContainer.java
// Created : Thu Sep 21 17:13:29 2000 by Daniel Bonniot

package bossa.syntax;

/**
   An entity in which methods can be declared.

   @version $Date$
   @author Daniel Bonniot
 */

public interface MethodContainer
{
  mlsub.typing.Variance variance();
  mlsub.typing.TypeSymbol getTypeSymbol();

  /** Create type parameters with the same names as in the entity. */
  mlsub.typing.MonotypeVar[] createSameTypeParameters();
}
