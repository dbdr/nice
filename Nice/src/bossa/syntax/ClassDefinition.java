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
//$Modified: Wed Jan 26 17:49:32 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;

import gnu.bytecode.*;
import java.util.*;

/**
 * Abstract syntax for a class definition.
 */
public class ClassDefinition extends Node
  implements Definition
{
  public ClassDefinition(LocatedString name, 
			 boolean isFinal, boolean isAbstract,
			 boolean isSharp,
			 List typeParameters,
			 List extensions, List implementations, List abstractions,
			 List fields, List methods)
  {
    super(Node.global);
    
    this.name=name;
    this.isFinal=isFinal;
    this.isAbstract=isAbstract;
    this.isSharp=isSharp;

    if(typeParameters==null)
      this.typeParameters=new ArrayList(0);
    else
      this.typeParameters=typeParameters;
    this.extensions=extensions;

    this.fields=keepFields(fields,isSharp);

    this.tc=new TypeConstructor(this);
    addTypeSymbol(this.tc);
    
    if(isSharp)
      // The sharp class must not declare children, 
      // since the associated class has already done so.
      // Anyway, those should be null.
      {
	this.implementations=implementations;
	this.abstractions=abstractions;
	this.methods=methods;
	addChildren(computeAccessMethods(this.fields));

	classType = gnu.bytecode.ClassType.make
	  (name.toString().substring(1));
	classType.requireExistingClass(false);
      }
    else
      {
	this.implementations=addChildren(implementations);
	this.abstractions=addChildren(abstractions);
	this.methods=addChildren(methods);
	addChildren(computeAccessMethods(this.fields));

	if(isAbstract || isFinal)
	  // since no associated concrete class is created,
	  // it needs its own classtype
	  {
	    classType = gnu.bytecode.ClassType.make
	      (name.toString());
	    classType.requireExistingClass(false);
	  }
      }
    
    // if this class is final, 
    // no #class is created below.
    // the associated #class is itself
    if(isFinal && !isSharp)
      addTypeMap("#"+name.content,this.tc);
  }

  /**
   * Creates an immediate descendant, 
   * that abstracts and doesn't implement Top<n>.
   */
  public Collection associatedDefinitions()
  {
    if(isFinal || isAbstract)
      return new LinkedList();

    Collection res=new ArrayList(1);
    LocatedString name=new LocatedString("#"+this.name.content,this.name.location());
    List parent=new ArrayList(1);
    parent.add(this.tc);
    ClassDefinition c=new ClassDefinition
      (name,true,false,true,typeParameters,parent,
       new LinkedList(),new LinkedList(),fields,methods);
    associatedConcreteClass = c;
    concreteClasses.add(c);
    
    res.add(c);
    return res;
  }

  static private List concreteClasses = new LinkedList();

  static public ListIterator listConcreteClasses()
  {
    return concreteClasses.listIterator();
  }
  
  /****************************************************************
   * Fields
   ****************************************************************/

  private List keepFields(List fields, boolean local)
  {
    List result=new ArrayList(fields.size());
    for(Iterator i=fields.iterator();i.hasNext();)
      {
	Field f=(Field)i.next();
	if(f.isLocal==local)
	  result.add(f);
      }
    return result;
  }
  
  public static class Field
  {
    public Field(MonoSymbol sym, boolean isFinal, boolean isLocal)
    {
      this.sym=sym;
      this.isFinal=isFinal;
      this.isLocal=isLocal;
    }

    public String toString()
    {
      return 
	(isFinal ? "final" : "") +
	(isLocal ? "local" : "") +
	sym;
    }
    
    MonoSymbol sym;
    boolean isFinal;
    boolean isLocal;
  }
  
  private List computeAccessMethods(List fields)
  {
    List res=new ArrayList(fields.size());
    for(Iterator i=fields.iterator();i.hasNext();)
      {
	Field f=(Field)i.next();

	MonoSymbol s=f.sym;
	MethodDefinition m = 
	  new FieldAccessMethod(this,s.name,s.type,typeParameters);

	res.add(m);
      }
    return res;
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
  
  void resolve()
  {
    extensions=TypeConstructor.resolve(typeScope,extensions);
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
      Typing.assertImp(tc,abstractions,true);
      Typing.assertAbs(tc,abstractions);
      if(isFinal)
	Typing.assertAbs(tc,InterfaceDefinition.top(typeParameters.size()));
      if(!isSharp)
	Typing.assertImp(tc,InterfaceDefinition.top(typeParameters.size()),true);
    }
    catch(TypingEx e){
      User.error(name,"Error in class "+name+" : "+e.getMessage());
    }
  }

  /****************************************************************
   * Module Interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    if(!isSharp)
    s.print
      (
         (isFinal ? "final " : "")
       + (isAbstract ? "abstract " : "")
       + "class "
       + name.toString()
       + Util.map("<",", ",">",typeParameters)
       + Util.map(" extends ",", ","",extensions)
       + Util.map(" implements ",", ","",implementations)
       + Util.map(" finally implements ",", ","",abstractions)
       + " {\n"
       + Util.map("",";\n",";\n",fields)
       + Util.map(methods)
       + "}\n\n"
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

  private ClassType classType;
  
  ClassDefinition superClass(int n)
  {
    return ((TypeConstructor) extensions.get(n)).definition;
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
   * The abstract class associated with a concrete class.
   * pre: this.isSharp() is true
   */
  ClassDefinition abstractClass()
  {
    return ((TypeConstructor) extensions.get(0)).definition;
  }
  
  static ClassType javaClass(ClassDefinition c)
  {
    if(c==null)
      return gnu.bytecode.Type.pointer_type;
    
    if(c.associatedConcreteClass!=null)
      return javaClass(c.associatedConcreteClass);
    else
      {
	if(c.classType==null)
	  Internal.error(c.name,
			 c+" has no classType field");

	return c.classType;
      }
  }
  
  ClassType javaSuperClass()
  {
    if(isSharp)
      return abstractClass().javaSuperClass();
    
    return javaClass(distinguishedSuperClass());
  }
  
  /**
   * illegitimateChildren is a list of ClassDefinition.
   * It is a list of classes that are below 'this'
   * but that have not 'this' in their (bytecode) super chain.
   * 
   * It would be possible to minimize this list (TODO !)
   */
  private List /* of ClassDefinition */ illegitimateChildren = new LinkedList();
  private List /* of ClassDefinition */ illegitimateParents  = new LinkedList();

  // child must be concrete
  private void addIllegitimateChild(ClassDefinition child)
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
    if(!isSharp)
      return;
    
    // We are sharp, let's take our immediate parent
    ClassDefinition me = abstractClass();
    
    for(Iterator i = listConcreteClasses();
	i.hasNext();)
      {
	ClassDefinition concrete = (ClassDefinition) i.next();
	ClassDefinition that = concrete.abstractClass();
	
	if(
	   that!=me &&
	   this.tc.getKind()==that.tc.getKind() &&
	   Typing.testRigidLeq(that.tc,me.tc) &&
	   !Typing.testRigidLeq(that.distinguishedSuperClass().tc,me.tc)
	   )
	  me.addIllegitimateChild(concrete);
      }  

    if(Debug.linkTests && me.illegitimateChildren.size()>0)
      Debug.println(me.name+" has illegitimate childs "+
		    Util.map("",", ","",me.illegitimateChildren));
  }
  
  // Illegitimate children computations are done while typechecking

  void typecheck()
  {
    computeIllegitimateChildren();
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public void compile()
  {
    if(!isSharp && !(isAbstract || isFinal))
      return;
    
    classType.setModifiers(Access.PUBLIC 
			   | (isAbstract ? Access.ABSTRACT : 0)
			   | (isFinal    ? Access.FINAL    : 0)
			   );
    classType.setSuper(javaSuperClass());

    addFields(classType);
    if(isSharp) abstractClass().addFields(classType);

    for(Iterator i = illegitimateParents.iterator();
	i.hasNext();)
      ((ClassDefinition) i.next()).addFields(classType);
    
    gnu.bytecode.Method constructor = classType.addNewMethod("<init>", Access.PUBLIC, new gnu.bytecode.Type[0], gnu.bytecode.Type.void_type);

    constructor.initCode();
    gnu.bytecode.CodeAttr code = constructor.getCode();
    gnu.bytecode.Variable thisVar = new gnu.bytecode.Variable();
    thisVar.setName("this");
    thisVar.setType(classType);
    thisVar.setParameter(true);
    thisVar.allocateLocal(code);

    code.emitLoad(thisVar);
    code.emitInvokeSpecial(constructor(javaSuperClass()));
    code.emitReturn();
    
    module.addClass(classType);
  }

  private static gnu.bytecode.Method constructor(ClassType parent)
  {
    if(parent==null)
      parent = gnu.bytecode.Type.pointer_type;
    
    gnu.bytecode.Method res = parent.addNewMethod("<init>", Access.PUBLIC, new gnu.bytecode.Type[0], gnu.bytecode.Type.void_type);

    return res;
  }
  
  private void addFields(ClassType c)
  {
    for(Iterator i=fields.iterator();
	i.hasNext();)
      {
	Field f=(Field)i.next();
	gnu.bytecode.Field field=
	  c.addField(f.sym.name.toString(),
		     f.sym.type.getJavaType(),
		     Access.PUBLIC);
      }
  }
  
  /****************************************************************
   * Module
   ****************************************************************/
  
  private bossa.modules.Module module;
  
  public void setModule(bossa.modules.Module module)
  {
    this.module = module;
    for(Iterator i = children.iterator();
	i.hasNext();)
      {
	Object child = i.next();
	if(child instanceof Definition)
	  ((Definition) child).setModule(module);
      }
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return
      "class "
      + name.toString()
      + Util.map("<",", ",">",typeParameters)
      + Util.map(" extends ",", ","",extensions)
      + Util.map(" implements ",", ","",implementations)
      + Util.map(" abstract ",", ","",abstractions)
      + " {\n"
      + Util.map("",";\n",";\n",fields)
      + Util.map(methods)
      + "}\n\n"
      ;
  }

  Collection methodDefinitions()
  {
    return methods;
  }

  LocatedString name;
  TypeConstructor tc;
  List /* of TypeSymbol */ typeParameters;
  private List /* of TypeConstructor */ extensions;
  private List /* of Interface */ implementations;
  private List /* of Interface */ abstractions;
  private List /* of ClassDefinition.Field */ fields;
  private List methods;
  private boolean isFinal;
  boolean isAbstract;
  boolean isSharp; // This class is a #A (not directly visible to the user)

  private ClassDefinition associatedConcreteClass; // non-null if !isSharp
}
