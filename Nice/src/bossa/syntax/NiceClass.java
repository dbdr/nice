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
import mlsub.typing.AtomicConstraint;

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
    if (definition instanceof ClassDefinition.Interface)
      User.error(sym, "An interface cannot have a field.");
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

	  dispatch.typecheck(value);

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
  
  // Used to resolve fields, and constructor cosntraint.
  private TypeScope localScope;

  void resolveClass()
  {
    classe.supers = computeSupers();
    localScope = definition.getLocalScope();
    resolveFields();
    createConstructor();
    definition.setJavaType(classe.getType());
  }

  private void resolveFields()
  {
    if (fields.length == 0)
      return;
    
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
    if (entered || definition.classConstraint == null) 
      return;
    Typing.enter();
    entered = true;
    Typing.introduce(definition.classConstraint.typeParameters);
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
    /* 
       We always generate a new ClassExp object, even for classes from
       already compiled packages. The reason is that we might need to
       add methods for the multiple dispatch of java methods in the class 
       (and maybe for adding fields, or optimizing the dispatch of Nice
       methods in the future).
    */
    classe = createClassExp();
  }

  private gnu.expr.ClassExp createClassExp()
  {
    gnu.expr.ClassExp classe = new gnu.expr.ClassExp();
    classe.setName(definition.name.toString());
    classe.setFile(definition.location().getFile());
    classe.setSimple(true);
    classe.setAccessFlags(definition.getBytecodeFlags());
    definition.module.addImplementationClass(classe);
    return classe;
  }

  gnu.expr.ClassExp classe;

  public gnu.expr.ClassExp getClassExp()
  {
    return classe;
  }

  /**
     Collect in 'constraints' the constraints set by each class
     on the type parameters.
  */
  private static FormalParameters.Parameter[][] getFieldsAsParameters
    (TypeConstructor tc, int nbFields, List constraints, 
     MonotypeVar[] typeParams)
  {
    ClassDefinition sup = ClassDefinition.get(tc);
    if (sup != null && sup.implementation instanceof NiceClass)
      return ((NiceClass) sup.implementation).
	getFieldsAsParameters(nbFields, constraints, typeParams);

    List constructors = TypeConstructors.getConstructors(tc);
    if (constructors == null)
      return new FormalParameters.Parameter[][]
        { new FormalParameters.Parameter[nbFields] };

    FormalParameters.Parameter[][] res = 
      new FormalParameters.Parameter[constructors.size()][];
    int n = 0;
    for (Iterator i = constructors.iterator(); i.hasNext(); n++)
      {
	MethodDeclaration.Symbol m = (MethodDeclaration.Symbol) i.next();
	res[n] = new FormalParameters.Parameter[nbFields + m.arity];
	mlsub.typing.Monotype[] args = m.getDefinition().getArgTypes();
	for (int j = 0; j < args.length; j++)
	  res[n][j] = new FormalParameters.Parameter(Monotype.create(args[j]));
      }
    return res;
  }

  private FormalParameters.Parameter[][] getFieldsAsParameters
    (int nbFields, List constraints, MonotypeVar[] typeParams)
  {
    nbFields += this.fields.length;
    FormalParameters.Parameter[][] res = getFieldsAsParameters
      (definition.getSuperClass(), nbFields, constraints, typeParams);

    if (fields.length == 0 && definition.classConstraint == null)
      return res;

    TypeScope scope = Node.getGlobalTypeScope();
    Map map = null;
    if (typeParams != null)
      {
	// Constructs a type scope that maps the type parameters of this
	// class to the corresponding symbol in the constructor.
	scope = new TypeScope(scope);
	map = new HashMap();
	for (int i = 0; i < typeParams.length; i++)
	  try {
	    scope.addMapping(definition.classConstraint.typeParameters[i].getName(), typeParams[i]);
	    map.put(definition.classConstraint.typeParameters[i], typeParams[i]);
	  } catch(TypeScope.DuplicateName e) {}
      }

    for (int j = 0; j < res.length; j++)
      for (int i = fields.length, n = res[j].length - nbFields + i; --i >= 0;)
	res[j][--n] = fields[i].asParameter(scope);

    if (definition.classConstraint != null)
      {
	AtomicConstraint[] newAtoms = 
	  AtomicConstraint.substitute(map, definition.resolvedConstraints);

	if (newAtoms != null)
	  for (int i = 0; i < newAtoms.length; i++)
	    constraints.add(newAtoms[i]);
      }

    return res;
  }

  private Constructor[] constructorMethod;

  private void createConstructor()
  {
    if (definition instanceof ClassDefinition.Interface)
      return;

    List constraints;
    mlsub.typing.MonotypeVar[] typeParams = definition.getTypeParameters();
    if (typeParams == null)
      constraints = null;
    else 
      constraints = new LinkedList();

    FormalParameters.Parameter[][] params = 
      getFieldsAsParameters(0, constraints, typeParams);

    Constraint cst;
    if (typeParams != null)
      cst = new Constraint
	(typeParams, (AtomicConstraint[])
	 constraints.toArray(new AtomicConstraint[constraints.size()]));
    else
      cst = Constraint.True;

    constructorMethod = new Constructor[params.length];
    for (int i = 0; i < params.length; i++)
      {
	FormalParameters values = new FormalParameters(params[i]);

	constructorMethod[i] = new Constructor
	  (this, fields, i, definition.location(),
	   values, 
	   cst,
	   Monotype.resolve(definition.typeScope, values.types()),
	   Monotype.sure(new MonotypeConstructor(definition.tc, typeParams)));

	TypeConstructors.addConstructor(definition.tc, constructorMethod[i]);
      }
  }

  private static gnu.expr.Expression objectConstructor =
    new gnu.expr.QuoteExp
    (new gnu.expr.InitializeProc
     (gnu.bytecode.Type.pointer_type.getDeclaredMethod("<init>", 0)));

  gnu.expr.Expression getSuper(int index)
  {
    TypeConstructor tc = definition.getSuperClass();
    
    if (tc == null)
      return objectConstructor;

    ClassDefinition sup = ClassDefinition.get(tc);
    if (sup != null && sup.implementation instanceof NiceClass)
      return ((NiceClass) sup.implementation).
	constructorMethod[index].getConstructorInvocation();

    List constructors = TypeConstructors.getConstructors(tc);
    JavaConstructor m = (JavaConstructor)
      ((MethodDeclaration.Symbol) constructors.get(index)).getDefinition();
    return m.getConstructorInvocation();
  }

  public void precompile()
  {
    // We have to do this after resolution, so that bytecode types are known, 
    // but before compilation.
    createFields();
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

    if (constructorMethod != null)
      for (int i = 0; i < constructorMethod.length; i++)
	constructorMethod[i].getCode();
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
    if (definition.javaInterfaces != null)
      len += definition.javaInterfaces.length;
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

    if (definition.javaInterfaces != null)
      for (int i = 0; i < definition.javaInterfaces.length; i++)
	res[--len] = typeExpression(definition.javaInterfaces[i]);

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
  public gnu.expr.Expression addJavaMethod(gnu.expr.LambdaExp method)
  {
    return classe.addMethod(method);
  }

  public gnu.expr.LambdaExp createJavaMethod(String name, 
					     gnu.bytecode.Method likeMethod,
					     gnu.expr.Expression[] params)
  {
    gnu.expr.LambdaExp lambda = 
      Gen.createMemberMethod
        (name, 
	 getClassExp().getType(), 
	 likeMethod.getParameterTypes(),
	 likeMethod.getReturnType(),
	 params);
    return lambda;
  }

  /**
     Returns an expression to call a super method from outside a class method.

     This is needed because the JVM restricts call to a specific implementation
     to occur inside a method of the same class. So this generates a stub class
     method that calls the desired super method, and return a reference to this
     stub.
  */
  gnu.expr.Expression callSuperMethod(gnu.bytecode.Method superMethod)
  {
    gnu.expr.Expression[] params = new gnu.expr.Expression[superMethod.getParameterTypes().length + 1];
    gnu.expr.LambdaExp lambda = createJavaMethod("$super$" + superMethod.getName(), superMethod, params);
    Gen.setMethodBody(lambda, 
		      new gnu.expr.ApplyExp(new gnu.expr.QuoteExp(gnu.expr.PrimProcedure.specialCall(superMethod)), params));
    return addJavaMethod(lambda);
  }

  /****************************************************************
   * Misc.
   ****************************************************************/

  public String toString() { return definition.toString(); }

  private Field[] fields;
  private List methods;
}
