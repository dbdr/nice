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
import nice.tools.code.*;

import gnu.expr.Declaration;
import gnu.expr.QuoteExp;

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
      {
        for(Iterator it = fields.iterator(); it.hasNext();)
          {
	    Field field = (Field)it.next();
            if (field.isFinal() && field.sym.getName().toString().equals("serialVersionUID"))
	      {
		it.remove();
		if (field.value instanceof ConstantExp && ((ConstantExp)field.value).value instanceof Long)
                  serialVersionUIDValue = (Long)((ConstantExp)field.value).value;
		else
		  User.error(field.sym, "the value of an serialVersionUID should a constant of type long");
	      }
          }
        this.fields = (NewField[]) fields.toArray(new NewField[fields.size()]);
      }
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

  public void setValueOverrides(List valueOverrides)
  {
    if (valueOverrides == null)
      this.valueOverrides = new LinkedList();
    else
      this.valueOverrides = valueOverrides;
  }

  ClassDefinition definition;

  public boolean isInterface()
  {
    return definition instanceof ClassDefinition.Interface;
  }

  private static NewField[] noFields = new NewField[0];
  private static OverridenField[] noOverrides = new OverridenField[0];
  
  static NiceClass get(TypeConstructor tc)
  {
    ClassDefinition res = ClassDefinition.get(tc);
    if (res != null && res.implementation instanceof NiceClass)
      return ((NiceClass) res.implementation);
    else
      return null;
  }

  public static NiceClass get(mlsub.typing.Monotype type)
  {
    if (! nice.tools.typing.Types.isSure(type))
      return null;

    return get(nice.tools.typing.Types.constructor(type));
  }

  NiceClass getParent()
  {
    return get(definition.getSuperClass());
  }

  /** List of symbols for calling constructors of this class. */
  private ArrayList constructors = new ArrayList(10);

  void addConstructorCallSymbol(MethodDeclaration.Symbol sym)
  {
    constructors.add(sym);
  }

  List getConstructorCallSymbols()
  {
    return (List) constructors.clone();
  }

  /****************************************************************
   * Fields
   ****************************************************************/
  
  public Field makeField
    (MonoSymbol sym, Expression value, 
     boolean isFinal, boolean isTransient, boolean isVolatile, String docString)
  {
    if (isInterface())
      User.error(sym, "An interface cannot have a field.");

    return new NewField(sym, value, isFinal, isTransient, isVolatile, docString);
  }

  public Field makeOverride (MonoSymbol sym, Expression value)
  {
    if (isInterface())
      User.error(sym, "An interface cannot have a field.");

    return new OverridenField(sym, value);
  }

  public ValueOverride makeValueOverride(LocatedString fname, Expression value)
  {
    if (isInterface())
      User.error(fname, "An interface cannot have a field.");

    return new ValueOverride(fname, value);
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
      
      if (nice.tools.typing.Types.isVoid(sym.type))
	User.error(sym, "A field cannot have void type");

      value = dispatch.analyse(value, scope, typeScope);
    }

    FormalParameters.Parameter asParameter()
    {
      Monotype type = sym.syntacticType;
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
                     boolean isFinal, boolean isTransient, boolean isVolatile,
		     String docString)
    {
      super(sym, value);
      this.isFinal = isFinal;
      this.isTransient = isTransient;
      this.isVolatile = isVolatile;
      this.docString = docString;

      if (isFinal && isVolatile)
	throw User.error(sym, "A field cannot be final and volatile");
    }

    boolean isFinal() { return isFinal; }

    void createField()
    {
      method.fieldDecl = classe.addField
	(sym.name.toString(), Types.javaType(sym.type));
      method.fieldDecl.setFlag(isFinal,     gnu.expr.Declaration.IS_CONSTANT);
      method.fieldDecl.setFlag(isTransient, gnu.expr.Declaration.TRANSIENT);
      method.fieldDecl.setFlag(isVolatile , gnu.expr.Declaration.VOLATILE);

      if (! definition.inInterfaceFile())
        {
          String fname = sym.getName().toString();
          String suffix = Character.toUpperCase(fname.charAt(0)) + fname.substring(1);
          createGetter(suffix);
          if (!isFinal)
            createSetter(suffix);
        }
    }
 
    void createGetter(String nameSuffix)
    {
      gnu.expr.Expression[] params = new gnu.expr.Expression[1];
      gnu.expr.LambdaExp getter = Gen.createMemberMethod
        ("get"+nameSuffix, classe.getType(), null,
	 method.fieldDecl.getType(), params);
      Gen.setMethodBody(getter, Inline.inline(new GetFieldProc(method.fieldDecl), params[0]));
      classe.addMethod(getter);
    }

    void createSetter(String nameSuffix)
    {
      gnu.expr.Expression[] params = new gnu.expr.Expression[2];
      gnu.bytecode.Type[] argTypes = new gnu.bytecode.Type[1];
      argTypes[0] = method.fieldDecl.getType();
      gnu.expr.LambdaExp setter = Gen.createMemberMethod
        ("set"+nameSuffix, classe.getType(), argTypes,
	 method.fieldDecl.getType(), params);
      Gen.setMethodBody(setter, Inline.inline(new SetFieldProc(method.fieldDecl), params[0], params[1]));
      classe.addMethod(setter);
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

    public String docString;
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
    void updateConstructorParameter(List inherited)
    {
      String name = sym.getName().toString();
      Monotype type = sym.syntacticType;

      for (int i = 1; i < inherited.size(); i++) {
        FormalParameters.Parameter param = (FormalParameters.Parameter)
          inherited.get(i);
        if (param.match(name))
          {
            if (value != null)
              inherited.set(i, new FormalParameters.OptionalParameter
                            (type, sym.getName(), true, value, 
                             param.value() == null || param.isOverriden()));
            else
              param.resetType(type);
          }
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

  public final class ValueOverride 
  {
    ValueOverride(LocatedString name, Expression value)
    {
      this.name = name;
      this.value = value;
    }

    void updateConstructorParameter(List inherited)
    {
      for (int i = 1; i < inherited.size(); i++) {
        FormalParameters.Parameter param = (FormalParameters.Parameter)
          inherited.get(i);
        if (param.match(name.toString()))
          {
            inherited.set(i, new FormalParameters.OptionalParameter
                            (param.type, name, true, value, 
                             param.value() == null || param.isOverriden()));
          }
      }
    }

    void resolve(VarScope scope, TypeScope typeScope)
    {
      value = dispatch.analyse(value, scope, typeScope);
    }


    void typecheck()
    {
      boolean exists = false;

      NiceClass parent = getParent();
      if (parent != null)
        exists = parent.checkValueOverride(name, value);

      if (! exists)
        throw User.error(name, "No field with this name exists in a super-class");
    }

    LocatedString name;
    Expression value;
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
    createDefaultConstructors();
    addPublicCloneMethod();
  }

  private void resolveFields()
  {
    for (int i = 0; i < fields.length; i++)
      fields[i].resolve(definition.scope, localScope);

    for (int i = 0; i < overrides.length; i++)
      overrides[i].resolve(definition.scope, localScope);
 
    for (Iterator it = valueOverrides.iterator(); it.hasNext(); )
      ((ValueOverride)it.next()).resolve(definition.scope, localScope);

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

  private boolean checkValueOverride(LocatedString name, Expression value)
  {
   
    Field original = null;

    for (int i = 0; i < fields.length; i++)
      if (fields[i].sym.getName().toString().equals(name.toString()))
        original = fields[i];

    for (int i = 0; i < overrides.length; i++)
      if (overrides[i].sym.getName().toString().equals(name.toString()))
        original = overrides[i];

    if (original != null)
      {
        NiceClass.this.enterTypingContext();

	mlsub.typing.Polytype declaredType = original.sym.getType();
	value = value.resolveOverloading(declaredType);

	dispatch.typecheck(value);

	try {
	  Typing.leq(value.getType(), declaredType);
	} 
	catch (mlsub.typing.TypingEx ex) {
	  User.error(name, "Value does not fit in the overriden field of type " + declaredType);
	}
        
        return true;
      }

    NiceClass parent = getParent();
    if (parent != null)
      return parent.checkValueOverride(name, value);
    else
      return false;
   }

  /****************************************************************
   * Initializers
   ****************************************************************/

  private Statement[] initializers = Statement.noStatements;

  public void setInitializers(List inits)
  {
    if (inits != null)
      initializers = (Statement[]) inits.toArray(new Statement[inits.size()]);
  }

  private int nbInitializers() { return initializers.length; }

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

  /**
     Reference to the method performing instance initialization for this class.
  */
  private gnu.expr.Expression initializer;

  gnu.expr.Expression getInitializer()
  {
    if (initializer != null)
      return initializer;

    gnu.expr.Expression parentInitializer = null;
    NiceClass parent = this.getParent();
    if (parent != null)
      {
        parentInitializer = parent.getInitializer();
      }

    if (nbInitializers() == 0 && parentInitializer == null)
      return null;

    gnu.expr.Expression[] params = new gnu.expr.Expression[1];
    gnu.expr.LambdaExp lambda = Gen.createMemberMethod
      ("$init", classe.getType(), null, gnu.bytecode.Type.void_type, params);
    thisExp = params[0];

    int nPrefix = parentInitializer == null ? 0 : 1;
    gnu.expr.Expression[] body = new gnu.expr.Expression
      [nPrefix + nbInitializers()];

    // Call the parent initializer if present
    if (parentInitializer != null)
      body[0] = Gen.superCall
        (parentInitializer, new gnu.expr.Expression[]{ thisExp });

    // Compile the initializers of this class
    for (int i = 0; i < nbInitializers(); i++)
      body[nPrefix + i] = initializers[i].generateCode();

    Gen.setMethodBody(lambda, new gnu.expr.BeginExp(body));
    initializer = addJavaMethod(lambda);

    return initializer;
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

      for (Iterator it = valueOverrides.iterator(); it.hasNext(); )
        ((ValueOverride)it.next()).typecheck();

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
       + serialUIDFieldString()
       + Util.map("", ";\n", ";\n", overrides)
       + "}\n\n"
       );
  }

  String serialUIDFieldString()
  {
    if (serialVersionUIDValue == null)
      return "";
    
    return "final long serialVersionUID = " + serialVersionUIDValue + "L;\n";
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
       However, the ClassExp object refers to the existing ClassType object,
       and only adds or modifies features when needed.
    */
    classe = definition.module.getClassExp(this);
  }

  static gnu.bytecode.Method cloneMethod = 
    gnu.bytecode.Type.pointer_type.getDeclaredMethod("clone", 0);

  private void addPublicCloneMethod()
  {
    if (! definition.implementsJavaInterface("java.lang.Cloneable"))
      return;

    gnu.expr.Expression[] params = new gnu.expr.Expression[1];
    gnu.expr.LambdaExp lambda = createJavaMethod("clone", cloneMethod, params);
    Gen.setMethodBody
      (lambda, new gnu.expr.ApplyExp(Gen.superCaller(cloneMethod), params));
    addJavaMethod(lambda);
  }

  public gnu.expr.ClassExp createClassExp()
  {
    gnu.expr.ClassExp res = new gnu.expr.ClassExp();
    res.setName(definition.name.toString());
    definition.location().write(res);
    res.setSimple(true);
    res.setAccessFlags(definition.getBytecodeFlags());
    definition.module.addUserClass(res);
    return res;
  }

  gnu.expr.ClassExp classe;

  public gnu.expr.ClassExp getClassExp()
  {
    return classe;
  }

  /**
      @return the scope that maps the type parameters of the other class
        to the corresponding symbol in the constructor of this class.
  */
  private TypeScope translationScope(NiceClass other)
  {
    mlsub.typing.TypeSymbol[] binders = other.definition.getBinders();
    mlsub.typing.TypeSymbol[] ourBinders = definition.getBinders();
    TypeScope scope = Node.getGlobalTypeScope();
    if (binders != null)
      {
	scope = new TypeScope(scope);
	for (int i = 0; i < binders.length; i++)
	  try {
	    scope.addMapping(ourBinders[i].toString(), binders[i]);
	  } catch(TypeScope.DuplicateName e) {}
      }

    return scope;
  }

  /**
     Collect in 'constraints' the constraints set by each class
     on the type parameters.
  */
  private List getNativeConstructorParameters
    (TypeConstructor tc, List constraints)
  {
    List constructors = TypeConstructors.getConstructors(tc);
    if (constructors == null)
      {
        List res = new ArrayList(10);
        List params = new ArrayList(10);
        // null stands for the Object() constructor
        params.add(null);
        res.add(params);
        return res;
      }

    List res = new ArrayList(constructors.size());
    for (Iterator i = constructors.iterator(); i.hasNext();)
      {
	MethodDeclaration m =
          ((MethodDeclaration.Symbol) i.next()).getMethodDeclaration();

        // Only consider parent methods for which a call from this class
        // is legal.
        if (m instanceof JavaMethod)
          {
            gnu.bytecode.ClassType thisClass = classe.getClassType();
            if (! gnu.bytecode.Access.legal(thisClass,
                                            ((JavaMethod) m).reflectMethod,
                                            thisClass))
              continue;
          }

	List params = new ArrayList(10);
        params.add(m);
        res.add(params);
	mlsub.typing.Monotype[] args = m.getArgTypes();
	for (int j = 0; j < args.length; j++)
	  params.add(new FormalParameters.Parameter(Monotype.create(args[j])));
      }
    return res;
  }

  private List getParentConstructorParameters
    (List constraints, mlsub.typing.Monotype[] typeParameters)
  {
    TypeScope scope = Node.getGlobalTypeScope();
    Map map = null;
    if (typeParameters != null)
      {
        // Constructs a type scope that maps the type parameters of this
        // class to the corresponding symbol in the constructor.
	scope = new TypeScope(scope);
        map = new HashMap();
        mlsub.typing.Monotype[] ourTypeParameters = 
          definition.getTypeParameters();
	for (int i = 0; i < ourTypeParameters.length; i++)
	  try {
            TypeSymbol ourSym = asTypeSymbol(ourTypeParameters[i]);
            TypeSymbol sym = asTypeSymbol(typeParameters[i]);
            scope.addMapping(ourSym.toString(), sym);
            map.put(ourSym, sym);
	  } catch(TypeScope.DuplicateName e) {}
      }

    ArrayList res = new ArrayList(constructors.size());
    for (Iterator i = constructors.iterator(); i.hasNext();)
      {
        MethodDeclaration decl = ((MethodDeclaration.Symbol) i.next()).
          getMethodDeclaration();

        List params = new ArrayList(1 + decl.arity);
        params.add(decl);
        if (decl.arity > 0)
          params.addAll(decl.formalParameters().getParameters(scope));
        res.add(params);
      }

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

  private TypeSymbol asTypeSymbol(mlsub.typing.Monotype type)
  {
    if (type instanceof TypeSymbol)
      return (TypeSymbol) type;
    else
      return ((MonotypeConstructor) type).getTC();
  }

  private List getConstructorParameters(List constraints, 
                                        mlsub.typing.Monotype[] typeParameters)
  {
    TypeConstructor supTC = definition.getSuperClass();

    NiceClass sup = get(supTC);
    List res;
    if (sup == null)
      res = getNativeConstructorParameters(supTC, constraints);
    else
      res = sup.getParentConstructorParameters(constraints, typeParameters);

    if (overrides.length > 0 || (! valueOverrides.isEmpty()))
      for (Iterator i = res.iterator(); i.hasNext();)
        updateConstructorParameters((List) i.next());

    if (fields.length > 0)
      for (int j = 0; j < res.size(); j++)
        {
          List params = (List) res.get(j);
          for (int i = 0; i < fields.length; i++)
            params.add(fields[i].asParameter());
        }

    if (definition.resolvedConstraints != null)
      constraints.addAll(Arrays.asList(definition.resolvedConstraints));

    return res;
  }

  /**
     This must be done in a given class for every subclass, since they
     have different type parameters.
  */
  private void updateConstructorParameters(List inherited)
  {
    for (int f = 0; f < overrides.length; f++)
      overrides[f].updateConstructorParameter(inherited);

    for (Iterator it = valueOverrides.iterator(); it.hasNext();)
      ((ValueOverride)it.next()).updateConstructorParameter(inherited);
  }

  /**
     This must be done only once per class.
  */
  private void checkFields (FormalParameters.Parameter[] allFields)
  {
    for (int f = 0; f < fields.length; f++)
      fields[f].checkNoDuplicate(allFields, f);

    for (int i = 0; i < overrides.length; i++)
      for (int k = i+1; k < overrides.length; k++)
        if (overrides[i].sym.hasName(overrides[k].sym.getName()))
          User.error(overrides[k].sym, 
             "A field override of the same field exists in this class");

    for (int i = 0; i < valueOverrides.size(); i++)
      for (int k = i+1; k < valueOverrides.size(); k++)
        if (((ValueOverride)valueOverrides.get(i)).name.equals(((ValueOverride)valueOverrides.get(k)).name))
          User.error(((ValueOverride)valueOverrides.get(k)).name, 
             "A field override of the same field exists in this class");

    for (int i = 0; i < overrides.length; i++)
      for (int k = 0; k < valueOverrides.size(); k++)
        if (overrides[i].sym.hasName(((ValueOverride)valueOverrides.get(k)).name))
          User.error(((ValueOverride)valueOverrides.get(k)).name, 
             "A field override of the same field exists in this class");


  }

  private Constructor[] constructorMethod;

  private void createDefaultConstructors()
  {
    if (definition.inInterfaceFile())
      // The constructors are loaded from the compiled package.
      return;

    if (isInterface())
      return;

    List constraints;
    mlsub.typing.TypeSymbol[] binders = definition.getBinders();
    if (binders == null)
      constraints = null;
    else 
      constraints = new LinkedList();

    mlsub.typing.Monotype[] typeParameters = definition.getTypeParameters();

    List allConstructorParams = 
      getConstructorParameters(constraints, typeParameters);

    Constraint cst;
    if (binders != null)
      cst = new Constraint
	(binders, (AtomicConstraint[])
	 constraints.toArray(new AtomicConstraint[constraints.size()]));
    else
      cst = Constraint.True;

    constructorMethod = new Constructor[allConstructorParams.size()];
    for (int i = 0; i < allConstructorParams.size(); i++)
      {
        List argList = (List) allConstructorParams.get(i);
        MethodDeclaration parent = (MethodDeclaration) argList.get(0);
        argList = argList.subList(1, argList.size());
        FormalParameters.Parameter[] args = (FormalParameters.Parameter[])
          argList.toArray(new FormalParameters.Parameter[argList.size()]);

        // Check only once.
        if (i == 0)
          checkFields(args);

	FormalParameters values = new FormalParameters(args);

	constructorMethod[i] = new DefaultConstructor
	  (this, fields, parent, definition.location(),
	   values, 
	   cst,
	   Monotype.resolve(definition.getLocalScope(), values.types()),
	   Monotype.sure(new MonotypeConstructor(definition.tc, definition.getTypeParameters())));

	TypeConstructors.addConstructor(definition.tc, constructorMethod[i]);
      }
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
    createSerialUIDField();
  }

  /**
     Called instead of compile if the package is up-to-date.
  */
  public void recompile()
  {
    // This needs to be done even if we don't recompile, 
    // since classes are always regenerated.
    if (constructorMethod != null)
      for (int i = 0; i < constructorMethod.length; i++)
	constructorMethod[i].getCode();

    // Take into account external interface implementations, which 
    // can add new interfaces to implement in the bytecode.
    classe.supers = computeSupers();
    classe.recomputeInterfaces();
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

  void createSerialUIDField()
  {
    if (serialVersionUIDValue == null)
      return;
   
    Declaration fieldDecl = classe.addDeclaration("serialVersionUID", SpecialTypes.longType);
    fieldDecl.setSimple(false);
    fieldDecl.setCanRead(true);
    fieldDecl.setFlag(Declaration.IS_CONSTANT);
    fieldDecl.setSpecifiedPrivate(true);
    fieldDecl.setFlag(Declaration.STATIC_SPECIFIED);
    fieldDecl.setFlag(Declaration.TYPE_SPECIFIED);
    fieldDecl.noteValue(new QuoteExp(serialVersionUIDValue, SpecialTypes.longType));
  }

  public Definition importMethod(gnu.bytecode.Method method)
  {
    if (method.isConstructor())
      return ImportedConstructor.load(this, method);

    if (method.getArity() == 0 && method.getName().equals("$init"))
      {
        initializer = Gen.superCaller(method);
        return null;
      }

    return null;
  }

  /****************************************************************
   * Misc.
   ****************************************************************/

  public String toString() { return definition.toString(); }

  private NewField[] fields;
  private OverridenField[] overrides;
  private List valueOverrides;
  private Long serialVersionUIDValue;
}
