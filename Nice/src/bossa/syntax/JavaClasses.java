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
import bossa.util.Location;
import bossa.util.Debug;

import mlsub.typing.*;

import gnu.bytecode.*;

import java.util.*;

/**
   Methods to deal with existing java classes and methods.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
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
    return make(name, ClassType.make(name));
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
    return create(name, javaType);
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
      ClassType.make("java.lang.Object")
      //,ClassType.make("java.lang.Throwable")
    }
  ;
  
  private static final gnu.bytecode.Type[] blackListInterface =
    new gnu.bytecode.Type[] {
      ClassType.make("java.io.Serializable"),
      ClassType.make("java.lang.Cloneable"),
      ClassType.make("java.lang.Comparable"),
      ClassType.make("java.lang.Runnable")
    };
  
  private static boolean excluded(gnu.bytecode.Type[] blackList, 
				  ClassType classType)
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
    
    // as the tc is not introduced, it should not be set instantiable
    // (thus used at link test with an error)
    // these late TCs should be avoided
    boolean instantiable = 
      !Typing.isInRigidContext() && instantiable(javaType);
      
    TypeConstructor res = new TypeConstructor(className, null, 
					      instantiable, true);
    hash.put(className, res);

    nice.tools.code.Types.set(res, javaType);
    
    if(Typing.isInRigidContext())
      // We should not add new classes at this point.
      // It doesn't matter, as this type is not used explicitely.
      {
	Internal.warning(className + " added late");
	
	res.setKind(Variance.empty().getConstraint());
	return res;
      }

    Typing.introduce(res);
    
    // Recursive search for java super-classes
    if(javaType instanceof ClassType)
      {
	ClassType superClass =  ((ClassType) javaType).getSuperclass();
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

	ClassType[] itfs = null;
	try{
	  itfs = ((ClassType) javaType).getInterfaces();
	}
	catch(NoClassDefFoundError ex){
	  User.warning("Interface " + ex.getMessage() + 
		       " implemented by " + javaType.getName() + 
		       " was not found in classpath");
	}
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
   * to make them available to the nice code.
   */
  private static void fetchMethods(TypeConstructor tc, ClassType classType)
  {
    try{
      classType.addMethods();
    }
    catch(NoClassDefFoundError e){
      User.warning("Class " + e.getMessage().replace('/','.') + 
		   " was not found.\n" + 
		   "It is refered to in class " + classType.getName() +
		   "\nYou probably need to install the corresponding package.");
    }
    
    for (Field f = classType.getFields(); f != null; f = f.getNext())
      JavaMethod.addFetchedMethod(f);

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
  
  /**
     Assume that native classes have a 0 arity
     unless it was asserted otherwise.
  */
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
    
    Class c = nice.tools.code.Types.lookupQualifiedJavaClass(className);    
    
    if(c==null)
      {
	hash.put(className,null);
	return null;
      }
    
    return create(c.getName(), gnu.bytecode.Type.make(c));
  }
}
