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
  
  void removeChild(Node n)
  {
    if(children == null || !children.contains(n))
      Internal.error(n+" is not a child of "+this);

    children.remove(n);
  }

  void removeChildren()
  {
     children = null;
  }
  
  /**
   * Always returns the argument (except an empty list for 'null').
   * This is just a convenience to be able to write 'this.f = addChildren(f)'.
   */
  List addChildren(List c)
  {
    if (c == null)
      return Collections.EMPTY_LIST;

    for(Iterator i = c.iterator(); i.hasNext();)
      addChild((Node) i.next());

    return c;
  }

  void addSymbol(/*VarSymbol*/Symbol s)
  {
    if (varSymbols == null)
      varSymbols = new ArrayList();
    varSymbols.add(s);
  }
  
  void addTypeSymbol(TypeSymbol s)
  {
    addTypeMap(s.toString(), s);
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
  
  /**
   * Scopes shared by all modules.
   */

  private static TypeScope globalTypeScope;
  public static final TypeScope getGlobalTypeScope()
  {
    return globalTypeScope;
  }

  public static final bossa.modules.Package getGlobalTypeScopeModule()
  {
    return globalTypeScope.getPackage();
  }

  public static mlsub.typing.TypeConstructor globalTypeScopeLookup(String name, Location loc)
  {
    return globalTypeScope.globalLookup(name, loc);
  }

  public static bossa.modules.Compilation compilation = null;

  public static void setPackage(bossa.modules.Package pkg)
  {
    // For multiple compilations in the same JVM:
    // If we are starting a new compilation, reset the global scopes.
    if (compilation != pkg.getCompilation())
      {
        compilation = pkg.getCompilation();
	globalTypeScope = dispatch.createGlobalTypeScope();
      }
    globalTypeScope.setPackage(pkg);
  }

  void buildScope(bossa.modules.Package pkg)
  {
    setPackage(pkg);
    buildScope(null, globalTypeScope);
  }
  
  /** 
      Sets up the scope, once the outer scope is given. 
  */
  void buildScope(VarScope outer, TypeScope typeOuter)
  {
    //if (this.scope != null)
    //Internal.error("Scope set twice for " + this + this.getClass());

    if (this instanceof Definition)
      outer = ((Definition) this).module.scope;

    switch(propagate)
      {
      case none:
        this.scope = outer;
        this.typeScope = typeOuter;
        break;
      case down: 
	this.scope = new VarScope(outer,varSymbols);
	this.typeScope = new TypeScope(typeOuter);
	break;

      case global:
	this.scope = outer;
	this.scope.addSymbols(varSymbols);
	typeOuter = globalTypeScope;
	this.typeScope = typeOuter;
	break;
	
      case upper:
	if(outer==null)
	  outer = new VarScope(null);
	outer.addSymbols(varSymbols);
	this.scope = outer;
	if(typeOuter==null)
	  typeOuter = new TypeScope(null);
	this.typeScope = typeOuter;
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
	  if (this instanceof Located)
	    User.error(((Located) this).location(), e.getMessage());
	  else
	    /// XXX This error will not be located.
	    User.error(e);
	}
      }

    // They won't be used anymore
    // Let's enable the memory to be reclamed
    varSymbols = null;
    typeMapsSymbols = null;
    typeMapsNames = null;

    // builds the scope of the children
    if (children != null)
      {
	for(Iterator i = children.iterator();i.hasNext();)
	  {
	    Node d = (Node) i.next();
	    d.buildScope(this.scope, this.typeScope);
	  }
      }
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
   * Type checking
   ****************************************************************/

  // The current function should be saved in nodes that need it
  // during execution of their typecheck method.
  static /*Function*/Object currentFunction;
  static /*Function*/Object getCurrentFunction() { return currentFunction; }
  static void setCurrentFunction(/*Function*/Object f) { currentFunction = f; }
  
  /** The this parameter of the current function, or null. */
  static Expression thisExp;

  /** override this when typechecking is needed. */
  void typecheck() { }

  final void doTypecheck()
  {
    // avoid to typecheck twice
    // usefull in bossa.syntax.Block for instance
    if (typecheckingDone)
      {
	//Internal.warning("Attempt to typecheck twice " + this + ", a " + getClass());
	return;
      }
    typecheckingDone = true;

    typecheck();
    
    if (children != null)
      for(Iterator i = children.iterator();i.hasNext();)
        try{
          ((Node)i.next()).doTypecheck();
        }
        catch(UserError ex){
          globalTypeScope.getPackage().getCompilation().error(ex);
	}
  }

  private boolean typecheckingDone = false;
  
  /****************************************************************
   * Misc
   ****************************************************************/

  protected VarScope scope;
  protected TypeScope typeScope;

  protected List children;
  private List varSymbols;
  int propagate;  
  private List typeMapsSymbols,typeMapsNames;
}
