/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 2000                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import mlsub.typing.*;

import gnu.bytecode.*;
import java.util.*;

import bossa.util.Debug;

/**
   Abstract syntax for a class definition.
   
   @version $Date$
   @author Daniel Bonniot
 */
public class NiceClass extends ClassDefinition
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
   */
  public NiceClass(LocatedString name, 
		   boolean isFinal, boolean isAbstract, 
		   boolean isInterface, boolean isSharp,
		   List typeParameters,
		   List typeParametersVariances,
		   List extensions, List implementations, List abstractions)
  {
    super(name.cloneLS(), isSharp || isFinal, isFinal, isAbstract, isInterface,
	  typeParameters, typeParametersVariances,
	  extensions, implementations, abstractions);

    this.simpleName = name;
    this.isSharp = isSharp;
    
    // if this class is final, 
    // no #class is created below.
    // the associated #class is itself
    if(this.isFinal && !isSharp)
      addTypeMap("#"+name.content, this.tc);
  }

  public void setFieldsAndMethods(List fields, List methods)
  {
    this.fields = keepFields(fields, isSharp);
    if(methods != null)
      this.methods = addChildren(methods);

    computeMethods();

    if(this.isSharp)
      classType = module.createClass(simpleName.toString().substring(1));
    else
      if(this.isAbstract || this.isFinal)
	// since no associated concrete class is created,
	// it needs its own classtype
	classType = module.createClass(simpleName.toString());

    if(classType != null)
      nice.tools.code.Types.set(tc, classType);
  }
  
  /**
   * Creates an immediate descendant, 
   * that abstracts and doesn't implement Top<n>.
   */
  public Collection associatedDefinitions()
  {
    if(isFinal || isAbstract)
      return null;

    LocatedString name = this.simpleName.cloneLS();
    name.prepend("#");

    NiceClass c = new NiceClass
      (name,true,false,false,true,typeParameters, typeParametersVariances,
       null, null, null);
    c.superClass = new TypeConstructor[]{ this.tc };
    
    c.setFieldsAndMethods(fields, null);
    associatedConcreteClass = c;
    concreteClasses.add(c);
    
    nice.tools.code.Types.set(this.tc, c.classType);
    
    Collection res = new ArrayList(1);
    res.add(c);
    return res;
  }

  protected boolean implementsTop()
  {
    return !isSharp;
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
    List result = new ArrayList(fields.size());
    for(Iterator i = fields.iterator(); i.hasNext();)
      {
	Field f = (Field) i.next();
	if(f.isLocal == local)
	  {
	    result.add(f);
	    f.sym.propagate = Node.none; // do not enter into global scope
	  }
      }
    return result;
  }
  
  public static class Field
  {
    public Field(MonoSymbol sym, boolean isFinal, boolean isLocal)
    {
      this.sym = sym;
      this.isFinal = isFinal;
      this.isLocal = isLocal;
    }

    public String toString()
    {
      return 
	(isFinal ? "final " : "") +
	(isLocal ? "local " : "") +
	sym;
    }
    
    MonoSymbol sym;
    boolean isFinal;
    boolean isLocal;
  }
  
  private void computeMethods()
  {
    for(Iterator i = fields.iterator(); i.hasNext();)
      {
	Field f = (Field) i.next();

	MonoSymbol s = f.sym;
	MethodDeclaration m = 
	  new FieldAccessMethod(this, s.name, s.syntacticType, typeParameters);

	addChild(m);
      }
  }

  void resolve()
  {
    super.resolve();

    // must be called when bytecode types are set, 
    // but before compilation, so that constructors are known
    // even in package variable initializers.
    prepareClassType();

    //optim
    if(fields.size()==0)
      return;
    
    TypeScope localScope = new TypeScope(typeScope);
    try{
      localScope.addSymbols(typeParameters);
    }
    catch(TypeScope.DuplicateName e){
      User.error(this, e);
    }
    
    for(Iterator i = fields.iterator();	i.hasNext();)
      {
	Field f = (Field) i.next();
	
	f.sym.type = f.sym.syntacticType.resolve(localScope);
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
       + "}\n\n"
       );
    printInterface(methods, s);
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
      return ClassDefinition.get(superClass[0]);
    else
      return this;
  }
  
  Type javaClass()
  {
    if(associatedConcreteClass != null)
      return javaClass(associatedConcreteClass);
    else
      {
	if(classType == null)
	  Internal.error(name,
			 name+" has no classType field");

	return classType;
      }
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  private void prepareClassType()
  {
    if (associatedConcreteClass != null)
      associatedConcreteClass.prepareClassType();
    else if(classType != null)
      {
	if(!isInterface)
	  createConstructor();

	// we need this before compile(), 
	// to know this type is, say, an interface
	// when it appears in other definitions
	ClassDefinition me = abstractClass();

	classType.setModifiers(Access.PUBLIC 
			       | (me.isAbstract  ? Access.ABSTRACT  : 0)
			       | (me.isFinal     ? Access.FINAL     : 0)
			       | (me.isInterface ? Access.INTERFACE : 0)
			       );
      }
  }
  
  public void compile()
  {
    if(classType == null)
      return;
    
    if(Debug.codeGeneration)
      Debug.println("Compiling class "+name);
    
    ClassDefinition me = abstractClass();

    ClassType[] itfs = null;
    if(isInterface)
      classType.setSuper(gnu.bytecode.Type.pointer_type);
    else
      classType.setSuper(javaSuperClass());
    
  createItfs:
    if(!isInterface)
      {
	if(me.impl == null) break createItfs;
	
	ArrayList imp = new ArrayList(me.impl.length);
	for(int i=0; i<me.impl.length; i++)
	  {
	    TypeConstructor assocTC = me.impl[i].associatedTC();
	    
	    if(assocTC == null)
	      continue;
	    
	    imp.add(nice.tools.code.Types.javaType(assocTC));
	  }
	itfs = (ClassType[]) imp.toArray(new ClassType[imp.size()]);
      }
    else 
      {
	if(superClass == null) break createItfs;
	
	itfs = new ClassType[superClass.length];
	for(int i=0; i<superClass.length; i++)
	  {
	    Interface itf = get(superClass[i]).getAssociatedInterface();
	    if(itf == null)
	      {
		Internal.warning("Should not happen. size of itfs gona be wrong");
		continue;
	      }
	    itfs[i] = (ClassType) 
	      nice.tools.code.Types.javaType(itf.associatedTC());
	  }
      }
    
    classType.setInterfaces(itfs);
    
    addFields(classType);
    if (me != this) me.addFields(classType);

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

    mlsub.typing.MonotypeVar[] params = createSameTypeParameters();
    MethodDeclaration md = new NiceMethod
      (new LocatedString("<init>", location()),
       null, null, null);
    md.setLowlevelTypes
      (mlsub.typing.Constraint.create(params, null),
       null, 
       new mlsub.typing.MonotypeConstructor(this.tc, params));

    md.setDispatchMethod(new gnu.expr.PrimProcedure
      (classType, gnu.bytecode.Type.typeArray0));
    TypeConstructors.addConstructor(abstractClass().tc, md);
  }

  private static gnu.bytecode.Method constructor(ClassType ct)
  {
    if(ct == null)
      ct = gnu.bytecode.Type.pointer_type;
    
    return ct.addMethod
      ("<init>", Access.PUBLIC, gnu.bytecode.Type.typeArray0, 
       gnu.bytecode.Type.void_type);
  }
  
  protected void addFields(ClassType c)
  {
    for(Iterator i = fields.iterator(); i.hasNext();)
      {
	Field f = (Field) i.next();
	
	String name = f.sym.name.toString();
	
	// The field might have been created by a FieldAccessMethod.compile*()
	if(c.getDeclaredField(name) == null)
	  c.addField(name,
		     nice.tools.code.Types.javaType(f.sym.type),
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
  private ClassType classType;  

  /** Not the fully qualified name */
  private LocatedString simpleName;
  private boolean isSharp;
  private NiceClass associatedConcreteClass; // non-null if !isSharp
}
