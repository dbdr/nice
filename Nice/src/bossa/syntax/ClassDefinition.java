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
import mlsub.typing.Monotype;

import gnu.bytecode.*;
import java.util.*;

/**
   Abstract syntax for a class definition.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */
public abstract class ClassDefinition extends MethodContainer
{
  public static 
    Interface makeInterface(LocatedString name, 
			    Constraint typeParameters, 
			    List typeParametersVariances,
			    List extensions,
                            List implementations, List abstractions)
  {
    return new Interface(name, typeParameters, typeParametersVariances, 
			 extensions, implementations, abstractions);
  }

  public static class Interface extends ClassDefinition
  {
    Interface(LocatedString name, 
	      Constraint typeParameters, List typeParametersVariances,
	      List extensions, 
              List implementations, List abstractions)
    {
      super(name, typeParameters, typeParametersVariances,
            implementations, abstractions);
      this.extensions = extensions;

      // Find out the real variance of the interface
      int arity = getVariance(extensions);
      if (arity == -1)
        arity = getVariance(implementations);
      if (arity == -1)
        arity = getVariance(abstractions);

      if (arity == -1 || arity <= typeParametersVariances.size())
        this.variance = makeVariance(typeParametersVariances);
      else
        this.variance = Variance.make(new int[arity]);

      this.createTC();
      associatedInterface = new mlsub.typing.Interface(variance, tc);
    }

    public boolean isConcrete()
    {
      return false;
    }

    int getBytecodeFlags() { return Access.INTERFACE; }

    boolean implementsJavaInterface(String name)
    {
      return false;
    }

    public mlsub.typing.TypeSymbol getTypeSymbol()
    {
      return associatedInterface;
    }
  
    TypeConstructor getSuperClass() { return null; }

    void resolveClass()
    {
      this.resolveInterfaces(extensions);
      extensions = null;

      createAssociatedInterface();

      super.resolveClass();
    }

    void createContext()
    {
      try{
	Typing.assertImp(tc, associatedInterface, true);
      }
      catch(TypingEx e){
	User.error(name, "Error in interface " + name, e.getMessage());
      }

      super.createContext();
    }

    public void printInterface(java.io.PrintWriter s)
    {
      super.printInterface(s);
      s.print("interface ");
      s.print(getSimpleName());
      s.print(this.printTypeParameters());
      s.print(printInterfaces(" extends ", interfaces));
      implementation.printInterface(s);
    }

    /****************************************************************
     * Associated interface
     ****************************************************************/

    private mlsub.typing.Interface associatedInterface;

    /**
     * Returns the abstract interface associated to this class, or null.
     *
     * An associated abstract interface in created
     * for each "interface" class.
     */
    public mlsub.typing.Interface getAssociatedInterface()
    { return associatedInterface; }

    private void createAssociatedInterface()
    {
      // the associated interface extends the associated interfaces
      // of the classes we extend
      for (Iterator i = interfaces.iterator(); i.hasNext();)
        {
          mlsub.typing.Interface ai = (mlsub.typing.Interface) i.next();

          try{
            Typing.assertLeq(associatedInterface, ai);
          }
          catch(KindingEx e){
            User.error(this, "Cannot extend interface " + ai +
                       " which has a different variance");
          }
        }
    }

    protected List
      /* of TypeConstructor */ extensions;
  }

  public static
    Class makeClass(LocatedString name,
		    boolean isFinal, boolean isAbstract,
		    Constraint typeParameters,
		    List typeParametersVariances,
		    MonotypeConstructor superClassIdent,
		    List implementations, List abstractions
		    )
  {
    return new Class(name, isFinal, isAbstract,
		     typeParameters, typeParametersVariances,
		     superClassIdent, implementations, abstractions);
  }

