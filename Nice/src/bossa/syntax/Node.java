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
//$Modified: Tue Aug 24 12:19:09 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Basic component of the syntax tree.
 * Defines its local scope.
 */
abstract class Node
{
  static final int forward = 0;
  static final int global  = 1;
  static final int down    = 2;
  static final int none    = 3;
  
  Node(int propagate)
  {
    this(null,propagate);
  }
  
  Node(List /* of Node */ children, 
       int propagate)
  {
    this.propagate=propagate;
    this.children=new ArrayList();
    this.varSymbols=new ArrayList();
    this.typeSymbols=new ArrayList();
    typeMapsNames=new ArrayList();
    typeMapsSymbols=new ArrayList();

    addChildren(children);
  }

  void addChild(Node n)
  {
    Internal.error(n==null,"null child in Node.addChild");
    children.add(n);
  }
  
  void addChildren(List c)
  {
    if(c==null) return;
    for(Iterator i=c.iterator();
	i.hasNext();)
      addChild((Node) i.next());
  }
    
  void addSymbol(VarSymbol s)
  {
    varSymbols.add(s);
  }
  
  void addSymbols(Collection c)
  {
    if(c!=null)
      varSymbols.addAll(c);
  }
  
  void addTypeSymbol(TypeSymbol s)
  {
    typeSymbols.add(s);
  }
  
  void addTypeSymbols(Collection c)
  {
    if(c!=null)
      typeSymbols.addAll(c);
  }
  
  void addTypeMaps(Collection names, Collection symbols)
    throws BadSizeEx
  {
    if(names.size()!=symbols.size()) throw new BadSizeEx(symbols.size(),names.size());
    typeMapsNames.addAll(names);
    typeMapsSymbols.addAll(symbols);
  }
  
  /** Sets up the scope, once the outer scope is given 
   * return the scope to pass to the following brothers 
   * (for forward scoping)
   */
  VarScope buildScope(VarScope outer, TypeScope typeOuter)
  // Default behaviour, must be overriden in nodes
  // that really define a new scope
  {
    Internal.error(this.scope!=null,"Scope set twice for "+this);

    VarScope res=null;
    switch(propagate)
      {
      case down: 
	this.scope=new VarScope(outer,varSymbols);
	this.typeScope=new TypeScope(typeOuter);
	res=outer;
	break;

      case global:
	if(outer==null)
	  outer=new VarScope(null);
	outer.addSymbols(varSymbols);
	this.scope=outer;
	if(typeOuter==null)
	  typeOuter=new TypeScope(null);
	this.typeScope=typeOuter;
	res=outer;
	break;
	
      case forward:
	this.scope=new VarScope(outer,varSymbols);
	this.typeScope=new TypeScope(typeOuter);
	res=this.scope;
	break;

      case none:
	res=null;
	break;
	
      default:
	Internal.error("Invalid case in Node.buildScope");
      }

    this.typeScope.addSymbols(typeSymbols);
    this.typeScope.addMappings(typeMapsNames,typeMapsSymbols);

    VarScope currentScope=this.scope;
    Iterator i=children.iterator();
    while(i.hasNext())
      {
	Object d=i.next();
	if(d!=null)
	  currentScope=((Node)d).buildScope(currentScope,typeScope);
      }

    return res;
  }
  
  /****************************************************************
   * Scoping resolution
   ****************************************************************/

  /** uses the scope to replace identifiers with their meaning */
  void resolve()
  {
  }

  void doResolve()
  {
    resolve();
    Iterator i=children.iterator();
    while(i.hasNext())
      ((Node)i.next()).doResolve();
  }

  VarScope getScope()
  {
    return scope;
  }

  TypeScope getTypeScope()
  {
    return typeScope;
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  /** override this when typechecking is needed */
  void typecheck() { }

  void doTypecheck()
  {
    typecheck();
    Iterator i=children.iterator();
    while(i.hasNext())
      ((Node)i.next()).doTypecheck();
  }

  /****************************************************************
   * Misc
   ****************************************************************/

  /**
   * Creates a reference to an expression, 
   * adds it as a child and returns it.
   */
  ExpressionRef expChild(Expression value)
  {
    ExpressionRef res=new ExpressionRef(value);
    addChild(res);
    return res;
  }

  protected VarScope scope;
  protected TypeScope typeScope;

  private List children, varSymbols, typeSymbols;
  private int propagate;  
  private List typeMapsSymbols,typeMapsNames;
}
