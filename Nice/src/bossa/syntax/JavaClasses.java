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
import bossa.util.Debug;
import bossa.util.Location;

import mlsub.typing.*;
import bossa.modules.Compilation;

import gnu.bytecode.*;

import java.util.*;
import java.util.jar.*;

/**
   Methods to deal with existing java classes and methods.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public final class JavaClasses
{
  private JavaClasses(){}
  
  /**
   * Returns a java type constructor with the given name
   * and the given java type.
   * <p>
   * Usefull for primitive types.
   */
  static TypeConstructor make
    (Compilation compilation, String name, gnu.bytecode.Type javaType)
  {
    Object o = compilation.javaTypeConstructors.get(javaType);
    if(o!=null)
      return (TypeConstructor) o;
    
    // we put the new JTC in the hashtable in the constructor
    // and not after it returned, to avoid infinite loop
    // if the code of the constructor does a lookup.
    return create(compilation, name, javaType);
  }

  /**
     @return null, except if javaName already had an
     associated TypeConstructor in which case it is returned.
   */
  static TypeConstructor setTypeConstructorForJavaClass
    (Compilation compilation, TypeConstructor tc, Type type)
  {
    Object old = compilation.javaTypeConstructors.put(type, tc);
    return (TypeConstructor) old;
  }
  
  private static final gnu.bytecode.Type[] blackListClass =
    new gnu.bytecode.Type[] {
      ClassType.make("java.lang.Object")
      //,ClassType.make("java.lang.Throwable")
    }
  ;
  
  private static final gnu.bytecode.Type[] blackListInterface =
    new gnu.bytecode.Type[] {
      ClassType.make("java.io.Serializable"),
      ClassType.make("java.lang.Cloneable"),
      ClassType.make("java.lang.Comparable")
    };
  
  private static boolean excluded(gnu.bytecode.Type[] blackList, 
				  Type classType)
  {
    for(int i=0; i<blackList.length; i++)
      if(classType==blackList[i])
	return true;
    return false;
  }
      
  static boolean excludedInterface(TypeConstructor itf)
  {
    gnu.bytecode.Type t = nice.tools.code.Types.get(itf);
    return excluded(blackListInterface, t);
  }

  private static TypeConstructor create
    (Compilation compilation, String className, gnu.bytecode.Type javaType)
  {
    if(Debug.javaTypes)
      Debug.println("Registering java class "+className);

    // as the tc is not introduced, it should not be set instantiable
    // (thus used at link test with an error)
    // these late TCs should be avoided
    boolean instantiable = 
      !Typing.isInRigidContext() && instantiable(javaType);
      
    TypeConstructor res = new TypeConstructor(className, null, 
					      instantiable, true);
    Type old = (Type) compilation.javaTypeConstructors.put(javaType, res);
    if (old != null)
      User.error(old + " was already associated with the Nice class " + old);

    nice.tools.code.Types.set(res, javaType);
    
    if(Typing.isInRigidContext())
      // We should not add new classes at this point.
      // It doesn't matter, as this type is not used explicitely.
      {
	Internal.warning(className + " added late");
	
	setArity(res, 0);
	return res;
      }

    Typing.introduce(res);
    
    // Recursive search for java super-classes
    if(javaType instanceof ClassType)
      {
	ClassType classType = (ClassType) javaType;

	int arity = classType.getArity();
	if (arity != -1)
	  setArity(res, classType.getArity());

	ClassType superClass =  classType.getSuperclass();
	if(superClass!=null && !(excluded(blackListClass, superClass)))
	  {
	    try{
              TypeConstructor superTC = 
                nice.tools.code.Types.typeConstructor(superClass);
	      Typing.initialLeq(res, superTC);
	    }
	    catch(TypingEx e){
	      Internal.warning("Invalid java super-class "+superClass+" for "+className);
	    }
            catch (nice.tools.code.Types.NotIntroducedClassException e) {
              Internal.warning
                (res + " extends " + superClass + ", which is not usable");
              }
	  }

	ClassType[] itfs = null;
	try{
	  itfs = classType.getInterfaces();
	}
	catch(NoClassDefFoundError ex){
	  User.warning("Interface " + ex.getMessage() + 
		       " implemented by " + classType.getName() + 
		       " was not found in classpath");
	}
	if(itfs!=null)
	  for(int i=0; i<itfs.length; i++)
	    if(!(excluded(blackListInterface,itfs[i])))
	    {
	      try{
                TypeConstructor superTC = 
                  nice.tools.code.Types.typeConstructor(itfs[i]);
		Typing.initialLeq(res, superTC);
	      }
	      catch(TypingEx e){
		Internal.warning
		  (res+" cannot implement " + itfs[i]
		   /* + ": " + e.toString()*/);
	      }
              catch (nice.tools.code.Types.NotIntroducedClassException e) {
                Internal.warning
		  (res + " implements " + itfs[i] + ", which is not usable");
              }
	    }

	// The default arity is 0.
	if (res.getKind() == null)
	  setArity(res, 0);

	if (classType.isFinal())
	  res.setMinimal();
      }

    if(javaType instanceof ClassType)
      fetchMethods(res, (ClassType) javaType);
    
    return res;
  }

  private static void setArity(TypeConstructor tc, int arity)
  {
    try {
      Variance variance = arity == 0 ? 
	Variance.empty() : Variance.make(new int[arity]);
      mlsub.typing.lowlevel.Engine.setKind
	(tc, variance.getConstraint());
    }
    catch(mlsub.typing.lowlevel.Unsatisfiable ex) {
      User.error("Java class " + tc + " cannot have arity " + arity);
    }
  }

  public static void reset()
  {
    retyped = new HashMap();
  }

  /**
      Remembers native methods and fields explicitly bound with a new type.
      Also store implicit method and fields (automatically fetched from the
      bytecode), so that they can be discarded if an explicit retyping
      is found later.
  */
  private static Map retyped;
  
  static void registerNativeMethod(RetypedJavaMethod m, Method reflectMethod)
  {
    MethodDeclaration auto = (MethodDeclaration) retyped.put(reflectMethod, m);
    /*
      If auto is a RetypedJavaMethod, we are explicitely declaring two
      methods for the same native method. This is useful to give it several
      incomparable types.
      Otherwise, auto is an implicit java method, and we discard it
      in favor of the new explicit one.
    */
    if (auto != null && ! (auto instanceof RetypedJavaMethod))
      removeFromScope(auto);
  }

  static void registerNativeConstructor(RetypedJavaMethod m, 
                                        Method reflectMethod,
                                        TypeConstructor classe)
  {
    MethodDeclaration auto = (MethodDeclaration) retyped.put(reflectMethod, m);
    /*
      If auto is a RetypedJavaMethod, we are explicitely declaring two
      constructors for the same native constructor. 
      This is useful to give it several
      incomparable types.
      Otherwise, auto is an implicit java constructor, and we discard it
      in favor of the new explicit one.
    */
    if (auto != null && ! (auto instanceof RetypedJavaMethod))
      TypeConstructors.removeConstructor(classe, auto);
  }

  static void registerNativeField(JavaFieldAccess f, Field reflectField)
  {
    MethodDeclaration auto = (MethodDeclaration) retyped.put(reflectField, f);
    if (auto != null)
      removeFromScope(auto);
  }

  private static void removeFromScope(MethodDeclaration m)
  {
    Node.getGlobalScope().removeSymbol(m.getSymbol());
    bossa.link.Dispatch.unregister(m);
  }

  /** Utility function for analyse.nice */

  static List findJavaMethods
    (ClassType declaringClass, String funName)
  {
    return findJavaMethods(declaringClass, funName, -1);
  }

  /** If arity == -1, the arity does not matter, 
      and the method or field must be static.
   */
  static List findJavaMethods
    (ClassType declaringClass, String funName, int arity)
  {
    List possibilities = new LinkedList();
    declaringClass.addMethods();
    
    // search methods
    for(gnu.bytecode.Method method = declaringClass.getMethods();
	method!=null; method = method.getNext())
      if(method.getName().equals(funName) &&
	 (arity == -1 && method.getStaticFlag()
	  || (method.arg_types.length + (method.getStaticFlag() ? 0 : 1)) 
	      == arity))
	{
	  MethodDeclaration md = (JavaMethod) retyped.get(method);
	  if (md == null)
	    md = JavaMethod.make(method, false);
	  if (md != null)
	    possibilities.add(md.getSymbol());
	  else
	    if(Debug.javaTypes)
	      Debug.println("Method " + method + " ignored");
	}

    // search a field
    if (arity <= 0)
      {
	gnu.bytecode.Field field = declaringClass.getField(funName);
	if (field != null && field.getStaticFlag())
	  {
	    MethodDeclaration md = (JavaFieldAccess) retyped.get(field);
	    if (md == null)
	      md = JavaFieldAccess.make(field);
	    if (md != null)
	      possibilities.add(md.getSymbol());
	    else
	      if(Debug.javaTypes)
		Debug.println("Field " + field + " ignored");
	  }
      }

    return possibilities;
  }    

  /**
   * Loads the methods defined in the java class
   * to make them available to the nice code.
   */
  static void fetchMethods(TypeConstructor tc, ClassType classType)
  {
    try{
      classType.addMethods();

      for (Field f = classType.getFields(); f != null; f = f.getNext())
	if (retyped.get(f) == null)
	  addSymbol(f, JavaFieldAccess.make(f));

      for (Method m = classType.getMethods(); m != null; m = m.getNext())
        {
          // Ignore the method if it is explicitely retyped
          if (retyped.get(m) != null)
            continue;

          if (m.isConstructor())
            {
              JavaMethod res = JavaMethod.make(m, true);

              if (res != null)
                {
                  TypeConstructors.addConstructor(tc, res);
                  retyped.put(m, res);
                }
              else if(Debug.javaTypes)
                Debug.println("Constructor " + m + " ignored");
            }
          else
            {
              /* We don't need to put static methods in the global scope.
                 They can and must be accessed by specifying the class 
                 explicitely, like in Java. 
              */
              if (m.getStaticFlag())
                continue;

              Method base = baseMethod(classType, m);
              if (base != null)
                continue;

              if (Debug.javaTypes)
                Debug.println("Loaded native method " + m);
              addSymbol(m, JavaMethod.make(m, false));
            }
        }
    }
    catch(NoClassDefFoundError e){
      User.warning("Class " + e.getMessage().replace('/','.') + 
		   " was not found.\n" + 
		   "It is refered to in class " + classType.getName() +
		   "\nYou probably need to install the corresponding package.");
    }
    catch (ClassFormatError e) {
      User.warning("Class " + classType.getName() + 
		   " has an invalid bytecode format");
    }
  }

  private static Method baseMethod(ClassType classType, Method m)
  {
    Method res = null;

    /* Skips m if it was just overriden in classType
       but declared in a superclass or superinterface.
    */
    ClassType superClass = classType.getSuperclass();

    /* If the super class is null, the current class is an interface.
       We consider that interfaces do not declare new methods if they also
       belong to Object.
       For instance, java.lang.CharSequence in JDK 1.4 declares toString.
    */
    if (superClass == null)
      superClass = Type.pointer_type;

    res = alreadyHasMethod(superClass,m);
    if (res != null)
      return res;

    ClassType[] itfs = classType.getInterfaces();
    if (itfs != null)
      for (int i = 0; i < itfs.length; i++)
	{
	  res = alreadyHasMethod(itfs[i],m);
	  if (res != null)
	    return res;
	}

    return null;
  }

  private static void addSymbol(Object key, MethodDeclaration def)
  {
    if (def == null || Node.getGlobalScope() == null)
      return;

    Node.getGlobalScope().addSymbol(def.getSymbol());
    retyped.put(key, def);
  }

  private static Method alreadyHasMethod(ClassType c, Method m)
  {
    // not exact if the new method overload the old one 
    // with more precise types.
    return c.getMethod(m.getName(), m.getParameterTypes());
  }
  
  static boolean instantiable(Type javaType)
  {
    if(!(javaType instanceof ClassType))
      return false;

    return (((ClassType) javaType).getModifiers() 
	    & (gnu.bytecode.Access.ABSTRACT|
	       gnu.bytecode.Access.INTERFACE)) == 0;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  /****************************************************************
   * List of TCs for java.lang.Object: one per variance.
   ****************************************************************/

  private static final Vector objects = new Vector(5);
  
  static TypeConstructor object(int arity)
  {
    if(arity>=objects.size())
      objects.setSize(arity+1);
    
    TypeConstructor res = (TypeConstructor) objects.get(arity);
    if(res==null)
      {
	res = make(compilation, "java.lang.Object", gnu.bytecode.Type.pointer_type);
      }
    return res;
  }
  
  /** The current compilation. This is not thread safe! */
  static bossa.modules.Compilation compilation;

  /**
     Lookup a (possibly unqualified) class name.
  */
  public static TypeConstructor lookup(String className, Location loc)
  {
    Type classType = nice.tools.code.TypeImport.lookup(className, loc);
    if (classType == null)
      return null;

    if (compilation.javaTypeConstructors.containsKey(classType))
      return (TypeConstructor) compilation.javaTypeConstructors.get(classType);

    return create(compilation, classType.getName(), classType);
  }
}
