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
//$Modified: Thu Dec 02 18:30:32 1999 by bonniot $

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

  /**
   * Creates the initial rigid context.
   */
  void createContext(bossa.modules.Module module);
  
  /**
   * Write the exported interface of the definition
   * to the stream.
   *
   * @param s a PrintWriter
   */
  void printInterface(java.io.PrintWriter s);

  /**
   * Generates bytecode for this definition.
   *
   * @param moduleClass the class for static elements
   of the module (alternatives, toplevel variables, ...).
   */
  void compile(bossa.modules.Module module);
}
