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
//$Modified: Thu Feb 24 12:18:56 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.engine.*;
import bossa.typing.*;

import gnu.bytecode.Type;

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
    
    JavaTypeConstructor res=new JavaTypeConstructor(name,javaType);

    hash.put(name.content,res);
    return res;
  }

  static void registerTypeConstructorForJavaClass
    (TypeConstructor tc, String javaName)
  {
    Object old = hash.put(javaName, tc);
    if(old!=null)
      Internal.error(tc,"TC for "+javaName+" set twice");
  }
  
  private gnu.bytecode.Type javaType;
  private boolean isPrimitive;
  
  private static HashMap hash = new HashMap();

  private static final gnu.bytecode.Type[] blackListClass =
    new gnu.bytecode.Type[] {
      gnu.bytecode.Type.pointer_type,
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
      
  private JavaTypeConstructor(LocatedString className, gnu.bytecode.Type javaType)
  {
    super(className);
    
    //setVariance(Variance.make(0));
    bossa.typing.Typing.introduce(this);
    this.javaType = javaType;
    
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
    javaTypeConstructors.add(this);
  }

  private static List javaTypeConstructors = new ArrayList(100);

  public static void createContext()
  {
    for(Iterator i = javaTypeConstructors.iterator(); i.hasNext();)
      {
	JavaTypeConstructor tc = (JavaTypeConstructor) i.next();
	if(tc.getKind()==null)
	  try{
	    Engine.setKind(tc,Variance.make(0).getConstraint());
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
    try{
      //bossa.typing.initialLeq(this, object(this.variance.size));
      bossa.typing.Typing.assertImp
	(this,
	 InterfaceDefinition.top(this.variance.size),
	 true);
      if(isPrimitive)
	try{
	  if(this.variance.size!=0)
	    Internal.warning(this, "Primitive type "+name+
			     " with arity "+this.variance.size);
	  Typing.assertAbs(this, InterfaceDefinition.top(this.variance.size));
	}
	catch(TypingEx e){
	  Internal.warning
	    (this, "Could not make primitive type "+name+" final");
	}
    }
    catch(bossa.typing.TypingEx e){
      Internal.error("Impossible");
    }
  }
      
  Polytype getType()
  {
    return new Polytype(new MonotypeConstructor(this,null,name.location()));
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
   * Global Scope
   ****************************************************************/

  private static void makePrimType(String name, Type javaType)
  {
    JavaTypeConstructor jtc = (JavaTypeConstructor) JavaTypeConstructor.make
      (new LocatedString(name,new Location("Base type",true)),javaType);
    jtc.isPrimitive = true;
    
    globalTypeScope.addSymbol(jtc);
    globalTypeScope.addMapping("#"+name, jtc);
  }
      
  static void addJavaTypes(TypeScope globalTypeScope)
  // Called by Node
  {
    // We need to remember it in order to add new java classes on the fly (see "lookup" below)
    JavaTypeConstructor.globalTypeScope = globalTypeScope;

    TypeConstructor voidTC = JavaTypeConstructor.make
      (new LocatedString("void",new Location("Base type",true)),
       bossa.SpecialTypes.voidType);

    JavaTypeConstructor.voidType = 
      new MonotypeConstructor(voidTC,null, voidTC.name.location());
    globalTypeScope.addSymbol(voidTC);

    makePrimType("boolean",bossa.SpecialTypes.booleanType);
    makePrimType("char",bossa.SpecialTypes.charType);

    globalTypeScope.addMapping
      ("int", JavaTypeConstructor.lookup("gnu.math.IntNum"));
    globalTypeScope.addMapping
      ("long", JavaTypeConstructor.lookup("gnu.math.IntNum"));
  }

  public static Monotype voidType;
  
  private static TypeScope globalTypeScope;
  
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

  static java.lang.Class lookupJavaClass(String className)
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
      
    if(c!=null)
      return c;
    
    if(globalTypeScope.module!=null)
      for(Iterator i = globalTypeScope.module.listImplicitPackages();
	  i.hasNext();)
	{
	  String pkg = ((LocatedString) i.next()).toString();
	  
	  try{ c=Class.forName(pkg+"."+className); break; }
	  catch(ClassNotFoundException e){ }
	}
    
    return c;
  }

  static TypeConstructor lookup(String className)
  {
    Class c = lookupJavaClass(className);    
    
    if(c==null)
      return null;

    if(Debug.javaTypes)
      Debug.println("Registering java class "+c.getName());
    
    TypeConstructor res = JavaTypeConstructor.make
      (c.getName(),gnu.bytecode.Type.make(c));
    globalTypeScope.addSymbol(res);
    
    // Remembers the short name as an alias
    if(!(className.equals(c.getName())))
      {
	if(Debug.javaTypes)
	  Debug.println("Registering alias "+className);

	globalTypeScope.addMapping(className,res);
      }
    
    return res;
  }

  String bytecodeRepresentation()
  {
    return name.toString();
  }
}
