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
//$Modified: Thu Apr 27 19:13:55 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Basic component of the syntax tree.
 * Defines its local scope.
 */
abstract public class Node
{
  static final int forward = 0;
  static final int global  = 1;
  static final int down    = 2;
  static final int upper   = 3;
  static final int none    = 4;

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

  /** child(n) is prefered now */
  void addChild(Node n)
  {
    if(n==null)
      Internal.error("null child in Node.addChild for node "+this);

    children.add(n);
  }
  
  final Node child(Node n)
  {
    if(n==null)
      return null;

    children.add(n);
    return n;
  }
  
  final Statement child(Statement n)
  {
    if(n==null)
      return null;

    children.add(n);
    return n;
  }
  
  void removeChild(Node n)
  {
    if(!children.contains(n))
      Internal.error(n+" is not a child of "+this);

    children.remove(n);
  }
  
  void removeChildren(List c)
  {
    if(c==null) return;
    for(Iterator i=c.iterator();
	i.hasNext();)
      removeChild((Node) i.next());
  }

  /**
   * Always returns the argument (except an empty list for 'null').
   * This is just a convenience to be able to write 'this.f=addChildren(f)'.
   */
  List addChildren(List c)
  {
    if(c==null) return new LinkedList();
    for(Iterator i=c.iterator();
	i.hasNext();)
      addChild((Node) i.next());
    return c;
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
  
  void addTypeMap(String name, TypeSymbol symbol)
  {
    typeMapsNames.add(name);
    typeMapsSymbols.add(symbol);
  }
  
  void addTypeMaps(Collection names, Collection symbols)
    throws BadSizeEx
  {
    if(names.size()!=symbols.size()) throw new BadSizeEx(symbols.size(),names.size());
    
    for(Iterator n=names.iterator();n.hasNext();)
      typeMapsNames.add(((LocatedString) n.next()).toString());
    typeMapsSymbols.addAll(symbols);
  }
  
  class Scopes
  {
    Scopes(VarScope v, TypeScope t)
    { scope=v; typeScope=t; }

    VarScope scope;
    TypeScope typeScope;
  }
 
  /**
   * Scopes shared by all modules.
   */

  private static final VarScope globalScope=new VarScope(null, true);
  public static final VarScope getGlobalScope()
  {
    return globalScope;
  }
  
  private static final TypeScope globalTypeScope;
  static
  {
    globalTypeScope = new TypeScope(null);
  }
  public static final TypeScope getGlobalTypeScope()
  {
    return globalTypeScope;
  }
  
  void buildScope(bossa.modules.Package module)
  {
    globalTypeScope.module = module;
    
    buildScope(globalScope,globalTypeScope);
  }
  
  /** Sets up the scope, once the outer scope is given 
   * return the scope to pass to the following brothers 
   * (for forward scoping)
   */
  Scopes buildScope(VarScope outer, TypeScope typeOuter)
  // Default behaviour, must be overriden in nodes
  // that really define a new scope
  {
    if(scope!=null)
      scope=null;
    if(this.scope!=null)
      Internal.error("Scope set twice for "+this);

    Scopes res=null;
    switch(propagate)
      {
      case none:
      case down: 
	this.scope=new VarScope(outer,varSymbols, true);
	this.typeScope=new TypeScope(typeOuter);
	res=new Scopes(outer,typeOuter);
	break;

      case global:
	outer=globalScope;
	outer.addSymbols(varSymbols);
	this.scope=outer;
	typeOuter=globalTypeScope;
	this.typeScope=typeOuter;
	res=new Scopes(outer,typeOuter);
	break;
	
      case upper:
	if(outer==null)
	  outer=new VarScope(null, true);
	outer.addSymbols(varSymbols);
	this.scope=outer;
	if(typeOuter==null)
	  typeOuter=new TypeScope(null);
	this.typeScope=typeOuter;
	res=new Scopes(outer,typeOuter);
	break;
	
      case forward:
	this.scope=new VarScope(outer,varSymbols, false);
	this.typeScope=new TypeScope(typeOuter);
	res=new Scopes(this.scope,this.typeScope);
	break;

      default:
	Internal.error("Invalid case in Node.buildScope");
      }

    if(propagate!=none)
      {
	this.typeScope.addSymbols(typeSymbols);
	try{ 
	  this.typeScope.addMappings(typeMapsNames,typeMapsSymbols);
	}
	catch(BadSizeEx e){
	  Internal.error(e.toString());
	}
      }
    
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
  
  VarScope getScope()
  {
    return scope;
  }

  TypeScope getTypeScope()
  {
    return typeScope;
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
    if(Debug.resolution)
      Debug.println("Resolving "+this+" ["+this.getClass()+"]");
    
    resolve();

    Iterator i=children.iterator();
    while(i.hasNext())
      ((Node)i.next()).doResolve();

    if(Debug.resolution)
      Debug.println("Resolved to "+this);
  }

  /****************************************************************
   * Java Classes
   ****************************************************************/
  
  /**
   * Add the java classes in the expression to the rigid context.
   */
  void findJavaClasses() { }

  final void doFindJavaClasses()
  {
    findJavaClasses();
    for(Iterator i=children.iterator();
	i.hasNext();)
      ((Node)i.next()).doFindJavaClasses();
  }
  
  /****************************************************************
   * Type checking
   ****************************************************************/

  // The current function should be saved in nodes that need it
  // curing execution of their typecheck method.
  static Function currentFunction;
  
  /** override this when typechecking is needed */
  void typecheck() { }

  /** 
   * Usefull when children are to be typechecked in a context
   * defined by this Node :
   * the Typing.enter() goes in typecheck()
   * and the Typing.leave() goes here
   */
  void endTypecheck() { }
  
  final void doTypecheck()
  {
    typecheck();

    Function savedFunction=null;
    if(this instanceof Function)
      {
	savedFunction = currentFunction;
	currentFunction = (Function) this;
      }
    
    Iterator i=children.iterator();
    while(i.hasNext())
      ((Node)i.next()).doTypecheck();

    endTypecheck();

    if(savedFunction!=null)
      currentFunction=savedFunction;
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
    if(value==null)
      return null;
    
    ExpressionRef res=new ExpressionRef(value);
    addChild(res);
    return res;
  }

  /**
   * Creates references to expressions.
   */
  List expChildren(List values)
  {
    List res = new ArrayList(values.size());
    
    for(Iterator i=values.iterator();i.hasNext();)
      res.add(expChild((Expression) i.next()));
    
    return res;
  }

  protected VarScope scope;
  protected TypeScope typeScope;

  protected List children;
  private List varSymbols, typeSymbols;
  int propagate;  
  private List typeMapsSymbols,typeMapsNames;

  // Temporary, see MethodBodyDefinition
  //static boolean resolveIdents = true;
}
