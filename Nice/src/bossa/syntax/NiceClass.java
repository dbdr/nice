/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
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
import mlsub.typing.Constraint;
import mlsub.typing.MonotypeConstructor;

import gnu.bytecode.Access;
import java.util.*;

import bossa.util.Debug;
import nice.tools.code.Types;
import nice.tools.code.Gen;

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

    // must be called when bytecode types are set, 
    // but before compilation, so that constructors are known
    // even in package variable initializers.
    prepareCodeGeneration();
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
    public Field(MonoSymbol sym, Expression value, boolean isFinal)
    {
      this.sym = sym;
      this.value = value;
      this.isFinal = isFinal;
    }

    public String toString()
    {
      return 
	(isFinal ? "final " : "") +
	sym + (value == null ? "" : " = " + value);
    }
    
    FormalParameters.Parameter asParameter(TypeMap map)
    {
      Monotype type = Monotype.create(sym.syntacticType.resolve(map));
      if (value == null)
	return new FormalParameters.NamedParameter(type, sym.getName(), true);
      else
	return new FormalParameters.OptionalParameter
	  (type, sym.getName(), true, value);
    }

    MonoSymbol sym;
    Expression value;
    boolean isFinal;
  }
  
  private void computeMethods()
  {
    for (int i = 0; i < fields.length; i++)
      {
	MonoSymbol s = fields[i].sym;
	addChild(new NiceFieldAccess(this, s.name, s.syntacticType));
      }
  }

  void resolve()
  {
    super.resolve();

    classe.supers = computeSupers();

    createConstructor();

    //optim
    if (fields.length == 0)
      return;
    
    TypeScope localScope = typeScope;
    if (typeParameters != null)
      try{
	localScope = new TypeScope(localScope);
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

    setJavaType(classe.getType());
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

  private void prepareCodeGeneration()
  {
    // The module will lookup the existing class if it is already compiled
    // and call createClassExp if not.
    classe = module.getClassExp(this);
  }

  gnu.expr.ClassExp classe;

  public gnu.expr.ClassExp createClassExp()
  {
    gnu.expr.ClassExp classe = new gnu.expr.ClassExp();
    classe.setName(name.toString());
    classe.setFile(location().getFile());
    classe.setSimple(true);
    classe.setAccessFlags((isInterface ? Access.INTERFACE : 0) |
			  (isAbstract ? Access.ABSTRACT : 0) |
			  (isFinal ? Access.FINAL : 0));
    return classe;
  }

  private FormalParameters.Parameter[] getFieldsAsParameters
    (int nbFields, MonotypeVar[] typeParams)
  {
    nbFields += this.fields.length;
    FormalParameters.Parameter[] res;
    ClassDefinition sup = distinguishedSuperClass();
    if (sup instanceof NiceClass)
      res = ((NiceClass) sup).getFieldsAsParameters(nbFields, typeParams);
    else
      res = new FormalParameters.Parameter[nbFields];

    if (fields.length == 0)
      return res;

    TypeScope map = Node.getGlobalTypeScope();
    if (typeParams != null)
      {
	// Constructs a type scope that maps the type parameters of this
	// class to the corresponding symbol in the constructor.
	map = new TypeScope(map);
	for (int i = 0; i < typeParams.length; i++)
	  try {
	    map.addMapping(this.typeParameters[i].getName(), typeParams[i]);
	  } catch(TypeScope.DuplicateName e) {}
      }

    for (int i = fields.length, n = res.length - nbFields + i; --i >= 0;)
      res[--n] = fields[i].asParameter(map);

    return res;
  }

  private MethodDeclaration constructorMethod;

  private void createConstructor()
  {
    if (isInterface)
      return;

    mlsub.typing.MonotypeVar[] typeParams = createSameTypeParameters();
    FormalParameters values = new FormalParameters(getFieldsAsParameters(0, typeParams));
    classe.setFieldCount(values.size);

    constructorMethod = new MethodDeclaration
      (new LocatedString("<init>", location()),
       values, 
       new Constraint(typeParams, null),
       Monotype.resolve(typeScope, values.types()),
       Monotype.sure(new MonotypeConstructor(this.tc, typeParams)))
      {
	protected gnu.expr.Expression computeCode()
	{
	  return classe.instantiate();
	}

	public void printInterface(java.io.PrintWriter s)
	{ throw new Error("Should not be called"); }
      };
    values.buildScope(scope, typeScope);
    values.doResolve();

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

    module.addImplementationClass(classe);
  }

  private gnu.expr.Expression typeExpression(TypeConstructor tc)
  {
    ClassDefinition c = ClassDefinition.get(tc);
    if (c instanceof NiceClass)
      return ((NiceClass) c).classe;
    else
      return new gnu.expr.QuoteExp(nice.tools.code.Types.javaType(tc));
  }

  private gnu.expr.Expression[] computeSupers()
  {
    if (!isInterface)
      {
	if (impl == null)
	  if (superClass == null)
	    return null;
	  else
	    return new gnu.expr.Expression[]{ typeExpression(superClass[0]) };
	  
	ArrayList imp = new ArrayList(impl.length + 1);
	if (superClass != null)
	  imp.add(typeExpression(superClass[0]));
	for (int i = 0; i < impl.length; i++)
	  {
	    TypeConstructor assocTC = impl[i].associatedTC();
	    
	    if (assocTC == null)
	      // This interface is abstract: ignore it.
	      continue;
	    
	    imp.add(typeExpression(assocTC));
	  }
	return (gnu.expr.Expression[]) 
	  imp.toArray(new gnu.expr.Expression[imp.size()]);
      }
    else 
      {
	if (superClass == null)
	  return null;
	
	gnu.expr.Expression[] itfs = new gnu.expr.Expression[superClass.length];
	for (int i = 0; i < superClass.length; i++)
	  itfs[i] = typeExpression(superClass[i]);

	return itfs;
      }
  }

  /** This native method is redefined for this Nice class. */
  void addJavaMethod(gnu.expr.LambdaExp method)
  {
    classe.addMethod(method);
  }

  /****************************************************************
   * Misc.
   ****************************************************************/

  private Field[] fields;
  private List methods;
}
