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
//$Modified: Mon Mar 13 18:09:40 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

import java.util.*;

/**
 * The Abstract Syntax Tree : a collection of definitions.
 *
 * @see Definition
 */
public class AST extends Node
{
  public AST(bossa.modules.Package module, List defs)
  {
    super(defs,Node.global);
    
    this.module=module;
    this.definitions=defs;
  }
  
  public void buildScope()
  {
    buildScope(module);
  }
  
  public void resolveScoping()
  {
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
    for(Iterator i=definitions.iterator();i.hasNext();)
      ((Definition)i.next()).compile();
  }
  
  public String toString()
  {
    return "Abstract Syntax Tree ("+definitions.size()+" definitions)";
  }

  private bossa.modules.Package module;
  private List /* of Definition */ definitions;
}

