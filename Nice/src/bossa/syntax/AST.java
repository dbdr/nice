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
//$Modified: Thu Oct 28 15:55:39 1999 by bonniot $

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
  public AST(List defs)
  {
    super(defs,Node.global);
    
    this.definitions=defs;
  }
  
  public void load()
  {
    Debug.println("Scoping (build  )");
    buildScope();

    Debug.println("Scoping (resolve)");
    doResolve();
  }
  
  public void createContext()
  {
    // Necessary because toplevel assertions can be unsatisfiable
    // in which case we should be able to backtrack
    for(Iterator i=definitions.iterator();i.hasNext();)
      ((Definition) i.next()).createContext();
  }
  
  public void typechecking()
  {
    Debug.println("Typechecking");
    doTypecheck();
  }

  public void printInterface(java.io.PrintWriter s)
  {
    for(Iterator i=definitions.iterator();i.hasNext();)
      ((Definition)i.next()).printInterface(s);
  }
  
  public String toString()
  {
    return Util.map(definitions);
  }

  private List /* of Definition */ definitions;
}

