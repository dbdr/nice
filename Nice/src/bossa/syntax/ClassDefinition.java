/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
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
abstract public class ClassDefinition extends MethodContainer
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
  public ClassDefinition(LocatedString name, 
			 boolean isFinal, boolean isAbstract, 
			 boolean isInterface,
			 List typeParameters,
			 List typeParametersVariances,
			 List extensions, 
			 List implementations, List abstractions
			 )
  {
    super(name, Node.upper, typeParameters, typeParametersVariances);

    if(isInterface)
      isAbstract = true;
    
    // Testing well formedness
    if(isInterface)
      {
	if(isFinal)
	  User.error(name,
		     "Interfaces can't be final");
	
	if(implementations!=null && implementations.size()>0)
	  User.error(name,
		     "Interfaces can't implement anything");
	
	if(abstractions!=null && abstractions.size()>0)
	  User.error(name,
		     "Interfaces can't abstract anything");	  
      }
    
    this.isFinal = isFinal;
    this.isAbstract = isAbstract;
    this.isInterface = isInterface;
    
    this.extensions = extensions;
    this.implementations = implementations;
    this.abstractions = abstractions;

    createTC();
  }
  
  void createTC()
  {
    String name = this.name.toString();
    if (name.equals("nice.lang.Array"))
      tc = new mlsub.typing.TypeConstructor
	(name.toString(), variance, isConcrete(), true)
	{
	  public String toString(mlsub.typing.Monotype[] parameters)
	  { 
	    String component = parameters[0].toString();
	    if (component.indexOf("->") != -1)
	      return "nice.lang.Array<"+ component + ">";
	    else
	      return component + "[]"; 
	  }
	};
    else if (name.equals("nice.lang.Sure"))
      tc = new mlsub.typing.TypeConstructor
	(name.toString(), variance, isConcrete(), true)
	{
	  public String toString(mlsub.typing.Monotype[] parameters)
	  { 
	    if (parameters[0] instanceof MonotypeVar)
	      return "!" + parameters[0].toString();
	    else
	      //  return "!" + parameters[0].toString(); 
	      return parameters[0].toString(); 
	  }
	};
    else if (name.equals("nice.lang.Maybe"))
      tc = new mlsub.typing.TypeConstructor
	(name.toString(), variance, isConcrete(), true)
	{
	  public String toString(mlsub.typing.Monotype[] parameters)
	  { return "?" + parameters[0].toString(); }
	};
    else if (name.equals("nice.lang.Null"))
      {
	tc = new mlsub.typing.TypeConstructor
	  ("null", variance, isConcrete(), true);
	ConstantExp.registerPrimType(name.toString(),tc);
      }
    else
      {
	tc = new mlsub.typing.TypeConstructor
	  (name.toString(), variance, isConcrete(), true);
	if (name.equals("nice.lang.Throwable"))
	  ConstantExp.throwableTC = tc;
      }

    if(isInterface)
      associatedInterface = new Interface(variance, tc);

    tcToClassDef.put(tc, this);
    Typing.introduce(tc);
    addTypeSymbol(tc);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }

  /****************************************************************
   * Map TypeConstructors to ClassDefinitions
   ****************************************************************/

  private static HashMap tcToClassDef;
  public static void reset() { tcToClassDef = new HashMap(); }
  
  static final ClassDefinition get(TypeConstructor tc)
  {
    return (ClassDefinition) tcToClassDef.get(tc);
  }
  
  /****************************************************************
   * Selectors
   ****************************************************************/

  public mlsub.typing.TypeSymbol getTypeSymbol()
  {
    if (associatedInterface != null)
      return associatedInterface;
    else
      return tc;
  }
  
  protected mlsub.typing.Monotype lowlevelMonotype()
  {
    return new mlsub.typing.MonotypeConstructor(tc, typeParameters);
  }
  
  public final boolean isConcrete()
  {
    return !isAbstract;
  }

  /****************************************************************
   * Resolution
   ****************************************************************/

  void resolve()
  {
    // If superClass is non null, it was build before.
    // Extensions should be null in that case.
    if(superClass==null)
      {
	superClass = TypeIdent.resolveToTC(typeScope, extensions);
	extensions = null;
      }
    
    impl = TypeIdent.resolveToItf(typeScope, implementations);
    abs = TypeIdent.resolveToItf(typeScope, abstractions);
    
    implementations = abstractions = null;
    
    // A class cannot extend an interface
    if(!isInterface && superClass!=null)
      for(int i=0; i<superClass.length; i++)
	{
	  ClassDefinition def = ClassDefinition.get(superClass[i]);
	  if(def!=null && def.getAssociatedInterface()!=null)
	    User.error(name,
		       tc+" is an interface, so "+name+
		       " may only implement it");
	}

    createAssociatedInterface();
  }

  /****************************************************************
   * Initial Context
   ****************************************************************/

  public void createContext()
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
      
      if (isFinal)
	Typing.assertMinimal(tc);

      if (impl != null)
	try{
	  Typing.assertImp(tc, impl, true);
	}
	catch(KindingEx e){
	  User.error(name,
		     "Class " + name + " cannot implement " + e.t2 +
		     ": they do not have the same number or kind of type parameters");
	}

      if (associatedInterface != null)
	Typing.assertImp(tc, associatedInterface, true);
      
      if (abs != null)
	{
	  Typing.assertImp(tc, abs, true);
	  Typing.assertAbs(tc, abs);
	}      
    }
    catch(TypingEx e){
      User.error(name, "Error in class " + name + " : " + e.getMessage());
    }
  }

  /****************************************************************
   * Module Interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print
      (
         (isFinal ? "final " : "")
       + (isInterface ? "interface " : 
	  (isAbstract ? "abstract " : "") + "class ")
       + getSimpleName()
       + printTP()
       + Util.map(" extends ",", ","",superClass)
       + Util.map(" implements ",", ","",impl)
       + Util.map(" finally implements ",", ","",abs)
      );
  }
  
  private String printTP()
  {
    if (typeParameters == null)
      return "";

    StringBuffer res = new StringBuffer("<");
    for (int n = 0; n < typeParameters.length; n++)
      {
	switch (variance.getVariance(n)) 
	  {
	  case Variance.CONTRAVARIANT: 
	    res.append("-");
	    break;
	  case Variance.COVARIANT: 
	    res.append("+");
	    break;
	  }
	res.append(typeParameters[n].toString());
	if (n + 1 < typeParameters.length)
	  res.append(", ");
      }
    return res.append(">").toString();
  }

  /****************************************************************
   * Class hierarchy
   ****************************************************************/

  // Bossa has multiple inheritance, java has not.
  // So we choose a class that will be described as the super-class
  // in the bytecode (javaSuperClass)

  final ClassDefinition superClass(int n)
  {
    return get(superClass[n]);
  }
  
  /**
   * Our super-class in the bytecode.
   */
  ClassDefinition distinguishedSuperClass()
  {
    if(superClass == null)
      return null;
    else
      return superClass(0);
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

  final ClassType javaClass()
  {
    return (ClassType) javaType;
  }

  final static Type javaClass(ClassDefinition c)
  {
    if (c == null)
      return gnu.bytecode.Type.pointer_type;
    else
      return c.getJavaType();
  }
  
  ClassType javaSuperClass()
  {
    Type res;
    
    if(superClass == null)
      res = gnu.bytecode.Type.pointer_type;
    else
      res = nice.tools.code.Types.javaType(superClass[0]);
    
    if(!(res instanceof ClassType))
      Internal.error("Java type="+res+"\nOnly special arrays are not a class type, and they must be final");
    
    return (ClassType) res;
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
    if(associatedInterface==null)
      return;

    // the associated interface extends the associated interfaces
    // of the classes we extend
    if(superClass!=null)
      for(int i = 0; i<superClass.length; i++)
	{
	  ClassDefinition c = ClassDefinition.get(superClass[i]);
	  if(c==null)
	    Internal.error(name,
			   "Superclass "+tc+
			   "("+tc.getClass()+")"+
			   " of "+name+
			   " has no definition");
	
	  Interface ai = c.getAssociatedInterface();
	  if(ai==null)
	    Internal.error("We should extend only \"interface\" classes");
	
	  try{
	    Typing.assertLeq(associatedInterface, ai);
	  }
	  catch(KindingEx e){
	    User.error(this, "Cannot extend interface " + ai + 
		       " which has a different variance");
	  }
	}
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "class "+name;
  }

  mlsub.typing.TypeConstructor tc;

  protected List
    /* of TypeConstructor */ extensions,
    /* of Interface */ implementations,
    /* of Interface */ abstractions;
  TypeConstructor[] superClass;
  Interface[] impl, abs;
  protected boolean isFinal, isInterface;
  
  boolean isAbstract;
}
