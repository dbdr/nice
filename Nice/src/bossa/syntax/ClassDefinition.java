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

// File    : ClassDefinition.java
// Created : Thu Jul 01 11:25:14 1999 by bonniot
//$Modified: Thu Apr 27 18:52:29 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;

import gnu.bytecode.*;
import java.util.*;

/**
 * Abstract syntax for a class definition.
 */
abstract public class ClassDefinition extends Definition
{
  /**
   * Creates a class definition.
   *
   * @param name the name of the class
   * @param isFinal 
   * @param isAbstract 
   * @param isInterface true iff the class is an "interface" 
   (which is not an "abstract interface").
   isInterface implies isAbstract.
   * @param isSharp true iff it's a #class
   * @param typeParameters a list of type symbols
   * @param extensions a list of TypeConstructors
   * @param implementations a list of Interfaces
   * @param abstractions a list of Interfaces
   * @param fields a list of ClassDefinition.Field
   * @param methods a list of MethodDefinition. Class methods.
   */
  public ClassDefinition(LocatedString name, 
			 boolean isFinal, boolean isAbstract, 
			 boolean isInterface,
			 List typeParameters,
			 List extensions, List implementations, List abstractions
			 )
  {
    super(name, Node.upper);

    if(isInterface)
      isAbstract=true;
    
    // Testing well formedness
    if(isInterface)
      {
	if(isFinal)
	  User.error(name,
		     "Interfaces can't be final");
	
	if(implementations!=null && implementations.size()>0)
	  User.error(name,
		     "Interfaces can't implement anything");
	
	if(abstractions!=null && abstractions.size()>0)
	  User.error(name,
		     "Interfaces can't abstract anything");	  
      }
    
    this.simpleName = this.name.toString();
    this.name.prepend(module.getName()+".");
    
    this.isFinal=isFinal;
    this.isAbstract=isAbstract;
    this.isInterface=isInterface;

    if(typeParameters==null)
      this.typeParameters = new ArrayList(0);
    else
      {
	this.typeParameters = typeParameters;
	addTypeSymbols(typeParameters);    
      }

    this.extensions=extensions;

    this.tc=new TypeConstructor(this);
    addTypeSymbol(this.tc);
    
    this.implementations=addChildren(implementations);
    this.abstractions=addChildren(abstractions);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }

  /****************************************************************
   * Selectors
   ****************************************************************/

  Constraint getConstraint()
  {
    return new Constraint(typeParameters,null);
  }

  Polytype getType()
  {
    return new Polytype(new Constraint(typeParameters,null),getMonotype());
  }

  /**
   * Returns the 'Monotype' part of the type.
   * Private use only (to compute the type of the field access methods).
   */
  private Monotype getMonotype()
  {
    return new MonotypeConstructor
      (this.tc,
       TypeParameters.fromSymbols(typeParameters),
       name.location());
  }
  
  abstract public boolean isConcrete();

  /****************************************************************
   * Resolution
   ****************************************************************/

  void resolve()
  {
    extensions=TypeConstructor.resolve(typeScope,extensions);

    // A class cannot extend an interface
    if(!isInterface)
      for(Iterator e = extensions.iterator(); e.hasNext();)
	{
	  TypeConstructor tc = (TypeConstructor) e.next();
	  if(tc.getDefinition() !=null &&
	     tc.getDefinition().getAssociatedInterface()!=null)
	    User.error(name,
		       tc+" is an interface, so "+name+
		       " may only implement it");
	}

    createAssociatedInterface();
  }

  /****************************************************************
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
    try{
      try{
	Typing.initialLeq(tc,extensions);
      }
      catch(KindingEx e){
	User.error(name,
		   "Class "+name+" cannot extend "+e.t2
		   +": they do not have the same kind");
      }
      
      Typing.assertImp(tc,implementations,true);
      if(associatedInterface!=null)
	Typing.assertImp(tc, associatedInterface, true);
      Typing.assertImp(tc,abstractions,true);

      if(implementsTop)
	Typing.assertImp(tc,
			 InterfaceDefinition.top(typeParameters.size()),
			 true);

      Typing.assertAbs(tc,abstractions);
      if(isFinal)
	Typing.assertAbs(tc,InterfaceDefinition.top(typeParameters.size()));
    }
    catch(TypingEx e){
      User.error(name,"Error in class "+name+" : "+e.getMessage());
    }

    if(associatedInterface!=null)
      associatedInterface.createContext();
  }

  /****************************************************************
   * Module Interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print
      (
         (isFinal ? "final " : "")
       + (isInterface ? "interface " : 
	  (isAbstract ? "abstract " : "") + "class ")
       + simpleName
       + Util.map("<",", ",">",typeParameters)
       + Util.map(" extends ",", ","",extensions)
       + Util.map(" implements ",", ","",implementations)
       + Util.map(" finally implements ",", ","",abstractions)
      );
  }
  
  /****************************************************************
   * Class hierarchy
   ****************************************************************/

