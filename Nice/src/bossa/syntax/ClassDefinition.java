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

    mlsub.typing.Interface[] getInterfaces() { return extendedInterfaces; }

    void resolveClass()
    {
      extendedInterfaces = this.resolveInterfaces(extensions);
      extensions = null;

      // Resolve the super-interfaces first.
      if (extendedInterfaces != null)
	for (int i = 0; i < extendedInterfaces.length; i++)
	  {
	    ClassDefinition d = ClassDefinition.get(extendedInterfaces[i].associatedTC());
	    if (d != null)
	      d.resolve();
	  }

      createAssociatedInterface();

      super.resolveClass();
    }

    void createContext()
    {
      try{
	if (extendedInterfaces != null)
	  try{
	    Typing.assertImp(tc, extendedInterfaces, true);
	  }
	  catch(KindingEx e){
	    User.error(name,
		       "Interface " + name + " cannot extend " + e.t2 +
		       ": they do not have the same number or kind of type parameters");
	  }
      
	if (javaInterfaces != null)
	  for (int i = 0; i < javaInterfaces.length; i++)
	    try {
	      Typing.initialLeq(tc, javaInterfaces[i]);
	    }
	    catch(KindingEx e){
	      User.error(name,
			 "Interface " + name + " cannot extend " + e.t2 +
			 ": they do not have the same number or kind of type parameters");
	    }

	Typing.assertImp(tc, associatedInterface, true);
      }
      catch(TypingEx e){
	User.error(name, "Error in interface " + name + " : " + e.getMessage());
      }

      super.createContext();
    }

    public void printInterface(java.io.PrintWriter s)
    {
      super.printInterface(s);
      s.print("interface ");
      s.print(getSimpleName());
      s.print(this.printTypeParameters());
      s.print(printInterfaces(" extends ", extendedInterfaces));
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
      if (extendedInterfaces != null)
	for(int i = 0; i < extendedInterfaces.length; i++)
	  {
	    mlsub.typing.Interface ai = extendedInterfaces[i];
	
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
    mlsub.typing.Interface[] extendedInterfaces;
  }

  public static 
    Class makeClass(LocatedString name, 
		    boolean isFinal, boolean isAbstract, 
		    Constraint typeParameters,
		    List typeParametersVariances,
		    TypeIdent superClassIdent, 
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
	  TypeIdent superClassIdent, 
	  List implementations, List abstractions
	  )
    {
      super(name, typeParameters, typeParametersVariances,
            implementations, abstractions);

      this.isFinal = isFinal;
      this.isAbstract = isAbstract;
    
      this.superClassIdent = superClassIdent;

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

    mlsub.typing.Interface[] getInterfaces() { return impl; }

    public ClassDefinition.Interface[] getImplementedInterfaces()
    {
      if (impl == null) return null;
      List res = new LinkedList();

      for (int i = 0; i < impl.length; i++)
        { 
          Object itf = ClassDefinition.get(impl[i].associatedTC());
          if (itf != null)
            res.add(itf);
        }

      return (ClassDefinition.Interface[])
        res.toArray(new ClassDefinition.Interface[res.size()]);
    }

    void resolveClass()
    {
      if (superClassIdent != null)
	{
	  superClass = superClassIdent.resolveToTC(typeScope);

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
      s.print(printInterfaces(" implements ", impl));
      s.print(Util.map(" finally implements ",", ","",abs));
      implementation.printInterface(s);
    }

    TypeIdent superClassIdent;
    TypeConstructor superClass;
    protected boolean isFinal;
  
    boolean isAbstract;
  }

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
    super(name, Node.upper, typeParameters, typeParametersVariances);
    this.implementations = implementations;
    this.abstractions = abstractions;
  }
  
  protected List
    /* of Interface */ implementations,
    /* of Interface */ abstractions;
  mlsub.typing.Interface[] impl, abs;

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
  
  /****************************************************************
   * Selectors
   ****************************************************************/

  public abstract mlsub.typing.TypeSymbol getTypeSymbol();
  public abstract mlsub.typing.Interface getAssociatedInterface();
  
  protected mlsub.typing.Monotype lowlevelMonotype()
  {
    return new mlsub.typing.MonotypeConstructor(tc, getTypeParameters());
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
	super.resolve();
	resolveClass();
      }
    finally
      {
	status = RESOLVED;
      }
  }

  void resolveClass()
  {
    impl = this.resolveInterfaces(implementations);
    abs = TypeIdent.resolveToItf(typeScope, abstractions);
    
    implementations = abstractions = null;

    // Resolve the super-interfaces first.
    if (impl != null)
      for (int i = 0; i < impl.length; i++)
        {
          ClassDefinition d = ClassDefinition.get(impl[i].associatedTC());
          if (d != null)
            d.resolve();
        }

    createContext();
    implementation.resolveClass();
  }

  /** Java interfaces implemented or extended by this class/interface. */
  TypeConstructor[] javaInterfaces;

  mlsub.typing.Interface[] resolveInterfaces(List names)
  {
    if (names == null)
      return null;

    mlsub.typing.Interface[] res = new mlsub.typing.Interface[names.size()];
    int n = 0;
    ArrayList javaInterfaces = null;

    for (Iterator i = names.iterator(); i.hasNext();)
      {
	TypeIdent name = (TypeIdent) i.next();
	TypeSymbol s = name.resolvePreferablyToItf(typeScope);
	
	if (s instanceof mlsub.typing.Interface)
	  res[n++] = (mlsub.typing.Interface) s;
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

    if (n < res.length) // The array is too long
      {
	mlsub.typing.Interface[] tmp = new mlsub.typing.Interface[n];
	System.arraycopy(res, 0, tmp, 0, n);
	res = tmp;
	this.javaInterfaces = (TypeConstructor[])
	  javaInterfaces.toArray(new TypeConstructor[javaInterfaces.size()]);
      }

    return res;
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
      if (impl != null)
        try{
          Typing.assertImp(tc, impl, true);
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
    }
    catch(TypingEx e){
      User.error(name, "Error in " + name + " : " + e.getMessage());
    }
  }

  /****************************************************************
   * Class hierarchy
   ****************************************************************/

  /**
   * Our super-class, or null.
   */
  abstract TypeConstructor getSuperClass();

  abstract mlsub.typing.Interface[] getInterfaces();
  
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

  String printInterfaces(String keyword, mlsub.typing.Interface[] interfaces)
  {
    StringBuffer res = new StringBuffer();
    if (interfaces != null || javaInterfaces != null)
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

