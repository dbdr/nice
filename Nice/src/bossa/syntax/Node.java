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
//$Modified: Thu Feb 03 13:36:04 2000 by Daniel Bonniot $

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

  void addChild(Node n)
  {
    Internal.error(n==null,"null child in Node.addChild");
    children.add(n);
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

  private static final VarScope globalScope=new VarScope(null);
  private static final TypeScope globalTypeScope;
  static
  {
    globalTypeScope = new TypeScope(null);
    JavaTypeConstructor.addJavaTypes(globalTypeScope);
  }
  public static TypeScope getGlobalTypeScope()
  {
    return globalTypeScope;
  }
  
  void buildScope(bossa.modules.Module module)
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
      case down: 
	this.scope=new VarScope(outer,varSymbols);
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
    try{ 
      this.typeScope.addMappings(typeMapsNames,typeMapsSymbols);
    }
    catch(BadSizeEx e){
      Internal.error(e.toString());
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

    if(Debug.resolution)
      Debug.println("Resolved to "+this);
    
    Iterator i=children.iterator();
    while(i.hasNext())
      ((Node)i.next()).doResolve();
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

  /** override this when typechecking is needed */
  void typecheck() { }

  /** 
   * Usefull when children are to be typechecked in a context
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
