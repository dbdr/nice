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

import java.util.*;

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

  public void setFields(List fields) 
  {
    if (fields == null || fields.size() == 0)
      this.fields = noFields;
    else
      this.fields = (NewField[]) fields.toArray(new NewField[fields.size()]);
  }

  public void setOverrides(List overrides) 
  {
    if (overrides == null || overrides.size() == 0)
      this.overrides = noOverrides;
    else
      {
	this.overrides = (OverridenField[]) 
          overrides.toArray(new OverridenField[overrides.size()]);
      }
  }

  ClassDefinition definition;

  private static NewField[] noFields = new NewField[0];
  private static OverridenField[] noOverrides = new OverridenField[0];
  
  NiceClass getParent()
  {
    TypeConstructor tc = definition.getSuperClass();
    ClassDefinition sup = ClassDefinition.get(tc);
    if (sup != null && sup.implementation instanceof NiceClass)
      return ((NiceClass) sup.implementation);
    else
      return null;
  }

  /****************************************************************
   * Fields
   ****************************************************************/
  
  public Field makeField
    (MonoSymbol sym, Expression value, 
     boolean isFinal, boolean isTransient, boolean isVolatile)
  {
    if (definition instanceof ClassDefinition.Interface)
      User.error(sym, "An interface cannot have a field.");

    return new NewField(sym, value, isFinal, isTransient, isVolatile);
  }

  public Field makeOverride (MonoSymbol sym, Expression value)
  {
    if (definition instanceof ClassDefinition.Interface)
      User.error(sym, "An interface cannot have a field.");

    return new OverridenField(sym, value);
  }

  abstract class Field
  {
    private Field(MonoSymbol sym, Expression value)
    {
      this.sym = sym;
      this.value = value;

      // Do not enter fields into global scope.
      sym.propagate = Node.none;

      method = new NiceFieldAccess(NiceClass.this, this);
      NiceClass.this.definition.addChild(method);
    }

    NiceClass getDeclaringClass()
    {
      return NiceClass.this;
    }

    abstract boolean isFinal();

    void resolve(VarScope scope, TypeScope typeScope)
    {
      sym.type = sym.syntacticType.resolve(typeScope);
      
      if (Types.isVoid(sym.type))
	User.error(sym, "A field cannot have void type");

      value = dispatch.analyse(value, scope, typeScope);
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

    void typecheck()
    {
      if (value != null)
	{
	  NiceClass.this.enterTypingContext();

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
	sym + (value == null ? "" : " = " + value);
    }
    
    MonoSymbol sym;
    Expression value;

    NiceFieldAccess method;
  }

  final class NewField extends Field
  {
    private NewField(MonoSymbol sym, Expression value, 
                     boolean isFinal, boolean isTransient, boolean isVolatile)
    {
      super(sym, value);
      this.isFinal = isFinal;
      this.isTransient = isTransient;
      this.isVolatile = isVolatile;

      if (isFinal && isVolatile)
	throw User.error(sym, "A field cannot be final and volatile");
    }

    boolean isFinal() { return isFinal; }

    void createField()
    {
      method.fieldDecl = classe.addField
	(sym.name.toString(), Types.javaType(sym.type));
      method.fieldDecl.setFlag(isTransient, gnu.expr.Declaration.TRANSIENT);
      method.fieldDecl.setFlag(isVolatile , gnu.expr.Declaration.VOLATILE);
    }

    void checkNoDuplicate(FormalParameters.Parameter[] fields, 
                          int rankInThisClass)
    {
      /*
         We check that there is no duplicate in all the inherited fields,
         but also in the fields of this class stricly before this one.
      */
      int max = fields.length - NiceClass.this.fields.length + rankInThisClass;
      String name = sym.getName().toString();
      for (int i = 0; i < max; i++)
        {
          if (fields[i].match(name))
            User.error(sym, 
                       (max - i >= NiceClass.this.fields.length)
                       ? "A field with the same name exists in a super-class"
                       : "A field with the same name exists in this class");
        }
    }

    public String toString()
    {
      return 
	(isFinal ? "final " : "") +
        super.toString();
    }
    
    boolean isFinal;
    boolean isTransient;
    boolean isVolatile;
  }

  final class OverridenField extends Field
  {
    private OverridenField(MonoSymbol sym, Expression value)
    {
      super(sym, value);
    }

    boolean isFinal() { return true; }

    /**
       Update the type and default values for the constructor, according
       to this overriding.
    */
    void updateConstructorParameter
      (FormalParameters.Parameter[] inherited, int nb, TypeScope scope)
    {
      String name = sym.getName().toString();
      Monotype type = Monotype.create(sym.syntacticType.resolve(scope));

      for (int i = 0; i < nb; i++)
        if (inherited[i].match(name))
          {
            if (value != null)
              inherited[i] = new FormalParameters.OptionalParameter
                (type, sym.getName(), true, value, 
                 inherited[i].value() == null || inherited[i].isOverriden());
            else
              inherited[i].resetType(type);
          }
    }

    void typecheck()
    {
      gnu.expr.Declaration decl = null;

      NiceClass parent = getParent();
      if (parent != null)
        decl = parent.getOverridenField(this, value == null);

      if (decl == null)
        throw User.error(sym, 
                         "No field with this name exists in a super-class");

      method.fieldDecl = decl;

      super.typecheck();
    }

    /**
       @param checkValue
         Whether to check that the original field's value, if it exists,
         must be checked against the overriden type.
       @return the checkValue to be used for other versions of this field
         higher up in the hierarchy.
    */
    boolean checkOverride(Field original, boolean checkValue)
    {
      NiceClass.this.enterTypingContext();

      mlsub.typing.Monotype originalType = original.sym.syntacticType.resolve
        (original.getDeclaringClass().translationScope(NiceClass.this));

      try {
        Typing.leq(this.sym.type, originalType);
      }
      catch (TypingEx ex) {
        User.error(this.sym, 
                   "The new type must be a subtype of the original type declared in " + original.getDeclaringClass() + ".\n" +
                   "Original type: " + originalType);
      }

      if (checkValue && original.value != null)
        {
	  try {
	    Typing.leq(original.value.getType(), this.sym.getType());
	  } 
	  catch (mlsub.typing.TypingEx ex) {
            User.error(sym, "The default value declared in " + 
                       original.getDeclaringClass() + 
                       "\nis not compatible with the overriden type");
          }
          return false;
        }

      return checkValue;
    }

    public String toString()
    {
      return "override " + super.toString();
    }
  }

  // Used to resolve fields, and constructor constraint.
  private TypeScope localScope;

  void resolveClass()
  {
    classe.supers = computeSupers();
    localScope = definition.getLocalScope();
    definition.setJavaType(classe.getType());
    resolveFields();
    resolveIntitializers();
    createConstructor();
    addPublicCloneMethod();
  }

  private void resolveFields()
  {
    for (int i = 0; i < fields.length; i++)
      fields[i].resolve(definition.scope, localScope);

    for (int i = 0; i < overrides.length; i++)
      overrides[i].resolve(definition.scope, localScope);
  }

  private void createFields()
  {
    for (int i = 0; i < fields.length; i++)
      fields[i].createField();
  }

  private gnu.expr.Declaration getOverridenField
    (OverridenField field, boolean checkValue)
  {
    String name = field.sym.getName().toString();

    for (int i = 0; i < fields.length; i++)
      if (fields[i].sym.getName().toString().equals(name))
        {
          if (! fields[i].isFinal)
            User.error(field.sym, "The original field in class " + this + 
                       " is not final, so its type cannot be overriden");

          checkValue = field.checkOverride(fields[i], checkValue);

          return fields[i].method.fieldDecl;
        }
  
    for (int i = 0; i < overrides.length; i++)
      if (overrides[i].sym.getName().toString().equals(name))
        checkValue = field.checkOverride(overrides[i], checkValue);

    NiceClass parent = getParent();
    if (parent != null)
      return parent.getOverridenField(field, checkValue);
    else
      return null;
  }

  /****************************************************************
   * Initializers
   ****************************************************************/

  private Statement[] initializers = Statement.noStatements;

  public void setInitializers(List inits)
  {
    initializers = (Statement[]) inits.toArray(new Statement[inits.size()]);
  }

  public int nbInitializers() { return initializers.length; }

  private void resolveIntitializers()
  {
    if (initializers.length == 0)
      return;

    VarScope scope = definition.scope;
    mlsub.typing.Monotype thisType = 
      Monotype.sure
      (new mlsub.typing.MonotypeConstructor
       (definition.tc, definition.getTypeParameters()));
    thisSymbol = 
      new MonoSymbol(FormalParameters.thisName, thisType)
      {
        gnu.expr.Expression compile()
        {
          return NiceClass.this.thisExp;
        }
      };

    Node.thisExp = new SymbolExp(thisSymbol, definition.location());
    scope.addSymbol(thisSymbol);

    for (int i = 0; i < initializers.length; i++)
      initializers[i] = bossa.syntax.dispatch.analyse
        (initializers[i], definition.scope, localScope, false);

    Node.thisExp = null;
    scope.removeSymbol(thisSymbol);
  }

  private MonoSymbol thisSymbol;
  private gnu.expr.Expression thisExp;

  void setThisExp(gnu.expr.Expression thisExp)
  {
    this.thisExp = thisExp;
  }

  gnu.expr.Expression compileInitializer(int index)
  {
    return initializers[index].generateCode();
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  void typecheck()
  {
    try {
      for (int i = 0; i < fields.length; i++)
	fields[i].typecheck();

      for (int i = 0; i < overrides.length; i++)
	overrides[i].typecheck();

      if (initializers.length != 0)
        {
          enterTypingContext();
          Node.thisExp = new SymbolExp(thisSymbol, definition.location());
          for (int i = 0; i < initializers.length; i++)
            bossa.syntax.dispatch.typecheck(initializers[i]);
        }
    }
    finally {
      if (entered) {
	entered = false;

        Node.thisExp = null;
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
       + Util.map("", ";\n", ";\n", fields)
       + Util.map("", ";\n", ";\n", overrides)
       + "}\n\n"
       );
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

  static gnu.bytecode.Method cloneMethod = 
    gnu.bytecode.Type.pointer_type.getDeclaredMethod("clone", 0);

  private void addPublicCloneMethod()
  {
    if (! definition.implementsJavaInterface("java.lang.Cloneable"))
      return;

    gnu.expr.Expression[] params = new gnu.expr.Expression[1];
    gnu.expr.LambdaExp lambda = createJavaMethod("clone", cloneMethod, params);
    Gen.setMethodBody(lambda, 
		      new gnu.expr.ApplyExp(new gnu.expr.QuoteExp(gnu.expr.PrimProcedure.specialCall(cloneMethod)), params));
    addJavaMethod(lambda);    
  }

  private gnu.expr.ClassExp createClassExp()
  {
    gnu.expr.ClassExp classe = new gnu.expr.ClassExp();
    classe.setName(definition.name.toString());
    definition.location().write(classe);
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
	mlsub.typing.Monotype[] args = m.getMethodDeclaration().getArgTypes();
	for (int j = 0; j < args.length; j++)
	  res[n][j] = new FormalParameters.Parameter(Monotype.create(args[j]));
      }
    return res;
  }

  /**
      @return the scope that maps the type parameters of the other class
        to the corresponding symbol in the constructor of this class.
  */
  private TypeScope translationScope(NiceClass other)
  {
    mlsub.typing.MonotypeVar[] typeParams = other.definition.getTypeParameters();
    TypeScope scope = Node.getGlobalTypeScope();
    Map map = null;
    if (typeParams != null)
      {
	scope = new TypeScope(scope);
	for (int i = 0; i < typeParams.length; i++)
	  try {
	    scope.addMapping(definition.classConstraint.typeParameters[i].getName(), typeParams[i]);
	  } catch(TypeScope.DuplicateName e) {}
      }

    return scope;
  }

  private FormalParameters.Parameter[][] getFieldsAsParameters
    (int nbFields, List constraints, MonotypeVar[] typeParams)
  {
    nbFields += this.fields.length;
    FormalParameters.Parameter[][] res = getFieldsAsParameters
      (definition.getSuperClass(), nbFields, constraints, typeParams);

    if (fields.length == 0 && overrides.length == 0 && 
        definition.classConstraint == null)
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

    updateConstructorParameters(res[0], res[0].length - nbFields, scope);

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

  /**
     This must be done in a given class for every subclass, since they
     have different type parameters.
  */
  private void updateConstructorParameters
    (FormalParameters.Parameter[] inherited, int nb, TypeScope scope)
  {
    for (int f = 0; f < overrides.length; f++)
      overrides[f].updateConstructorParameter(inherited, nb, scope);
  }

  /**
     This must be done only once per class.
  */
  private void checkFields (FormalParameters.Parameter[] allFields)
  {
    int len = allFields.length;
    for (int f = 0; f < fields.length; f++)
      fields[f].checkNoDuplicate(allFields, f);
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

    checkFields(params[0]);

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

  gnu.expr.Expression getSuper(int index, boolean omitDefaults)
  {
    TypeConstructor tc = definition.getSuperClass();
    
    if (tc == null)
      return objectConstructor;

    ClassDefinition sup = ClassDefinition.get(tc);
    if (sup != null && sup.implementation instanceof NiceClass)
      return ((NiceClass) sup.implementation).
	constructorMethod[index].getConstructorInvocation(omitDefaults);

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
    recompile();
  }

  /**
     Called instead of compile is the package is up-to-date.
  */
  public void recompile()
  {
    // This needs to be done even if we don't recompile, 
    // since classes are always regenerated.
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

  private NewField[] fields;
  private OverridenField[] overrides;
}
