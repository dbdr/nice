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
//$Modified: Mon Jun 19 11:02:03 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import mlsub.typing.*;

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
   * @param isConcrete
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
   */
  public ClassDefinition(LocatedString name, 
			 boolean isConcrete,
			 boolean isFinal, boolean isAbstract, 
			 boolean isInterface,
			 List typeParameters,
			 List extensions, 
			 List implementations, List abstractions
			 )
  {
    super(name, Node.upper);

    this.isConcrete = isConcrete;
    
    if(isInterface)
      isAbstract = true;
    
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
    
    this.isFinal = isFinal;
    this.isAbstract = isAbstract;
    this.isInterface = isInterface;
    
    if(typeParameters==null)
      this.typeParameters = new ArrayList(0);
    else
      {
	this.typeParameters = typeParameters;
	//addTypeSymbols(typeParameters);    
      }
    this.variance = mlsub.typing.Variance.make(typeParameters.size());
    
    this.extensions = extensions;

    this.tc = new mlsub.typing.TypeConstructor
      (this.name.toString(), variance, isConcrete);

    if(isInterface)
      associatedInterface = new Interface(variance, tc);

    tcToClassDef.put(tc, this);
    Typing.introduce(tc);
    addTypeSymbol(this.tc);    
    
    this.implementations = implementations;
    this.abstractions = abstractions;
  }
  
  public Collection associatedDefinitions()
  {
    return null;
  }

  private Variance variance;
  public  Variance variance()
  {
    return variance;
  }
  
  /****************************************************************
   * Map TypeConstructors to ClassDefinitions
   ****************************************************************/

  private static final HashMap tcToClassDef = new HashMap();
  
  static final ClassDefinition get(TypeConstructor tc)
  {
    return (ClassDefinition) tcToClassDef.get(tc);
  }
  
  /****************************************************************
   * Selectors
   ****************************************************************/

  List getTypeParameters()
  {
    return typeParameters;
  }

  /** create type parameters with the same names as in the class */
  mlsub.typing.MonotypeVar[] createSameTypeParameters()
  {
    mlsub.typing.MonotypeVar[] thisTypeParams;
    if(tc.arity()>0)
      {
	thisTypeParams = new mlsub.typing.MonotypeVar[tc.arity()];
	List tp = getTypeParameters();
	for(int i=0; i<thisTypeParams.length; i++)
	  thisTypeParams[i] = new MonotypeVar(tp.get(i).toString());
      }
    else
      thisTypeParams = null;
    return thisTypeParams;
  }

  /*
  Polytype getType()
  {
    return new Polytype(new Constraint(typeParameters,null), getMonotype());
  }
  */
  /**
   * Returns the 'Monotype' part of the type.
   * Private use only (to compute the type of the field access methods).
   */
  /*
  protected Monotype getMonotype()
  {
    return new MonotypeConstructor
      (new TypeIdent(this.name), //TODO: optimize, no lookup
       TypeParameters.fromMonotypeVars(typeParameters),
       name.location());
  }
  */
  protected mlsub.typing.Monotype lowlevelMonotype()
  {
    return new mlsub.typing.MonotypeConstructor
      (tc, typeParameters.size()==0 
       ? null : 
       (mlsub.typing.Monotype[]) typeParameters.toArray(new MonotypeVar[typeParameters.size()]));
  }
  
  final boolean isConcrete;
  public final boolean isConcrete()
  {
    return isConcrete;
  }

  /****************************************************************
   * Resolution
   ****************************************************************/

  void resolve()
  {
    // if superClass is non null, it was build before
    // extensions should be null in that case
    if(superClass==null)
      {
	superClass = TypeIdent.resolveToTC(typeScope, extensions);
	extensions = null;
      }
    
    impl = TypeIdent.resolveToItf(typeScope, implementations);
    abs = TypeIdent.resolveToItf(typeScope, abstractions);
    
    implementations = abstractions = null;
    
    // A class cannot extend an interface
    if(!isInterface && superClass!=null)
      for(int i=0; i<superClass.length; i++)
	{
	  ClassDefinition def = ClassDefinition.get(superClass[i]);
	  if(def!=null && def.getAssociatedInterface()!=null)
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
	if(superClass!=null)
	  Typing.initialLeq(tc, superClass);
      }
      catch(KindingEx e){
	User.error(name,
		   "Class "+name+" cannot extend "+e.t2+
		   ": they do not have the same kind");
      }
      
      if(impl!=null)
	Typing.assertImp(tc,impl,true);

      if(associatedInterface!=null)
	Typing.assertImp(tc, associatedInterface, true);
      
      if(abs!=null)
	{
	  Typing.assertImp(tc, abs, true);
	  Typing.assertAbs(tc, abs);
	}
      
      if(implementsTop())
	Typing.assertImp(tc, variance.top, true);
      if(isFinal)
	Typing.assertAbs(tc, variance.top);
    }
    catch(TypingEx e){
      User.error(name,"Error in class "+name+" : "+e.getMessage());
    }

    //if(associatedInterface!=null)
    //associatedInterface.createContext();
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
       + Util.map(" extends ",", ","",superClass)
       + Util.map(" implements ",", ","",impl)
       + Util.map(" finally implements ",", ","",abs)
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

  final ClassDefinition superClass(int n)
  {
    return get(superClass[n]);
  }
  
  /**
   * Our super-class in the bytecode.
   */
  ClassDefinition distinguishedSuperClass()
  {
    if(superClass == null)
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
    ClassDefinition abs = abstractClass();
    Type res;
    
    if(abs.superClass == null)
      res = gnu.bytecode.Type.pointer_type;
    else
      res = bossa.CodeGen.javaType(abs.superClass[0]);
    
    if(!(res instanceof ClassType))
      Internal.error("Java type="+res+"\nOnly special arrays are not a class type, and they must be final");
    
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

  private mlsub.typing.Interface associatedInterface;

  /**
   * Returns the abstract interface associated to this class, or null.
   *
   * An associated abstract interface in created 
   * for each "interface" class.
   */
  public mlsub.typing.Interface getAssociatedInterface()
  { return associatedInterface; }
    
  private void createAssociatedInterface()
  {
    if(associatedInterface==null)
      return;

    // the associated interface extends the associated interfaces
    // of the classes we extend
    if(superClass!=null)
      for(int i = 0; i<superClass.length; i++)
	{
	  ClassDefinition c = ClassDefinition.get(superClass[i]);
	  if(c==null)
	    Internal.error(name,
			   "Superclass "+tc+
			   "("+tc.getClass()+")"+
			   " of "+name+
			   " has no definition");
	
	  Interface ai = c.getAssociatedInterface();
	  if(ai==null)
	    Internal.error("We should extend only \"interface\" classes");
	
	  try{
	    Typing.assertLeq(associatedInterface, ai);
	  }
	  catch(KindingEx e){
	    User.error(this, "Cannot extend interface " + ai + 
		       " which has a different variance");
	  }
	}
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
  
  mlsub.typing.TypeConstructor tc;

  List /* of TypeSymbol */ typeParameters;
  protected List
    /* of TypeConstructor */ extensions,
    /* of Interface */ implementations,
    /* of Interface */ abstractions;
  TypeConstructor[] superClass;
  Interface[] impl, abs;
  protected boolean isFinal, isInterface;
  
  abstract protected boolean implementsTop();
  
  boolean isAbstract;
}