  // Bossa has multiple inheritance, java has not.
  // So we choose a class that will be described as the super-class
  // in the bytecode (javaSuperClass)

  // A child class of A whose javaSuperClass is not A
  // is called an "illegitimate" child

  ClassDefinition superClass(int n)
  {
    return ((TypeConstructor) extensions.get(n)).getDefinition();
  }
  
  /**
   * Our super-class in the bytecode.
   */
  ClassDefinition distinguishedSuperClass()
  {
    if(extensions.size()==0)
      return null;
    else
      return superClass(0);
  }
  
  /**
   * The abstract class associated with this class.
   * If no #class has been generated, returns this.
   */
  ClassDefinition abstractClass()
  {
    return this;
  }
  
  abstract Type javaClass();

  final static Type javaClass(ClassDefinition c)
  {
    if(c==null)
      return gnu.bytecode.Type.pointer_type;
    else
      return c.javaClass();
  }
  
  ClassType javaSuperClass()
  {
    Type res = javaClass(abstractClass().distinguishedSuperClass());
    if(!(res instanceof ClassType))
      Internal.error("Only _Array is not a class type, and it must be final");
    
    return (ClassType) res;
  }
  
  /**
   * illegitimateChildren is a list of ClassDefinition.
   * It is a list of classes that are below 'this'
   * but that have not 'this' in their (bytecode) super chain.
   * 
   * It would be possible to minimize this list (TODO !)
   */
  protected List /* of ClassDefinition */ illegitimateChildren = new LinkedList();
  protected List /* of ClassDefinition */ illegitimateParents  = new LinkedList();

  // child must be concrete
  protected void addIllegitimateChild(ClassDefinition child)
  {
    illegitimateChildren.add(child.abstractClass());
    child.illegitimateParents.add(this);
  }
  
  List getIllegitimateChildren()
  {
    return illegitimateChildren;
  }
  
  /**
   * Computes the minimal set S of classes below this concrete class C
   * such that for all concrete D,
   *
   * the test D<=C (where <= defined by Bossa (multiple-)inheritance)
   *
   * is equivalent to 
   * 
   * the test D<C or D<s for some s in S
   * (where < is the single-inheritance relation in the bytecode)
   *
   */
  public void computeIllegitimateChildren()
  {
  }
  
  // Illegitimate children computations are done while typechecking

  void typecheck()
  {
    computeIllegitimateChildren();
  }

  abstract protected void addFields(ClassType c);

  /****************************************************************
   * Associated interface
   ****************************************************************/

  private AssociatedInterface associatedInterface;

  /**
   * Returns the abstract interface associated to this class, or null.
   *
   * An associated abstract interface in created 
   * for each "interface" class.
   */
  public AssociatedInterface getAssociatedInterface()
  { return associatedInterface; }
    

  private void createAssociatedInterface()
  {
    if(!isInterface)
      return;

    // the associated interface extends the associated interfaces
    // of the classes we extend
    List ext = new LinkedList();
    for(Iterator i = extensions.iterator(); i.hasNext();)
      {
	TypeConstructor tc = (TypeConstructor) i.next();
	ClassDefinition c = tc.getDefinition();
	if(c==null)
	  Internal.error(name,
			 "Superclass "+tc+
			 "("+tc.getClass()+")"+
			 " of "+name+
			 " has no definition");
	
	InterfaceDefinition ai = c.getAssociatedInterface();
	if(ai==null)
	  Internal.error("We should extend only \"interface\" classes");
	ext.add(new Interface(ai));
      }
    
    associatedInterface = new AssociatedInterface
      (name, typeParameters, ext, this);
    
    addChild(associatedInterface);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "class "+name;
  }

  /** The name of the class without package qualification. */
  String simpleName;
  
  TypeConstructor tc;
  List /* of TypeSymbol */ typeParameters;
  protected List
    /* of TypeConstructor */ extensions,
    /* of Interface */ implementations,
    /* of Interface */ abstractions;
  protected boolean isFinal, isInterface;
  protected boolean implementsTop = true;
  boolean isAbstract;
}
