/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;

import java.util.*;

/**
   The Abstract Syntax Tree : a collection of definitions.

   @see Definition
 */
public abstract class AST extends Node
{
  AST(List children, int propagate)
  {
    super(children, propagate);
  }

  public List definitions()
  {
    return children;
  }

  abstract public void buildScope();

  abstract public void resolveScoping();

  abstract public void typedResolve();

  abstract public void localResolve();

  abstract public void typechecking(boolean compiling);

  public void printInterface(java.io.PrintWriter s)
  {
    for(Iterator i = children.iterator(); i.hasNext();)
      ((Definition) i.next()).printInterface(s);
  }

  abstract public void compile(boolean generateCode);
  
  public int numberOfDeclarations()
  {
    return children.size();
  }

}