  public static class Class extends ClassDefinition
  {
    Class(LocatedString name,
	  boolean isFinal, boolean isAbstract,
	  Constraint typeParameters,
	  List typeParametersVariances,
	  MonotypeConstructor superClassIdent,
	  List implementations, List abstractions
	  )
    {
      super(name, typeParameters, typeParametersVariances,
            implementations, abstractions);

      this.isFinal = isFinal;
      this.isAbstract = isAbstract;

      this.superClassIdent = superClassIdent;

      // Find out the real variance of the interface
      int arity = getVariance(superClassIdent);
      if (arity == -1)
        arity = getVariance(implementations);
      if (arity == -1)
        arity = getVariance(abstractions);

      if (arity == -1 || arity <= typeParametersVariances.size())
        this.variance = makeVariance(typeParametersVariances);
      else
        this.variance = Variance.make(new int[arity]);

      this.createTC();
      if (isFinal)
	tc.setMinimal();
    }

    public boolean isConcrete()
    {
      return !isAbstract;
    }

    int getBytecodeFlags() 
    { 
      if (isFinal) 
	return Access.FINAL;
      else if (isAbstract)
	return Access.ABSTRACT;
      else return 0;
    }

    boolean implementsJavaInterface(String name)
    {
      if (javaInterfaces != null)
        for (int i = 0; i < javaInterfaces.length; i++)
          if (javaInterfaces[i].toString().equals(name))
            return true;
      return false;
    }

    public mlsub.typing.TypeSymbol getTypeSymbol()
    {
      return tc;
    }
  
    public mlsub.typing.Interface getAssociatedInterface()
    { 
      return null;
    }

    TypeConstructor getSuperClass() { return superClass; }

    public ClassDefinition.Class getSuperClassDefinition()
    {
      return (ClassDefinition.Class) ClassDefinition.get(superClass);
    }

    public ClassDefinition.Interface[] getImplementedInterfaces()
    {
      if (interfaces.size() == 0) return null;
      List res = new LinkedList();

      for (Iterator i = interfaces.iterator(); i.hasNext();)
        {
          mlsub.typing.Interface ai = (mlsub.typing.Interface) i.next();

          ClassDefinition itf = ClassDefinition.get(ai.associatedTC());
          if (itf != null)
            res.add(itf);
        }

      return (ClassDefinition.Interface[])
        res.toArray(new ClassDefinition.Interface[res.size()]);
    }

    void resolveClass()
    {
      Monotype[] params = null;
      if (superClassIdent != null)
	{
          params = resolveParent(superClassIdent, getLocalScope());
	  superClass = superClassIdent.tc.resolveToTC(typeScope);

	  if (superClass.isMinimal())
	    User.error(superClassIdent,
		       superClass + " is a final class. It cannot be extended");
	  if (TypeConstructors.isInterface(superClass))
	    User.error(superClassIdent,
		       superClass + " is an interface, so " + name +
		       " may only implement it");

	  superClassIdent = null;
	}

      ClassDefinition d = getSuperClassDefinition();
      if (d != null)
        {
          d.resolve();

          useParent(d, params);

          if(d.getImplementation() instanceof PrimitiveType && ! 
		(this.getImplementation() instanceof PrimitiveType))
            User.error(this, "A class can't extends a primitive");
        }
      super.resolveClass();
    }

    void createContext()
    {
      try{
	if (superClass != null)
	  try{
	    Typing.initialLeq(tc, superClass);
	  }
	  catch(KindingEx e){
	    User.error(name,
		       "Class " + name + " cannot extend " + e.t2 +
		       ": they do not have the same number or kind of type parameters");
	  }

      }
      catch(TypingEx e){
	User.error(name, "Error in class " + name + " : " + e.getMessage());
      }

      super.createContext();
    }

