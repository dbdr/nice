/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import mlsub.typing.TypeSymbol;

/**
   Basic component of the syntax tree.
   Defines its local scope.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr) 
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
    this.propagate = propagate;
  }
  
  Node(List /* of Node */ children, 
       int propagate)
  {
    this(propagate);
    addChildren(children);
  }

  /** child(n) is prefered now */
  void addChild(Node n)
  {
    if (n==null)
      Internal.error("null child in Node.addChild for node "+this);

    if (children == null) children=new ArrayList();
    children.add(n);
  }
  
  final Node child(Node n)
  {
    if(n==null)
      return null;

    if (children == null) children=new ArrayList();
    children.add(n);
    return n;
  }
  
  final Statement child(Statement n)
  {
    if(n==null)
      return null;

    if (children == null) children=new ArrayList();
    children.add(n);
    return n;
  }
  
  void removeChild(Node n)
  {
    if(children == null || !children.contains(n))
      Internal.error(n+" is not a child of "+this);

    children.remove(n);
  }
  
  void removeChildren(List c)
  {
    if(c==null) return;
    for(Iterator i = c.iterator();
	i.hasNext();)
      removeChild((Node) i.next());
  }

  /**
   * Always returns the argument (except an empty list for 'null').
   * This is just a convenience to be able to write 'this.f = addChildren(f)'.
   */
  List addChildren(List c)
  {
    if(c==null) return new LinkedList();
    for(Iterator i = c.iterator(); i.hasNext();)
      addChild((Node) i.next());

    return c;
  }
    
  void addSymbol(VarSymbol s)
  {
    if (varSymbols == null)
      varSymbols = new ArrayList();
    varSymbols.add(s);
  }
  
  void addSymbols(Collection c)
  {
    if(c!=null)
      {
	if (varSymbols == null)
	  varSymbols = new ArrayList();
	varSymbols.addAll(c);
      }
  }
  
  void addTypeSymbol(TypeSymbol s)
  {
    addTypeMap(s.toString(), s);
  }
  
  void addTypeSymbols(Collection c)
  {
    if(c!=null)
      for(Iterator i=c.iterator(); i.hasNext();)
	{
	  TypeSymbol s = (TypeSymbol) i.next();
	  addTypeMap(s.toString(), s);
	}
  }
  
  void addTypeMap(String name, TypeSymbol symbol)
  {
    if (typeMapsNames == null)
      {
	typeMapsNames = new ArrayList();
	typeMapsSymbols = new ArrayList();
      }
    typeMapsNames.add(name);
    typeMapsSymbols.add(symbol);
  }
  
  void addTypeMaps(Collection names, Collection symbols)
  {
    if(names.size()!=symbols.size()) 
      throw new Error(symbols.size()+" != "+names.size());
    
    if (typeMapsNames == null)
      {
	typeMapsNames = new ArrayList();
	typeMapsSymbols = new ArrayList();
      }

    for(Iterator n = names.iterator();n.hasNext();)
      typeMapsNames.add(((LocatedString) n.next()).toString());
    typeMapsSymbols.addAll(symbols);
  }
  
  class Scopes
  {
    Scopes(VarScope v, TypeScope t)
    { scope = v; typeScope = t; }

    VarScope scope;
    TypeScope typeScope;
  }
 
  /**
   * Scopes shared by all modules.
   */

  private static final VarScope globalScope = new VarScope(null);
  public static final VarScope getGlobalScope()
  {
    return globalScope;
  }
  
  private static final TypeScope globalTypeScope= new TypeScope(null);
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
  {
    if(scope!=null)
      scope = null;
    if(this.scope!=null)
      Internal.error("Scope set twice for "+this);

    Scopes res = null;
    switch(propagate)
      {
      case none:
      case down: 
	this.scope = new VarScope(outer,varSymbols);
	this.typeScope = new TypeScope(typeOuter);
	res = new Scopes(outer,typeOuter);
	break;

      case global:
	outer = globalScope;
	outer.addSymbols(varSymbols);
	this.scope = outer;
	typeOuter = globalTypeScope;
	this.typeScope = typeOuter;
	res = new Scopes(outer,typeOuter);
	break;
	
      case upper:
	if(outer==null)
	  outer = new VarScope(null);
	outer.addSymbols(varSymbols);
	this.scope = outer;
	if(typeOuter==null)
	  typeOuter = new TypeScope(null);
	this.typeScope = typeOuter;
	res = new Scopes(outer,typeOuter);
	break;
	
      case forward:
	this.scope = new VarScope(outer,varSymbols);
	this.typeScope = new TypeScope(typeOuter);
	res = new Scopes(this.scope,this.typeScope);
	break;

      default:
	Internal.error("Invalid case in Node.buildScope");
      }

    if(propagate!=none)
      {
	try{
	  if (typeMapsNames != null)
	    this.typeScope.addMappings
	      (typeMapsNames, (TypeSymbol[]) 
	       typeMapsSymbols.toArray(new TypeSymbol[typeMapsSymbols.size()]));
	}
	catch(TypeScope.DuplicateName e){
	  User.error(e);
	}
      }

    // They won't be used anymore
    // Let's enable the memory to be reclamed
    varSymbols = typeMapsSymbols = typeMapsNames = null;

    // builds the scope of the children
    if (children != null)
      {
	Scopes current = new Scopes(this.scope,this.typeScope);
	for(Iterator i = children.iterator();i.hasNext();)
	  {
	    Object d = i.next();
	    //if(d!=null)
	    current = ((Node)d).buildScope(current.scope,current.typeScope);
	  }
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
      Debug.println("Resolving " + this + " [" + this.getClass() + "]");
    
    resolve();

    // They won't be used anymore
    // Let's enable the memory to be reclamed
    scope = null;
    typeScope = null;

    if (children != null)
      for(Iterator i = children.iterator();i.hasNext();)
	((Node)i.next()).doResolve();

    if(Debug.resolution)
      Debug.println("Resolved to "+this + " [" + this.getClass() + "]");
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
    if (children != null)
      for(Iterator i = children.iterator();i.hasNext();)
	((Node)i.next()).doFindJavaClasses();
  }
  
  /****************************************************************
   * Type checking
   ****************************************************************/

  // The current function should be saved in nodes that need it
  // during execution of their typecheck method.
  static Function currentFunction;
  
  /** override this when typechecking is needed. */
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
    // avoid to typecheck twice
    // usefull in bossa.syntax.Block for instance
    if (typecheckingDone)
      return;
    typecheckingDone = true;
    
    Function savedFunction = null;
    
    try{
      typecheck();

      if(this instanceof Function)
	{
	  savedFunction = currentFunction;
	  currentFunction = (Function) this;
	}
    
      if (children != null)
	for(Iterator i = children.iterator();i.hasNext();)
	  ((Node)i.next()).doTypecheck();
    }
    finally{
      if(savedFunction != null)
	currentFunction = savedFunction;
      
      // it might be necessary to have this endTypecheck guarded by 
      // this finally, as a leave() might be in it
      endTypecheck();
    }
  }

  private boolean typecheckingDone = false;
  
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
    
    ExpressionRef res = new ExpressionRef(value);
    addChild(res);
    return res;
  }

  /**
   * Creates references to expressions.
   */
  List expChildren(List values)
  {
    List res = new ArrayList(values.size());
    
    for(Iterator i = values.iterator();i.hasNext();)
      res.add(expChild((Expression) i.next()));
    
    return res;
  }

  /*
    Creates references to expressions.
  */
  Expression[] expTabChildren(List values)
  {
    Expression[] res = new Expression[values.size()];

    int n = 0;
    for (Iterator i = values.iterator();i.hasNext();)
      res[n++] = expChild((Expression) i.next());

    return res;
  }

  protected VarScope scope;
  protected TypeScope typeScope;

  protected List children;
  private List varSymbols;
  int propagate;  
  private List typeMapsSymbols,typeMapsNames;
}
