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
//$Modified: Fri Aug 27 10:34:13 1999 by bonniot $

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
  
  class Scopes
  {
    Scopes(VarScope v, TypeScope t)
    { scope=v; typeScope=t; }

    VarScope scope;
    TypeScope typeScope;
  }
  
  void buildScope()
  {
    buildScope(null,null);
  }
  
  /** Sets up the scope, once the outer scope is given 
   * return the scope to pass to the following brothers 
   * (for forward scoping)
   */
  Scopes buildScope(VarScope outer, TypeScope typeOuter)
  // Default behaviour, must be overriden in nodes
  // that really define a new scope
  {
    Internal.error(this.scope!=null,"Scope set twice for "+this);

    Scopes res=null;
    switch(propagate)
      {
      case down: 
	this.scope=new VarScope(outer,varSymbols);
	this.typeScope=new TypeScope(typeOuter);
	res=new Scopes(outer,typeOuter);
	break;

      case global:
	if(outer==null)
	  outer=new VarScope(null);
	outer.addSymbols(varSymbols);
	this.scope=outer;
	if(typeOuter==null)
	  typeOuter=new TypeScope(null);
	this.typeScope=typeOuter;
	res=new Scopes(outer,typeOuter);
	break;
	
      case forward:
	this.scope=new VarScope(outer,varSymbols);
	this.typeScope=new TypeScope(typeOuter);
	res=new Scopes(this.scope,this.typeScope);
	break;

      case none:
	res=null;
	break;
	
      default:
	Internal.error("Invalid case in Node.buildScope");
      }

    this.typeScope.addSymbols(typeSymbols);
    this.typeScope.addMappings(typeMapsNames,typeMapsSymbols);

    // builds the scope of the children
    Scopes current=new Scopes(this.scope,this.typeScope);
    Iterator i=children.iterator();
    while(i.hasNext())
      {
	Object d=i.next();
	if(d!=null)
	  current=((Node)d).buildScope(current.scope,current.typeScope);
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

  /** 
   * Usefull when children are to be typechecked in a content
   * defined by this Node :
   * the Typing.enter() goes in typecheck()
   * and the Typing.leave() goes here
   */
  void endTypecheck() { }
  
  void doTypecheck()
  {
    typecheck();
    Iterator i=children.iterator();
    while(i.hasNext())
      ((Node)i.next()).doTypecheck();
    endTypecheck();
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