    public void printInterface(java.io.PrintWriter s)
    {
      super.printInterface(s);
      if (isFinal) s.print("final ");
      if (isAbstract) s.print("abstract ");
      s.print("class ");
      s.print(getSimpleName());
      s.print(this.printTypeParameters());
      if (superClass != null)
	s.print(" extends " + superClass);
      s.print(printInterfaces(" implements ", interfaces));
      s.print(Util.map(" finally implements ",", ","",abs));
      implementation.printInterface(s);
    }

    MonotypeConstructor superClassIdent;
    TypeConstructor superClass;
    protected boolean isFinal;
  
    boolean isAbstract;
  }

  /****************************************************************
   * Generic definition, common to classes and interfaces
   ****************************************************************/

  /**
   * Creates a class definition.
   *
   * @param name the name of the class
   * @param typeParameters a list of type symbols
   * @param extensions a list of TypeConstructors
   */
  public ClassDefinition(LocatedString name, 
			 Constraint typeParameters, 
			 List typeParametersVariances,
                         List implementations, List abstractions
                         )
  {
    super(name, Node.upper, typeParameters);
    this.implementations = implementations;
    this.abstractions = abstractions;
  }

  protected List
    /* of Interface */ implementations,
    /* of Interface */ abstractions;

  mlsub.typing.Interface[] abs;

  List interfaces = new ArrayList(5);

  void createTC()
  {
    String name = this.name.toString();
    if (name.equals("nice.lang.Array"))
      tc = new mlsub.typing.TypeConstructor
	(name, variance, isConcrete(), true)
	{
	  public String toString(mlsub.typing.Monotype[] parameters)
	  { 
	    return parameters[0].toString(false, "[]");
	  }

	  public String toString(mlsub.typing.Monotype[] parameters,
				 boolean isNull, String suffix)
	  {
	    if (suffix == null)
	      suffix = "";
	    return parameters[0].toString(false, suffix + 
					  (isNull ? "[?]" : "[]"));
	  }
	};
    else if (name.equals("nice.lang.Sure"))
      tc = new mlsub.typing.TypeConstructor
	(name, mlsub.typing.NullnessKind.instance, isConcrete(), true)
	{
	  public String toString(mlsub.typing.Monotype[] parameters,
				 boolean isNull, String suffix)
	  { 
	    if (parameters[0] instanceof MonotypeVar)
	      return "!" + parameters[0].toString(false, suffix);
	    else
	      //  return "!" + parameters[0].toString(); 
	      return parameters[0].toString(false, suffix);
	  }
	};
    else if (name.equals("nice.lang.Maybe"))
      tc = new mlsub.typing.TypeConstructor
	(name, mlsub.typing.NullnessKind.instance, isConcrete(), true)
	{
	  public String toString(mlsub.typing.Monotype[] parameters,
				 boolean isNull, String suffix)
	  { return parameters[0].toString(true, suffix); }
	};
    else if (name.equals("nice.lang.Null"))
      {
	tc = new mlsub.typing.TypeConstructor
	  ("null", mlsub.typing.NullnessKind.instance, isConcrete(), true);
	PrimitiveType.registerPrimType(name, tc);
      }
    else
      {
	tc = new mlsub.typing.TypeConstructor
	  (name, variance, isConcrete(), true);
	if (name.equals("nice.lang.Throwable"))
	  PrimitiveType.throwableTC = tc;
        else if (name.equals("nice.lang.Collection"))
          PrimitiveType.collectionTC = tc;
        else if (name.equals("nice.lang.Class"))
          PrimitiveType.classTC = tc;
      }

    tcToClassDef.put(tc, this);
    Typing.introduce(tc);
    addTypeSymbol(tc);
  }

  TypeScope getLocalScope()
  {
    TypeScope localScope = typeScope;
    if (classConstraint != null)
      try{
	localScope = new TypeScope(localScope);
        mlsub.typing.TypeSymbol[] binders = getBinders();
        for (int i = 0; i < binders.length; i++)
	  localScope.addSymbol(binders[i]);
      }
      catch(TypeScope.DuplicateName e){
	User.error(this, e);
      }
    return localScope;
  }

