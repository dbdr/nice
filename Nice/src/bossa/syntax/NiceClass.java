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

    setJavaType(module.createClass(simpleName.toString()));
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

  void resolve()
  {
    super.resolve();

    // must be called when bytecode types are set, 
    // but before compilation, so that constructors are known
    // even in package variable initializers.
    prepareClassType();

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

	// The test to void should be more high-level than string comparison
	if (f.sym.type.toString().equals("nice.lang.void"))
	  User.error(f.sym, "Fields cannot have void type");
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
    if(!isInterface)
      createConstructor();

    ClassType classtype = javaClass();
    classtype.setModifiers(Access.PUBLIC 
			   | (isAbstract  ? Access.ABSTRACT  : 0)
			   | (isFinal     ? Access.FINAL     : 0)
			   | (isInterface ? Access.INTERFACE : 0)
			   );
  }

  public void compile()
  {
    if(Debug.codeGeneration)
      Debug.println("Compiling class "+name);
    
    ClassType classType = javaClass();
    ClassType[] itfs = null;
    if(isInterface)
      classType.setSuper(gnu.bytecode.Type.pointer_type);
    else
      classType.setSuper(javaSuperClass());
    
  createItfs:
    if(!isInterface)
      {
	if(impl == null) break createItfs;
	
	ArrayList imp = new ArrayList(impl.length);
	for(int i=0; i<impl.length; i++)
	  {
	    TypeConstructor assocTC = impl[i].associatedTC();
	    
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

    classType.sourcefile = location().getFile();
    module.addClass(classType);
  }

  private void createConstructor()
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
    TypeConstructors.addConstructor(tc, md);
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
