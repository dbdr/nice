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
import nice.tools.code.Types;

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
   * @param typeParameters a list of type symbols
   * @param extensions a list of TypeConstructors
   * @param implementations a list of Interfaces
   * @param abstractions a list of Interfaces
   */
  public NiceClass(LocatedString name, 
		   boolean isFinal, boolean isAbstract, 
		   boolean isInterface,
		   List typeParameters,
		   List typeParametersVariances,
		   List extensions, List implementations, List abstractions)
  {
    super(name.cloneLS(), isFinal, isAbstract, isInterface,
	  typeParameters, typeParametersVariances,
	  extensions, implementations, abstractions);

    this.simpleName = name;


    // must be called when bytecode types are set, 
    // but before compilation, so that constructors are known
    // even in package variable initializers.
    prepareClassType();
  }

  private static Field[] noFields = new Field[0];
  
  public void setFieldsAndMethods(List fields, List methods)
  {
    if (fields != null && fields.size() > 0)
      {
	this.fields = (Field[]) fields.toArray(new Field[fields.size()]);

	//do not enter fields into global scope
	for (int i = 0; i < this.fields.length; i++)
	  this.fields[i].sym.propagate = Node.none; 

	computeMethods();
      }
    else
      this.fields = noFields;
    
    if(methods != null)
      this.methods = addChildren(methods);
  }
  
  /****************************************************************
   * Fields
   ****************************************************************/

  public static class Field
  {
    public Field(MonoSymbol sym, boolean isFinal)
    {
      this.sym = sym;
      this.isFinal = isFinal;
    }

    public String toString()
    {
      return 
	(isFinal ? "final " : "") +
	sym;
    }
    
    MonoSymbol sym;
    boolean isFinal;
  }
  
  private void computeMethods()
  {
    for (int i = 0; i < fields.length; i++)
      {
	MonoSymbol s = fields[i].sym;
	addChild(new NiceFieldAccess
	  (this, s.name, s.syntacticType, typeParameters));
      }
  }

  void resolve()
  {
    super.resolve();

    if (!isInterface)
      compileConstructor();

    //optim
    if (fields.length == 0)
      return;
    
    TypeScope localScope = new TypeScope(typeScope);
    try{
      localScope.addSymbols(typeParameters);
    }
    catch(TypeScope.DuplicateName e){
      User.error(this, e);
    }
    
    for (int i = 0; i < fields.length; i++)
      {
	Field f = fields[i];
	
	f.sym.type = f.sym.syntacticType.resolve(localScope);

	if (Types.isVoid(f.sym.type))
	  User.error(f.sym, "Fields cannot have void type");
      }
  }

  public void createContext()
  {
    super.createContext();
    
    if (children != null)
      for (Iterator i = children.iterator(); i.hasNext();)
	{
	  Object child = i.next();
	  if (child instanceof MethodDeclaration)
	    ((MethodDeclaration) child).createContext();
	}
  }

  /****************************************************************
   * Module Interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
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
   * Code generation
   ****************************************************************/

  private void prepareClassType()
  {
    setJavaType(module.createClass(simpleName.toString()));
    createConstructor();
  }

  private MethodDeclaration constructorMethod;

  private void createConstructor()
  {
    if (isInterface)
      return;

    mlsub.typing.MonotypeVar[] params = createSameTypeParameters();
    constructorMethod = new NiceMethod
      (new LocatedString("<init>", location()),
       null, null, null);
    constructorMethod.setLowlevelTypes
      (mlsub.typing.Constraint.create(params, null),
       null, 
       Monotype.sure(new mlsub.typing.MonotypeConstructor(this.tc, params)));

    TypeConstructors.addConstructor(tc, constructorMethod);
  }

  public void compile()
  {
    if (children != null)
      for (Iterator i = children.iterator(); i.hasNext();)
	{
	  Object child = i.next();
	  if (child instanceof ToplevelFunction)
	    ((ToplevelFunction) child).compile();
	}

    if(Debug.codeGeneration)
      Debug.println("Compiling class "+name);
    
    ClassType classType = javaClass();

    classType.setModifiers(Access.PUBLIC 
			   | (isAbstract  ? Access.ABSTRACT  : 0)
			   | (isFinal     ? Access.FINAL     : 0)
			   | (isInterface ? Access.INTERFACE : 0)
			   );

    if(isInterface)
      classType.setSuper(gnu.bytecode.Type.pointer_type);
    else
      classType.setSuper(javaSuperClass());
    
    classType.setInterfaces(computeImplementedInterfaces());
    addFields(classType);

    classType.sourcefile = location().getFile();
    module.addImplementationClass(classType);
  }

  private ClassType[] computeImplementedInterfaces()
  {
    if(!isInterface)
      {
	if (impl == null) 
	  return null;
	
	ArrayList imp = new ArrayList(impl.length);
	for(int i=0; i<impl.length; i++)
	  {
	    TypeConstructor assocTC = impl[i].associatedTC();
	    
	    if (assocTC == null)
	      continue;
	    
	    imp.add(nice.tools.code.Types.javaType(assocTC));
	  }
	return (ClassType[]) imp.toArray(new ClassType[imp.size()]);
      }
    else 
      {
	if (superClass == null)
	  return null;
	
	ClassType[] itfs = new ClassType[superClass.length];
	for (int i=0; i<superClass.length; i++)
	  {
	    Interface itf = get(superClass[i]).getAssociatedInterface();
	    if (itf == null)
	      {
		Internal.warning("Should not happen");
		continue;
	      }
	    itfs[i] = (ClassType) 
	      nice.tools.code.Types.javaType(itf.associatedTC());
	  }
	return itfs;
      }
  }

  private void compileConstructor()
  {
    ClassType classType = javaClass();
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

    constructorMethod.setCode( new gnu.expr.QuoteExp
      (new gnu.expr.PrimProcedure(constructor)));
  }

  private static gnu.bytecode.Method constructor(ClassType ct)
  {
    if(ct == null)
      ct = gnu.bytecode.Type.pointer_type;

    return ct.addMethod
      ("<init>", Access.PUBLIC, gnu.bytecode.Type.typeArray0, 
       gnu.bytecode.Type.void_type);
  }
  
  void addFields(ClassType c)
  {
    for (int i = 0; i < fields.length; i++)
      {
	Field f = fields[i];
	String name = f.sym.name.toString();
	
	// The field might have been created by a NiceFieldAccess.compile*()
	if(c.getDeclaredField(name) == null)
	  c.addField(name,
		     nice.tools.code.Types.javaType(f.sym.type),
		     Access.PUBLIC);
      }
  }
  
  /****************************************************************
   * Misc.
   ****************************************************************/

  private Field[] fields;
  private List methods;

  /** Not the fully qualified name */
  private LocatedString simpleName;
}
