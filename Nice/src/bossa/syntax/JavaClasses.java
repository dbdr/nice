/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : JavaClasses.java
// Created : Thu Jul 08 11:51:09 1999 by bonniot
//$Modified: Wed Sep 20 18:12:28 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.util.Location;
import bossa.util.Debug;

import mlsub.typing.*;

import gnu.bytecode.*;

import java.util.*;

/**
 * A java class.
 */
public final class JavaClasses
{
  private JavaClasses(){}
  
  /**
   * Returns a java type constructor with the given name.
   *
   * If an existing JTC has the same name, returns it,
   * else creates a new one.
   */
  static TypeConstructor make(String name)
  {
    return make(name,
		gnu.bytecode.ClassType.make(name));
  }

  /**
   * Returns a java type constructor with the given name
   * and the given java type.
   * <p>
   * Usefull for primitive types.
   */
  static TypeConstructor make(String name, 
			      gnu.bytecode.Type javaType)
  {
    Object o = hash.get(name);
    if(o!=null)
      return (TypeConstructor) o;
    
    // we put the new JTC in the hashtable in the constructor
    // and not after it returned, to avoid infinite loop
    // if the code of the constructor does a lookup.
    return create(name,javaType);
  }

  /**
     @return null, except if javaName already had an
     associated TypeConstructor in which case it is returned.
   */
  static TypeConstructor setTypeConstructorForJavaClass(TypeConstructor tc, 
							String javaName)
  {
    Object old = hash.put(javaName, tc);
    return (TypeConstructor) old;
  }
  
  private gnu.bytecode.Type javaType;
  
  private static HashMap hash = new HashMap();

  private static final gnu.bytecode.Type[] blackListClass =
    new gnu.bytecode.Type[] {
      gnu.bytecode.ClassType.make("java.lang.Object")
      //,gnu.bytecode.ClassType.make("java.lang.Throwable")
    }
  ;
  
  private static final gnu.bytecode.Type[] blackListInterface =
    new gnu.bytecode.Type[] {
      gnu.bytecode.ClassType.make("java.io.Serializable"),
      gnu.bytecode.ClassType.make("java.lang.Cloneable"),
      gnu.bytecode.ClassType.make("java.lang.Comparable"),
      gnu.bytecode.ClassType.make("java.lang.Runnable")
    };
  
  private static boolean excluded(gnu.bytecode.Type[] blackList, 
			   gnu.bytecode.ClassType classType)
  {
    for(int i=0; i<blackList.length; i++)
      if(classType==blackList[i])
	return true;
    return false;
  }
      
  private static TypeConstructor create(String className, 
					gnu.bytecode.Type javaType)
  {
    if(Debug.javaTypes)
      Debug.println("Registering java class "+className);
    
    TypeConstructor res = new TypeConstructor(className, null, 
					      instantiable(javaType), true);
    hash.put(className, res);

    nice.tools.code.Types.set(res, javaType);
    
    if(bossa.modules.Package.contextFrozen())
      // We should not add new classes at this point.
      // The new TC should not implement top, as it would cause an error
      // to assert it. It doesn't matter, as this type is not used
      // explicitely.
      {
	res.setKind(Variance.empty().getConstraint());
	return res;
      }

    res.rememberToImplementTop();

    Typing.introduce(res);
    
    // Recursive searching for java super classes
    if(javaType instanceof gnu.bytecode.ClassType)
      {
	gnu.bytecode.ClassType superClass = 
	  ((gnu.bytecode.ClassType) javaType).getSuperclass();
	if(superClass!=null && !(excluded(blackListClass, superClass)))
	  {    	
	    TypeConstructor superTC = make(superClass.getName(), superClass);

	    try{
	      Typing.initialLeq(res, superTC);
	    }
	    catch(TypingEx e){
	      Internal.error("Invalid java super-class "+superClass+" for "+className);
	    }
	  }

	gnu.bytecode.ClassType[] itfs = ((gnu.bytecode.ClassType) javaType).getInterfaces();
	if(itfs!=null)
	  for(int i=0; i<itfs.length; i++)
	    if(!(excluded(blackListInterface,itfs[i])))
	    {
	      TypeConstructor superTC = make(itfs[i].getName(), itfs[i]);
	      
	      try{
		Typing.initialLeq(res, superTC);
	      }
	      catch(TypingEx e){
		Internal.error(res+" cannot implement "+
			       itfs[i],
			       ": "+e.toString());
	      }
	    }
      }
    
    if(javaType instanceof ClassType)
      fetchMethods(res, (ClassType) javaType);
    
    javaTypeConstructors.add(res);

    return res;
    
  }

