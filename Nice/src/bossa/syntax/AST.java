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
//$Modified: Fri Jul 09 19:31:35 1999 by bonniot $
// Description : the Abstract Syntax Tree, father of all nodes
//   May its mighty name be blessed

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class AST extends Node
{
  public AST(Collection defs)
  {
    this.definitions=defs;
    this.scope=VarScope.makeScope(null,findSymbols(defs,false));
    this.typeScope=TypeScope.makeScope(null,findSymbols(defs,true));
    buildScope();
    linkMethodBodiesToDefinitions();
    resolveScope();
  }

  private void buildScope()
  {
    Iterator i=definitions.iterator();
    while(i.hasNext())
      {
	Object d=i.next();
	if(d instanceof Node && ! (d instanceof MethodBodyDefinition))
	  ((Node)d).buildScope(scope,typeScope);
      }
  }

  private void linkMethodBodiesToDefinitions()
  {
    Iterator i=definitions.iterator();
    while(i.hasNext())
      {
	Object d=i.next();
	if(d instanceof MethodBodyDefinition)
	  {
	    MethodBodyDefinition m=(MethodBodyDefinition)d;
	    m.setDefinition((MethodDefinition)scope.lookup(m.name));
	  }
      }						  
  }

  void resolveScope()
  {
    Iterator i=definitions.iterator();
    while(i.hasNext())
      ((Node)i.next()).resolveScope();
  }

  private Collection findSymbols(Collection defs, boolean types)
  {
    Collection res=new ArrayList();
    Iterator i=defs.iterator();
    while(i.hasNext())
      {
	Object d=i.next();
	if(types && d instanceof TypeSymbol
	   || !types && d instanceof VarSymbol)
	  res.add(d);
	if(!types && d instanceof ClassDefinition)
	  res.addAll(((ClassDefinition)d).methodDefinitions());
      }
    return res;
  }

  public String toString()
  {
    String res="";
    Iterator i=definitions.iterator();
    while(i.hasNext()){
      res=res+i.next().toString();
    }
    return res;
  }

  private Collection /* of Definition */ definitions;
}