  /****************************************************************
   * Map TypeConstructors to ClassDefinitions
   ****************************************************************/

  private static Map tcToClassDef;
  public static void reset() { tcToClassDef = new HashMap(); }

  public static final ClassDefinition get(TypeConstructor tc)
  {
    return (ClassDefinition) tcToClassDef.get(tc);
  }

  public static final ClassDefinition get(mlsub.typing.Interface itf)
  {
    return ClassDefinition.get(itf.associatedTC());
  }

  /****************************************************************
   * Selectors
   ****************************************************************/

  public abstract mlsub.typing.TypeSymbol getTypeSymbol();
  public abstract mlsub.typing.Interface getAssociatedInterface();
  
  protected mlsub.typing.Monotype lowlevelMonotype()
  {
    return new mlsub.typing.MonotypeConstructor(tc, getTypeParameters());
  }
  
  /**
     Return the type representing a constrained instance of this class,
     or null if this class has no constraint on its type parameters.
  */
  mlsub.typing.Polytype getConstrainedType()
  {
    mlsub.typing.Constraint cst = getResolvedConstraint();

    if (cst == null)
      return null;

    mlsub.typing.Polytype res = new mlsub.typing.Polytype
      (cst, lowlevelMonotype());

    return res.cloneType();
  }

  public abstract boolean isConcrete();

  abstract int getBytecodeFlags();

  abstract boolean implementsJavaInterface(String name);

  /****************************************************************
   * Resolution
   ****************************************************************/

  /** Marker to avoid cycles and multiple resolutions. */
  private int status = NOT_RESOLVED;
  private static int NOT_RESOLVED = 0;
  private static int RESOLVING = 1;
  private static int RESOLVED = 2;

  void resolve()
  {
    if (status == RESOLVED)
      return;
    else if (status == RESOLVING)
      {
	// We found a cycle.
	String message;
	if (TypeConstructors.isInterface(tc))
	  message = "Interface " + getName() + " extends itself";
	else
	  message = "Class " + getName() + " extends itself";

	throw User.error(this, message);
      }

    status = RESOLVING;

    try
      {
	resolveClass();

	super.resolve();

        // This needs to be after super.resolve(), so that the constraint is
        // resolved
        implementation.resolveClass();
      }
    finally
      {
	status = RESOLVED;
      }
  }

  void resolveClass()
  {
    this.resolveInterfaces(implementations);
    abs = AbstractInterface.resolve(typeScope, abstractions);

    implementations = abstractions = null;

    // Resolve the super-interfaces first.
    for (Iterator i = interfaces.iterator(); i.hasNext();)
      {
        mlsub.typing.Interface itf = (mlsub.typing.Interface) i.next();
        ClassDefinition d = ClassDefinition.get(itf.associatedTC());
        if (d != null)
          d.resolve();
      }

    createContext();
  }

  /** Java interfaces implemented or extended by this class/interface. */
  TypeConstructor[] javaInterfaces;

  void resolveInterfaces(List names)
  {
    if (names == null)
      return;

    ArrayList javaInterfaces = null;

    for (Iterator i = names.iterator(); i.hasNext();)
      {
        MonotypeConstructor parent = (MonotypeConstructor) i.next();
        Monotype[] params = resolveParent(parent, getLocalScope());

	TypeIdent name = parent.tc;
	TypeSymbol s = name.resolvePreferablyToItf(typeScope);

	if (s instanceof mlsub.typing.Interface)
	  {
            interfaces.add(s);

            ClassDefinition def = ClassDefinition.get((mlsub.typing.Interface) s);
            if (def != null)
              useParent(def, params);
          }
	else
	  {
	    TypeConstructor tc = (TypeConstructor) s;
	    if (! TypeConstructors.isInterface(tc))
	      throw User.error(name, tc + " is not an interface");

	    if (javaInterfaces == null)
	      javaInterfaces = new ArrayList(5);

	    javaInterfaces.add(s);
	  }
      }

    if (javaInterfaces != null)
      this.javaInterfaces = (TypeConstructor[])
        javaInterfaces.toArray(new TypeConstructor[javaInterfaces.size()]);
  }

