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
public class NiceClass extends ClassDefinition.ClassImplementation
{
  public NiceClass(ClassDefinition definition)
  {
    this.definition = definition;

    // must be called when bytecode types are set, 
    // but before compilation, so that constructors are known
    // even in package variable initializers.
    prepareCodeGeneration();
  }

  public String getName() { return definition.getName().toString(); }

  public void setFieldsAndMethods(List fields, List methods) 
  {
    if (fields == null || fields.size() == 0)
      this.fields = noFields;
    else
      {
	this.fields = (Field[]) fields.toArray(new Field[fields.size()]);

	//do not enter fields into global scope
	for (int i = 0; i < this.fields.length; i++)
	  this.fields[i].sym.propagate = Node.none; 

	for (int i = 0; i < this.fields.length; i++)
	  {
	    NiceFieldAccess f = new NiceFieldAccess(this, this.fields[i]);
	    this.fields[i].method = f;
	    definition.addChild(f);
	  }
      }
    
    if(methods != null)
      this.methods = definition.addChildren(methods);
  }

  ClassDefinition definition;

  private static Field[] noFields = new Field[0];
  
  /****************************************************************
   * Fields
   ****************************************************************/
  
  public Field makeField
    (MonoSymbol sym, Expression value, 
     boolean isFinal, boolean isTransient, boolean isVolatile)
  {
    return new Field(sym, value, isFinal, isTransient, isVolatile);
  }

  public class Field
  {
    public Field(MonoSymbol sym, Expression value, 
		 boolean isFinal, boolean isTransient, boolean isVolatile)
    {
      this.sym = sym;
      this.value = value;
      this.isFinal = isFinal;
      this.isTransient = isTransient;
      this.isVolatile = isVolatile;

      if (isFinal && isVolatile)
	throw User.error(sym, "A field cannot be final and volatile");
    }

    void resolve(VarScope scope, TypeScope typeScope)
    {
      sym.type = sym.syntacticType.resolve(typeScope);
      
      if (Types.isVoid(sym.type))
	User.error(sym, "A field cannot have void type");

      value = dispatch.analyse(value, scope, typeScope);
    }

    void createField()
    {
      method.fieldDecl = classe.addField
	(sym.name.toString(), Types.javaType(sym.type));
      method.fieldDecl.setFlag(isTransient, gnu.expr.Declaration.TRANSIENT);
      method.fieldDecl.setFlag(isVolatile , gnu.expr.Declaration.VOLATILE);
    }

    void typecheck(NiceClass c)
    {
      if (value != null)
	{
	  c.enterTypingContext();
	  mlsub.typing.Polytype declaredType = sym.getType();
	  value = value.resolveOverloading(declaredType);
	  try {
	    Typing.leq(value.getType(), declaredType);
	  } 
	  catch (mlsub.typing.TypingEx ex) {
	    throw bossa.syntax.dispatch.assignmentError
	      (value, sym.getName().toString(), sym.getType().toString(), value);
	  }
	}
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
    boolean isTransient;
    boolean isVolatile;

    NiceFieldAccess method;
  }
  
  void resolveClass()
  {
    classe.supers = computeSupers();
    resolveFields();
    createConstructor();
    createFields();
    definition.setJavaType(classe.getType());
  }

  private void resolveFields()
  {
    if (fields.length == 0)
      return;

    TypeScope localScope = definition.getLocalScope();
    
    for (int i = 0; i < fields.length; i++)
      fields[i].resolve(definition.scope, localScope);
  }

  private void createFields()
  {
    if (fields.length == 0)
      return;

    for (int i = 0; i < fields.length; i++)
      fields[i].createField();
  }

