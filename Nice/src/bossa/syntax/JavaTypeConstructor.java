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

// File    : JavaTypeConstructor.java
// Created : Thu Jul 08 11:51:09 1999 by bonniot
//$Modified: Tue Apr 04 18:37:32 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.engine.*;
import bossa.typing.*;

import gnu.bytecode.*;

import bossa.util.Location;

import java.util.*;

/**
 * A java class.
 */
public class JavaTypeConstructor extends TypeConstructor
{
  /**
   * Returns a java type constructor with the given name.
   *
   * If an existing JTC has the same name, returns it,
   * else creates a new one.
   */
  static TypeConstructor make(LocatedString name)
  {
    return make(name,
		gnu.bytecode.ClassType.make(name.content));
  }

  static TypeConstructor make(String name)
  {
    return make(new LocatedString(name,Location.nowhere()));
  }
  
  static TypeConstructor make(String name, gnu.bytecode.Type javaType)
  {
    return make(new LocatedString(name,Location.nowhere()),javaType);
  }
  
  /**
   * Returns a java type constructor with the given name
   * and the given java type.
   * <p>
   * Usefull for primitive types.
   */
  private static TypeConstructor make(LocatedString name, 
				      gnu.bytecode.Type javaType)
  {
    Object o=hash.get(name.content);
    if(o!=null)
      return (TypeConstructor) o;
    
    // we put the new JTC in the hashtable in the constructor
    // and not after it returned, to avoid infinite loop
    // if the code of the constructor does a lookup.
    return new JavaTypeConstructor(name,javaType);
  }

  static void registerTypeConstructorForJavaClass
    (TypeConstructor tc, String javaName)
  {
    Object old = hash.put(javaName, tc);
    if(old!=null)
      Internal.error(tc,"TC for "+javaName+" set twice");
  }
  
  private gnu.bytecode.Type javaType;
  
  private static HashMap hash = new HashMap();

  private static final gnu.bytecode.Type[] blackListClass =
    new gnu.bytecode.Type[] {
      gnu.bytecode.ClassType.make("java.lang.Object"),
      gnu.bytecode.ClassType.make("java.lang.Throwable")
    }
  ;
  
  private static final gnu.bytecode.Type[] blackListInterface =
    new gnu.bytecode.Type[] {
      gnu.bytecode.ClassType.make("java.io.Serializable"),
      gnu.bytecode.ClassType.make("java.lang.Cloneable"),
      gnu.bytecode.ClassType.make("java.lang.Comparable"),
      gnu.bytecode.ClassType.make("java.lang.Runnable")
    };
  
  private boolean excluded(gnu.bytecode.Type[] blackList, 
			   gnu.bytecode.ClassType classType)
  {
    for(int i=0; i<blackList.length; i++)
      if(classType==blackList[i])
	return true;
    return false;
  }
      
  private JavaTypeConstructor(LocatedString className, 
			      gnu.bytecode.Type javaType)
  {
    super(className);
    hash.put(className.content, this);
    this.javaType = javaType;

    if(Debug.javaTypes)
      Debug.println("Registering java class "+className);
    
    if(bossa.modules.Package.contextFrozen())
      // we should not add new classes at this point    
      return;

    //setVariance(Variance.make(0));
    bossa.typing.Typing.introduce(this);
    
    // Recursive searching for java super classes
    if(javaType instanceof gnu.bytecode.ClassType)
      {
	gnu.bytecode.ClassType superClass = 
	  ((gnu.bytecode.ClassType) javaType).getSuperclass();
	if(superClass!=null && !(excluded(blackListClass, superClass)))
	  {    	
	    TypeConstructor superTC = make(superClass.getName(),superClass);

	    try{
	      bossa.typing.Typing.initialLeq(this,superTC);
	    }
	    catch(bossa.typing.TypingEx e){
	      Internal.error("Invalid java super-class "+superClass+" for "+this);
	    }
	  }

	gnu.bytecode.ClassType[] itfs = ((gnu.bytecode.ClassType) javaType).getInterfaces();
	if(itfs!=null)
	  for(int i=0; i<itfs.length; i++)
	    if(!(excluded(blackListInterface,itfs[i])))
	    {    	
	      TypeConstructor superTC = make(itfs[i].getName(),itfs[i]);
	      
	      try{
		bossa.typing.Typing.initialLeq(this,superTC);
	      }
	      catch(bossa.typing.TypingEx e){
		Internal.error(this,
			       this+" cannot implement "+
			       itfs[i],
			       ": "+e.toString());
	      }
	    }
      }
    Node.getGlobalTypeScope().addSymbol(this);
    fetchMethods();
    javaTypeConstructors.add(this);
  }