  Monotype[] resolveParent(MonotypeConstructor parent, TypeScope typeScope)
  {
    if (parent.parameters == null)
      return null;

    return bossa.syntax.Monotype.resolve(typeScope, parent.parameters.content);
  }

  void resolveBody()
  {
    implementation.resolveBody();
  }

  void typecheckClass()
  {
    implementation.typecheck();
  }

  void typecheck()
  {
    // Typechecking is done earlier, in typecheckClass.
  }

  void precompile()
  {
    if (implementation instanceof NiceClass)
      ((NiceClass) implementation).precompile();
  }

  void compile()
  {
    implementation.compile();
  }

  /**
     Called instead of compile is the package is up-to-date.
  */
  void recompile()
  {
    implementation.recompile();
  }

  /****************************************************************
   * Initial Context
   ****************************************************************/

  void createContext()
  {
    try {
      for (Iterator i = interfaces.iterator(); i.hasNext();)
        try{
          mlsub.typing.Interface itf = (mlsub.typing.Interface) i.next();
          Typing.assertImp(tc, itf, true);
        }
        catch(KindingEx e){
          User.error(name,
                     "Class " + name + " cannot implement " + e.t2 +
                     ": they do not have the same number or kind of type parameters");
        }

      if (abs != null)
        {
          Typing.assertImp(tc, abs, true);
          Typing.assertAbs(tc, abs);
        }

	if (javaInterfaces != null)
	  for (int i = 0; i < javaInterfaces.length; i++)
            if (tc.arity() == 0 ||
                ! JavaClasses.excludedInterface(javaInterfaces[i]))
              try {
                Typing.initialLeq(tc, javaInterfaces[i]);
              }
              catch(KindingEx e){
                User.error(name,
                           "Class " + name + " cannot implement " + e.t2 +
                           ": they do not have the same number or kind of type parameters");
              }
    }
    catch(TypingEx e){
      User.error(name, "Error in " + name + " : " + e.getMessage());
    }
  }

  /**
     Returns a monotype based on this tc, provided that the class
     requires no type parameter because it specializes its parent.
  */
  static Monotype toType(TypeConstructor tc)
  {
    ClassDefinition def = ClassDefinition.get(tc);
    if (def == null)
      return null;

    if (def.parentParams == null)
      return null;

    // Look for missing type parameters
    int missing = 0;
    for (int i = 0; i < def.parentParams.length; i++)
      if (def.parentParams[i] == null)
        missing++;

    if (missing == 0)
      return new mlsub.typing.MonotypeConstructor(tc, def.parentParams);

    return null;
  }

  /**
     Returns a monotype based on this tc, provided that the class
     requires exactly these type parameters because it specializes its parent.
  */
  static Monotype toType(TypeConstructor tc, Monotype[] sourceParams)
  {
    ClassDefinition def = ClassDefinition.get(tc);
    if (def == null)
      return null;

    if (def.parentParams == null)
      return null;

    Monotype[] fullParams = new Monotype[def.parentParams.length];

    int used = 0;
    for (int i = 0; i < fullParams.length; i++)
      if (def.parentParams[i] == null)
        {
          fullParams[i] = sourceParams[def.parentTypeParameterMap[i]];
          used++;
        }
      else
        fullParams[i] = def.parentParams[i];

    if (used < sourceParams.length)
      return null;

    return new mlsub.typing.MonotypeConstructor(tc, fullParams);
  }

  private Monotype[] parentParams;
  private int[] parentTypeParameterMap;