  private static List javaTypeConstructors = new ArrayList(100);

  /**
   * Loads the methods defined in the java class
   * to make them available to the bossa code.
   */
  private static void fetchMethods(TypeConstructor tc, ClassType classType)
  {
    try{
      classType.addMethods();
    }
    catch(NoClassDefFoundError e){
      User.error("Class "+e.getMessage().replace('/','.')+
		 " was not found.\n"+
		 "You probably need to install the corresponding package.");
    }
    
  addingFetchedMethod:
    for(Method m = classType.getMethods(); m!=null; m = m.getNext())
      {
	if(m.isConstructor())
	  JavaMethod.addFetchedConstructor(m, tc);
	else
	  {  
	    // skips m if it was just overriden in classType
	    // but declared in a superclass or superinterface.
	    ClassType superClass = classType.getSuperclass();
	    if(superClass!=null
	       && alreadyHasMethod(superClass,m))
	      continue;
	    ClassType[] itfs = classType.getInterfaces();
	    if(itfs!=null)
	      for(int i=0; i<itfs.length; i++)
		if(alreadyHasMethod(itfs[i],m))
		  continue addingFetchedMethod;
	
	    JavaMethod.addFetchedMethod(m);
	  }
      }
  }

  private static boolean alreadyHasMethod(ClassType c, Method m)
  {
    // not exact if the new method overload the old one 
    // with more precise types.
    return c.getMethod(m.getName(), m.getParameterTypes())!=null;
  }
  
  public static void createContext()
  {
    for(Iterator i = javaTypeConstructors.iterator(); i.hasNext();)
      {
	TypeConstructor tc = (TypeConstructor) i.next();
	
	if(tc.getKind() == null)
	  try{
	    mlsub.typing.lowlevel.Engine.setKind
	      (tc, Variance.empty().getConstraint());
	  }
	  catch(mlsub.typing.lowlevel.Unsatisfiable e){
	    User.error("Java class " + tc + " is not well kinded");
	  }
      }
  }
  
  static boolean instantiable(Type javaType)
  {
    if(!(javaType instanceof ClassType))
      return false;

    return (((gnu.bytecode.ClassType)javaType).getModifiers() 
	    & (gnu.bytecode.Access.ABSTRACT|
	       gnu.bytecode.Access.INTERFACE)) == 0;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  /****************************************************************
   * List of TCs for java.lang.Object: one per variance.
   ****************************************************************/

  private final Vector objects = new Vector(5);
  
  TypeConstructor object(int arity)
  {
    if(arity>=objects.size())
      objects.setSize(arity+1);
    
    TypeConstructor res = (TypeConstructor) objects.get(arity);
    if(res==null)
      {
	res = make("java.lang.Object", gnu.bytecode.Type.pointer_type);
      }
    return res;
  }
  
  static TypeConstructor lookup(String className)
  {
    if(hash.containsKey(className))
      return (TypeConstructor) hash.get(className);
    
    Class c = nice.tools.code.Types.lookupJavaClass0(className);    
    
    if(c==null)
      {
	hash.put(className,null);
	return null;
      }
    
    return create(c.getName(), gnu.bytecode.Type.make(c));
  }
}