  void resolveBody()
  {
    if (methods != null)
      for (Iterator i = methods.iterator(); i.hasNext();)
	{
	  Object child = i.next();
	  if (child instanceof ToplevelFunction)
	    ((ToplevelFunction) child).resolveBody();
	}
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  void typecheck() 
  { 
    //super.typecheck();
    if (fields == null)
      return;

    try {
      for (int i = 0; i < fields.length; i++)
	fields[i].typecheck(this);
    }
    finally {
      if (entered) {
	entered = false;
	try {
	  Typing.leave();
	}
	catch(TypingEx ex) {
	  User.error(this.definition, "Type error in field declarations");
	}
      }
    }
  }

  private boolean entered = false;

  private void enterTypingContext()
  {
    if (entered || definition.typeParameters == null) 
      return;
    Typing.enter();
    entered = true;
    Typing.introduce(definition.typeParameters);
    try {
      Typing.implies();
    }
    catch(TypingEx ex) {
      Internal.error(ex);
    }
  }


  /****************************************************************
   * Module Interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print
      (
         " {\n"
       + Util.map("",";\n",";\n",fields)
       + "}\n\n"
       );
    Definition.printInterface(methods, s);
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  private void prepareCodeGeneration()
  {
    // The module will lookup the existing class if it is already compiled
    // and call createClassExp if not.
    classe = definition.module.getClassExp(this);
  }

  gnu.expr.ClassExp classe;

  public gnu.expr.ClassExp createClassExp()
  {
    gnu.expr.ClassExp classe = new gnu.expr.ClassExp();
    classe.setName(definition.name.toString());
    classe.setFile(definition.location().getFile());
    classe.setSimple(true);
    classe.setAccessFlags(definition.getBytecodeFlags());
    return classe;
  }

  private FormalParameters.Parameter[] getFieldsAsParameters
    (int nbFields, MonotypeVar[] typeParams)
  {
    nbFields += this.fields.length;
    FormalParameters.Parameter[] res;
    ClassDefinition sup = ClassDefinition.get(definition.getSuperClass());
    if (sup != null && sup.implementation instanceof NiceClass)
      res = ((NiceClass) sup.implementation).getFieldsAsParameters(nbFields, typeParams);
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
	    map.addMapping(definition.typeParameters[i].getName(), typeParams[i]);
	  } catch(TypeScope.DuplicateName e) {}
      }

    for (int i = fields.length, n = res.length - nbFields + i; --i >= 0;)
      res[--n] = fields[i].asParameter(map);

    return res;
  }

  private MethodDeclaration constructorMethod;

  private void createConstructor()
  {
    if (definition instanceof ClassDefinition.Interface)
      return;

    mlsub.typing.MonotypeVar[] typeParams = definition.createSameTypeParameters();
    FormalParameters values = new FormalParameters(getFieldsAsParameters(0, typeParams));
    classe.setFieldCount(values.size);

    constructorMethod = new MethodDeclaration
      (new LocatedString("<init>", definition.location()),
       values, 
       new Constraint(typeParams, null),
       Monotype.resolve(definition.typeScope, values.types()),
       Monotype.sure(new MonotypeConstructor(definition.tc, typeParams)))
      {
	protected gnu.expr.Expression computeCode()
	{
	  return classe.instantiate();
	}

	public void printInterface(java.io.PrintWriter s)
	{ throw new Error("Should not be called"); }
      };

    TypeConstructors.addConstructor(definition.tc, constructorMethod);
  }

  public void compile()
  {
    if (methods != null)
      for (Iterator i = methods.iterator(); i.hasNext();)
	{
	  Object child = i.next();
	  if (child instanceof ToplevelFunction)
	    ((ToplevelFunction) child).compile();
	}

    definition.module.addImplementationClass(classe);
  }

  private gnu.expr.Expression typeExpression(TypeConstructor tc)
  {
    ClassDefinition c = ClassDefinition.get(tc);
    if (c != null && c.implementation instanceof NiceClass)
      return ((NiceClass) c.implementation).classe;
    else
      return new gnu.expr.QuoteExp(nice.tools.code.Types.javaType(tc));
  }

  private gnu.expr.Expression[] computeSupers()
  {
    TypeConstructor superClass = definition.getSuperClass();
    Interface[] interfaces = definition.getInterfaces();

    int len = superClass == null ? 0 : 1;
    if (interfaces != null)
      len += interfaces.length;
    if (len == 0)
      return null;

    gnu.expr.Expression[] res = new gnu.expr.Expression[len];

    if (interfaces != null)
      for (int i = 0; i < interfaces.length; i++)
	{
	  TypeConstructor assocTC = interfaces[i].associatedTC();
	    
	  if (assocTC == null)
	    // This interface is abstract: ignore it.
	    continue;
	    
	  res[--len] = typeExpression(assocTC);
	}
    if (superClass != null)
      res[--len] = typeExpression(superClass);

    if (len != 0) // The array was too big.
      {
	gnu.expr.Expression[] tmp = new gnu.expr.Expression[res.length - len];
	System.arraycopy(res, len, tmp, 0, tmp.length);
	res = tmp;
      }

    return res;
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
