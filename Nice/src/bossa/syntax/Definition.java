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
//$Modified: Fri Sep 03 11:37:33 1999 by bonniot $

package bossa.syntax;

import java.util.Collection;

/**
 * Abstract definition
 * May be a class definition, a method definition, 
 * an interface definition...
 */
public interface Definition
{
  /**
   * Returns a collection of definitions that are derived
   * from the current definition.
   * For example, a class definition A returns a collection
   * with the definition of class #A (#A<:A, #A is final, abstracts Top without implementing it).
   */
  Collection associatedDefinitions();
  // If we turn Definition to an abstract class,
  // it would be good to have a default implementation
  // that returns null, it would save lines
  // since only ClassDefinition has a different implementation.
}
