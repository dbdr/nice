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
//$Modified: Thu Aug 19 14:27:19 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

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
    
    Debug.println("Scoping (build  )");
    buildScope(null,null);

    Debug.println("Scoping (resolve)");
    doResolve();

    Debug.println("Typechecking");
    doTypecheck();
  }

  private static Collection findSymbols(Collection defs, boolean types)
  {
    Collection res=new ArrayList();
    Iterator i=defs.iterator();
    while(i.hasNext())
      {
	Object d=i.next();
	if(types && d instanceof ClassDefinition)
	  res.add(((ClassDefinition)d).tc);
	else if(types && d instanceof TypeSymbol)
	  res.add((TypeSymbol)d);
	else if(!types && d instanceof VarSymbol)
	  res.add(d);
	if(!types && d instanceof ClassDefinition)
	  res.addAll(((ClassDefinition)d).methodDefinitions());
      }
    return res;
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  public String toString()
  {
    String res="";
    Iterator i=definitions.iterator();
    while(i.hasNext()){
      res=res+i.next().toString();
    }
    return res;
  }

  private List /* of Definition */ definitions;
}

