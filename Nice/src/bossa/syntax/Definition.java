/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import java.util.*;

/**
   Abstract definition.
   May be a class definition, a method definition, an interface definition...

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
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
     Resolve local entities, that do not influence the global context.

     Toplevel entities have already been resolved and declared earlier,
     since they can be mutually recursive.
  */
  void resolveBody() { }

  /**
   * Write the exported interface of the definition
   * to the stream.
   *
   * @param s a PrintWriter
   */
  abstract void printInterface(java.io.PrintWriter s);

  static void printInterface(List definitions, java.io.PrintWriter s)
  {
    if (definitions == null)
      return;
    for (Iterator i = definitions.iterator(); i.hasNext();)
      ((Definition) i.next()).printInterface(s);
  }
  
  /**
   * Generates bytecode for this definition.
   */
  abstract void compile();
  
  /**
   * The module this definition appears in.
   */
  protected Module module;
  public static Module currentModule;

  public boolean inInterfaceFile()
  {
    return module.interfaceFile();
  }

  bossa.modules.Compilation compilation()
  {
    return module.compilation();
  }

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
