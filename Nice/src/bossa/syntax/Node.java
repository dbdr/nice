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

// File    : Node.java
// Created : Thu Jul 08 10:24:56 1999 by bonniot
//$Modified: Fri Jul 09 20:31:41 1999 by bonniot $
// Description : Basic component of the syntax tree
//   Defines its local scope 

package bossa.syntax;

import java.util.*;
import bossa.util.*;

abstract class Node
{
  Node()
  {
    
  }

  /** Sets up the scope, once the outer scope is given */
  void buildScope(VarScope outer, TypeScope typeOuter)
  // Default behaviour, must be overriden in nodes
  // that really define a new scope
  {
    Internal.warning(this.scope!=null,"Scope set twice for "+this);
    this.scope=outer;
    this.typeScope=typeOuter;
  }

  /** iterates on the collection of nodes */
  void buildScope(VarScope outer, TypeScope typeOuter, Collection c)
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      ((Node)i.next()).buildScope(outer,typeOuter);
  }

  /** uses the scope to replace identifiers with their meaning */
  abstract void resolveScope();

  void resolveScope(Collection c)
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      ((Node)i.next()).resolveScope();
  }

  VarScope getScope()
  {
    return scope;
  }

  TypeScope getTypeScope()
  {
    return typeScope;
  }

  VarScope scope;
  TypeScope typeScope;
}