  private static List javaTypeConstructors = new ArrayList(100);

  /**
   * Loads the methods defined in the java class
   * to make them available to the bossa code.
   */
  private void fetchMethods()
  {
    if(!(javaType instanceof ClassType))
      return;
    ClassType classType = (ClassType) javaType;
    try{
      classType.addMethods();
    }
    catch(NoClassDefFoundError e){
      User.error(this,
		 "Class "+e.getMessage().replace('/','.')+
		 " was not found.\n"+
		 "You probably need to install the corresponding package.");
    }
    
  addFetchedMethod:
    for(Method m = classType.getMethods(); m!=null; m = m.getNext())
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
	      continue addFetchedMethod;
	
	JavaMethodDefinition.addFetchedMethod(m);
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
	JavaTypeConstructor tc = (JavaTypeConstructor) i.next();
	if(tc.getKind()==null)
	  try{
	    Engine.setKind(tc, Variance.make(0).getConstraint());
	  }
	  catch(Unsatisfiable e){
	    User.error(tc,
		       "Java class "+tc+" is not well kinded");
	  }
      }
  }
    
  public TypeSymbol cloneTypeSymbol()
  {
    return this;
  }
  
  public void setKind(Kind k)
  {
    super.setKind(k);
    if(this.variance == null)
      Internal.warning(this,
		       this+" was not given an arity");
    else
      try{
	//bossa.typing.initialLeq(this, object(this.variance.size));
	
	bossa.typing.Typing.assertImp
	  (this,
	   InterfaceDefinition.top(this.variance.size),
	   true);
      }
      catch(bossa.typing.TypingEx e){
	Internal.error("Impossible");
      }

  }
      
  Polytype getType()
  {
    return new Polytype(getMonotype());
  }

  Monotype getMonotype()
  {
    return new MonotypeConstructor(this,null,name.location());
  }
  
  boolean instantiable()
  {
    if(!(javaType instanceof gnu.bytecode.ClassType))
      return false;

    return (((gnu.bytecode.ClassType)javaType).getModifiers() 
	    & (gnu.bytecode.Access.ABSTRACT|
	       gnu.bytecode.Access.INTERFACE)) == 0;
  }
  
  boolean constant()
  {
    return true;
  }
  
  public boolean isConcrete()
  {
    //TODO: abstract class ?
    return true;
  }
  
  gnu.bytecode.Type getJavaType()
  {
    return this.javaType;
  }
  
  public ListIterator getJavaInstanceTypes()
  {
    List res = new LinkedList();
    res.add(getJavaType());
    
    return res.listIterator();
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  TypeConstructor resolve(TypeScope typeScope)
  {
    return this;
  }

  TypeSymbol resolveToTypeSymbol(TypeScope typeScope)
  {
    return this;
  }

  /****************************************************************
   * Printing
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
  
  /****************************************************************
   * On the fly lookup of java types
   ****************************************************************/

  /** Search className in opened packages too */
  static java.lang.Class lookupJavaClass(String className)
  {
    Class res = internLookupJavaClass(className);

    if(res==null)
      for(Iterator i=Node.getGlobalTypeScope().module.listImplicitPackages(); 
	  i.hasNext();)
	{
	  String pkg = ((LocatedString) i.next()).toString();

	  res = internLookupJavaClass(pkg+"."+className);
	  if(res!=null)
	    break;
	}
    return res;
  }
  
  /** Do not search in opened packages */
  private static java.lang.Class internLookupJavaClass(String className)
  {
    Class c = null;

    try{ c=Class.forName(className); }
    catch(ClassNotFoundException e)
      // when the class does not exist
      { }
    catch(NoClassDefFoundError e) 
      // when a class with similar name but with different case exists
      // can occur in Windows
      { }
      
    return c;
  }

  static TypeConstructor lookup(String className)
  {
    if(hash.containsKey(className))
      return (TypeConstructor) hash.get(className);
    
    Class c = internLookupJavaClass(className);    
    
    if(c==null)
      {
	hash.put(className,null);
	return null;
      }
    
    return new JavaTypeConstructor
      (new LocatedString(c.getName(), Location.nowhere()),
       gnu.bytecode.Type.make(c));
  }

  String bytecodeRepresentation()
  {
    return name.toString();
  }
}
