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

// File    : BossaClass.java
// Created : Thu Jul 01 11:25:14 1999 by bonniot
//$Modified: Fri Feb 25 13:27:34 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;

import gnu.bytecode.*;
import java.util.*;

/**
 * Abstract syntax for a class definition.
 */
public class BossaClass extends ClassDefinition
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
  public BossaClass(LocatedString name, 
		    boolean isFinal, boolean isAbstract, 
		    boolean isInterface, boolean isSharp,
		    List typeParameters,
		    List extensions, List implementations, List abstractions,
		    List fields, List methods)
  {
    super(name.cloneLS(), isFinal, isAbstract, isInterface,
	  typeParameters, extensions, implementations, abstractions);

    this.simpleName = name;    
    this.isSharp=isSharp;
    this.implementsTop = !isSharp;
    
    this.fields=keepFields(fields,isSharp);

    addTypeSymbol(this.tc);
    
    if(this.isSharp)
      // The sharp class must not declare children, 
      // since the associated class has already done so.
      // Anyway, those should be null.
      {
	this.methods=methods;
	addChildren(computeAccessMethods(this.fields));

	classType = module.createClass(simpleName.toString().substring(1));
      }
    else
      {
	this.methods=addChildren(methods);
	addChildren(computeAccessMethods(this.fields));

	if(this.isAbstract || this.isFinal)
	  // since no associated concrete class is created,
	  // it needs its own classtype
	  classType = module.createClass(simpleName.toString());
      }
    
    // if this class is final, 
    // no #class is created below.
    // the associated #class is itself
    if(this.isFinal && !this.isSharp)
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

    LocatedString name=this.simpleName.cloneLS();
    name.prepend("#");
    List parent=new ArrayList(1);
    parent.add(this.tc);
    ClassDefinition c=new BossaClass
      (name,true,false,false,true,typeParameters,parent,
       new LinkedList(),new LinkedList(),fields,methods);
    associatedConcreteClass = c;
    concreteClasses.add(c);
    
    Collection res=new ArrayList(1);
    res.add(c);
    return res;
  }

  static private List concreteClasses = new LinkedList();

  static public ListIterator listConcreteClasses()
  {
    return concreteClasses.listIterator();
  }
  
  public boolean isConcrete()
  {
    return isSharp;
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
	  {
	    result.add(f);
	    addChild(f.sym);
	    f.sym.propagate=Node.none; // do not enter into global scope
	  }
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
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
    super.createContext();

    // This is done here for convenience only
    if(classType!=null)
      {
	if(!isInterface)
	  createConstructor();

	// we need this before compile, 
	// to know this type is, say, an interface
	// when it appears in other definitions
	ClassDefinition me = abstractClass();

	classType.setModifiers(Access.PUBLIC 
			       | (me.isAbstract ? Access.ABSTRACT : 0)
			       | (me.isFinal    ? Access.FINAL    : 0)
			       | (me.isInterface? Access.INTERFACE: 0)
			       );
      }
  }

  /****************************************************************
   * Module Interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    if(isSharp)
      return;
    
    super.printInterface(s);
    s.print
      (
         " {\n"
       + Util.map("",";\n",";\n",fields)
       + Util.map(methods)
       + "}\n\n"
       );
  }
  
  /****************************************************************
   * Class hierarchy
   ****************************************************************/
  
  /**
   * The abstract class associated with this class.
   * If no #class has been generated, returns this.
   */
  ClassDefinition abstractClass()
  {
    if(isSharp)
      return ((TypeConstructor) extensions.get(0)).getDefinition();
    else
      return this;
  }
  
  Type javaClass()
  {
    if(associatedConcreteClass!=null)
      return javaClass(associatedConcreteClass);
    else
      {
	if(classType==null)
	  Internal.error(name,
			 name+" has no classType field");

	return classType;
      }
  }
  
  /**
   * Computes the minimal set S of classes below this concrete class C
   * such that for all concrete D,
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
    BossaClass me = (BossaClass) abstractClass();
    
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
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public void compile()
  {
    if(!(isSharp || isAbstract || isFinal))
      return;
    
    if(Debug.codeGeneration)
      Debug.println("Compiling class "+name);
    
    ClassDefinition me = abstractClass();

    ClassType[] itfs;
    if(!isInterface)
      {
	classType.setSuper(javaSuperClass());
      }
    else
      {
	classType.setSuper(gnu.bytecode.Type.pointer_type);
      }
    
    if(!isInterface)
      {
	List imp = new ArrayList(me.implementations.size());
	for(Iterator i = me.implementations.iterator(); i.hasNext();)
	  {
	    InterfaceDefinition itf = ((Interface) i.next()).definition;
	    if(!(itf instanceof AssociatedInterface))
	      {
		//Debug.println(itf+" ---- "+this);
		continue;
	      }
	    
	    imp.add(((AssociatedInterface) itf).getAssociatedClass().getJavaType());
	  }
	itfs = new ClassType[imp.size()];
	int n=0;
	for(Iterator i = imp.iterator(); i.hasNext();)
	  itfs[n++] = (ClassType) i.next();
      }
    else
      {
	itfs = new ClassType[extensions.size()];
	int n=0;
	for(Iterator i = extensions.iterator(); i.hasNext();)
	  {
	    ClassDefinition c = ((TypeConstructor)i.next()).getDefinition();
	    AssociatedInterface itf = c.getAssociatedInterface();
	    if(itf==null)
	      {
		Internal.warning("Should not happen. size of itfs gona be wrong");
		continue;
	      }
	    itfs[n++] = (ClassType) itf.getAssociatedClass().getJavaType();
	  }
      }
    
    classType.setInterfaces(itfs);
    
    addFields(classType);
    if(me!=this) me.addFields(classType);

    for(Iterator i = illegitimateParents.iterator();
	i.hasNext();)
      ((ClassDefinition) i.next()).addFields(classType);

    module.addClass(classType);
  }

  private void createConstructor()
  {
    gnu.bytecode.Method constructor = constructor(classType);

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
  }

  private static gnu.bytecode.Method constructor(ClassType ct)
  {
    if(ct==null)
      ct = gnu.bytecode.Type.pointer_type;
    
    gnu.bytecode.Method res = ct.addMethod("<init>", Access.PUBLIC, gnu.bytecode.Type.typeArray0, gnu.bytecode.Type.void_type);

    return res;
  }
  
  protected void addFields(ClassType c)
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
   * Misc.
   ****************************************************************/

  Collection methodDefinitions()
  {
    return methods;
  }

  private List /* of ClassDefinition.Field */ fields;
  private List methods;
  boolean isSharp; // This class is a #A (not directly visible to the user)
  private ClassType classType;  

  /** Not the fully qualified name */
  private LocatedString simpleName;
  private ClassDefinition associatedConcreteClass; // non-null if !isSharp
}
