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

// File    : Module.java
// Created : Mon Feb 14 11:46:42 2000 by Daniel Bonniot
//$Modified: Tue Feb 22 17:12:15 2000 by Daniel Bonniot $

package mlsub.compilation;

import java.util.*;

/**
 * The smallest unit that can be compiled independantly.
 * 
 * @author Daniel Bonniot
 */

public interface Module
{
  /** Returns the list of Modules this modules depends on. */
  List getRequirements();

  /** Creates the scope. */
  void scope();

  /** Resolve scoping and load the constants in the context. */
  void load();

  /** Compile the module.

      This may involve:
       - typecheking
       - code generation
       - saving the interface of the module
  */
  void compile();

  void link();
  
  /** Returns the name of this module. */
  String getName();

  /** The date of the last modification of this module. */
  long lastModification();

  // should be "static"
  void freezeGlobalContext();
  void unfreezeGlobalContext();
}
