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

// File    : AST.java
// Created : Thu Jul 01 11:01:56 1999 by bonniot
//$Modified: Tue Dec 07 20:13:49 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

import java.util.*;

/**
 * The Abstract Syntax Tree :
 * A collection of definitions
 *
 * @see Definition
 */
public class AST extends Node
{
  public AST(bossa.modules.Module module, List defs)
  {
    super(defs,Node.global);
    
    this.module=module;
    this.definitions=defs;

    for(Iterator i = defs.iterator();
	i.hasNext();)
      ((Definition) i.next()).setModule(module);
  }
  
  public void load()
  {
    buildScope(module);
    doResolve();
  }
  
  public void createContext()
  {
    for(Iterator i=definitions.iterator();i.hasNext();)
      ((Definition) i.next()).createContext();
  }
  
  public void typechecking()
  {
    doTypecheck();
  }

  public void printInterface(java.io.PrintWriter s)
  {
    for(Iterator i=definitions.iterator();i.hasNext();)
      ((Definition)i.next()).printInterface(s);
  }

  public void compile()
  {
    // compile all the field accesses that have been generated
    MethodDefinition.compileMethods(module);
    
    for(Iterator i=definitions.iterator();i.hasNext();)
      ((Definition)i.next()).compile();

    MethodBodyDefinition.compileMain(module);
  }
  
  public String toString()
  {
    return Util.map(definitions);
  }

  private bossa.modules.Module module;
  private List /* of Definition */ definitions;
}

