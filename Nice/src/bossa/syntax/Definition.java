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
//$Modified: Sat Feb 19 15:27:20 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.Collection;

/**
 * Abstract definition.
 * May be a class definition, a method definition, 
 * an interface definition...
 */
public abstract class Definition extends Node implements bossa.util.Located
{

  Definition(LocatedString name, int propagate)
  {
    super(propagate);
    
    this.name = name;
    module = currentModule;
  }
  
  /**
   * Returns a collection of definitions that are derived
   * from the current definition.
   * For example, a class definition A returns a collection
   * with the definition of class #A.
   */
  public Collection associatedDefinitions()
  {
    return null;
  }
  
  /**
   * Creates the initial rigid context.
   */
  abstract void createContext();
  
  /**
   * Write the exported interface of the definition
   * to the stream.
   *
   * @param s a PrintWriter
   */
  abstract void printInterface(java.io.PrintWriter s);

  /**
   * Generates bytecode for this definition.
   */
  abstract void compile();
  
  /**
   * The module this definition appears in.
   */
  protected bossa.modules.Package module;
  static public bossa.modules.Package currentModule;

  /****************************************************************
   * Name and location of the definition
   ****************************************************************/
  
  public LocatedString getName()
  {
    return name;
  }

  public bossa.util.Location location()
  {
    return name.location();
  }

  LocatedString name;
}
