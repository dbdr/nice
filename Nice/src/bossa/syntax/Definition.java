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

// File    : Definition.java
// Created : Thu Jul 01 11:17:28 1999 by bonniot
//$Modified: Mon Aug 30 15:42:52 1999 by bonniot $
// Description : Abstract definition
//   May be a class definition, a method definition, 
//   an interface definition...

package bossa.syntax;

import java.util.Collection;

public interface Definition
{
  /**
   * Returns a collection of definitions that are derived
   * from the current definition.
   * For example, a class definition A returns a collection
   * with the definition of class #A (#A<:A, #A is final, abstracts Top without implementing it).
   */
  Collection associatedDefinitions();
}