  MethodContainer.Constraint specialize(MethodContainer.Constraint our,
                                        MethodContainer.Constraint parent,
                                        Monotype[] params)
  {
    if (parent == null)
      return null;

    if (params == null)
      return our;

    this.parentParams = params;
    this.parentTypeParameterMap = new int[params.length];

    List binders = new ArrayList();
    List atoms = new ArrayList();
    Monotype[] typeParameters = new Monotype[params.length];

    if (our != null)
      {
        binders.addAll(Arrays.asList(our.getBinderArray()));
        atoms.addAll(our.getAtoms());
      }

    for (int i = 0; i < params.length; i++)
      {
        if (params[i] instanceof MonotypeVar)
          {
            typeParameters[i] = params[i];
            parentTypeParameterMap[i] = find(params[i], our.typeParameters);
            parentParams[i] = null;
            continue;
          }

        MonotypeVar mvar = new MonotypeVar("D" + i);
        binders.add(mvar);
        typeParameters[i] = mvar;

        bossa.syntax.Monotype var = bossa.syntax.Monotype.create(mvar);
        atoms.add(new MonotypeLeqCst(bossa.syntax.Monotype.create(params[i]), var));
        atoms.add(new MonotypeLeqCst(var, bossa.syntax.Monotype.create(params[i])));
      }

    return new MethodContainer.Constraint
      ((TypeSymbol[]) binders.toArray(new TypeSymbol[binders.size()]),
       atoms,
       typeParameters,
       this.location());
  }

  private int find(Object o, Object[] array)
  {
    for (int i = 0; ; i++)
      if (array[i] == o)
        return i;
  }

  int getVariance(List parents)
  {
    if (parents == null)
      return -1;

    int res;

    for (Iterator i = parents.iterator(); i.hasNext();)
      {
        res = getVariance((MonotypeConstructor) i.next());
        if (res != -1)
          return res;
      }

    return -1;
  }

  int getVariance(MonotypeConstructor parent)
  {
    if (parent == null || parent.parameters.content == null)
      return -1;

    // Assume non-variance
    return parent.parameters.content.length;
  }

  void useParent(MethodContainer parent, Monotype[] params)
  {
    if (parentParams == null)
      classConstraint =
        specialize(this.classConstraint, parent.classConstraint, params);

  }

  /****************************************************************
   * Class hierarchy
   ****************************************************************/

  /**
   * Our super-class, or null.
   */
  abstract TypeConstructor getSuperClass();

  mlsub.typing.Interface[] getInterfaces()
  {
    return (mlsub.typing.Interface[])
      interfaces.toArray(new mlsub.typing.Interface[interfaces.size()]);
  }

  void addInterfaceImplementation(mlsub.typing.Interface itf)
  {
    interfaces.add(itf);
  }

  private Type javaType;

  protected void setJavaType(Type javaType)
  {
    this.javaType = javaType;
    nice.tools.code.Types.set(tc, javaType);
  }

  final Type getJavaType()
  {
    return javaType;
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "class "+name;
  }

  String printInterfaces(String keyword, List interfaces)
  {
    StringBuffer res = new StringBuffer();
    if (interfaces.size() != 0 || javaInterfaces != null)
      {
	res.append(keyword);
	String sNice = Util.map("", ", ", "", interfaces);
	res.append(sNice);
	String sJava = Util.map("", ", ", "", javaInterfaces);
	if (sNice != "" && sJava != "")
	  res.append(", ");
	res.append(sJava);
      }
    return res.toString();
  }

  mlsub.typing.TypeConstructor tc;

  public void setImplementation(ClassImplementation implementation)
  {
    this.implementation = implementation;
  }

  abstract static class ClassImplementation
  {
    abstract void resolveClass();
    void resolveBody() {}
    void typecheck() {}
    void compile() {}
    void recompile() {}
    abstract void printInterface(java.io.PrintWriter s);
  }

  ClassImplementation implementation;

  public ClassImplementation getImplementation()
  {
    return implementation;
  }
}

