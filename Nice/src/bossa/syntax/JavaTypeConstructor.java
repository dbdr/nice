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
//$Modified: Sat Dec 04 11:23:30 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.engine.*;
import bossa.typing.Variance;

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
  static JavaTypeConstructor make(LocatedString name)
  {
    Object o=hash.get(name.toString());
    if(o!=null)
      return (JavaTypeConstructor) o;
    
    JavaTypeConstructor res=new JavaTypeConstructor
      (name,gnu.bytecode.ClassType.make(name.content));

    hash.put(name.toString(),res);
    return res;
  }

  static JavaTypeConstructor make(String name)
  {
    return make(new LocatedString(name,Location.nowhere()));
  }
  
  /**
   * Returns a java type constructor with the given name
   * and the given java type.
   * <p>
   * Usefull for primitive types.
   */
  static JavaTypeConstructor make(String name, gnu.bytecode.Type javaType)
  {
    Object o=hash.get(name);
    if(o!=null)
      return (JavaTypeConstructor) o;
    
    JavaTypeConstructor res=new JavaTypeConstructor(new LocatedString(name,Location.nowhere()),javaType);

    hash.put(name,res);
    return res;
  }

  private gnu.bytecode.Type javaType;
  
  private static HashMap hash = new HashMap();
  
  private JavaTypeConstructor(LocatedString className, gnu.bytecode.Type javaType)
  {
    super(className);
    setVariance(new Variance(0));
    bossa.typing.Typing.introduce(this);

    this.javaType = javaType;
    
    // Searching for java super class
    // This is recursive !
    if(javaType instanceof gnu.bytecode.ClassType)
      {
	// Forces computation of the reflectClass
	// This make getSuperClass work properly
	javaType.getReflectClass();

	gnu.bytecode.ClassType superClass = ((gnu.bytecode.ClassType) javaType).getSuperclass();
	if(superClass!=null)
	  {    	
	    JavaTypeConstructor superTC = make(superClass.getName(),superClass);

	    try{
	      bossa.typing.Typing.initialLeq(this,superTC);
	    }
	    catch(bossa.typing.TypingEx e){
	      Internal.error("Invalid java super-class "+superClass+" for "+this);
	    }
	  }
	
      }
  }

  public TypeSymbol cloneTypeSymbol()
  {
    return this;
  }
  
  Polytype getType()
  {
    return new Polytype(new MonotypeConstructor(this,null,name.location()));
  }
  boolean instantiable()
  {
    //TODO: abstract class ?
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
  
  static void addJavaTypes(TypeScope globalTypeScope)
  // Called by Node
  {
    // We need to remember it in order to add new java classes on the fly (see "lookup" below)
    JavaTypeConstructor.globalTypeScope = globalTypeScope;

    TypeConstructor voidTC = JavaTypeConstructor.make("void",gnu.bytecode.Type.void_type);
    voidType = new MonotypeConstructor(voidTC,null,Location.nowhere());

    globalTypeScope.addSymbol(voidTC);
  }

  public static Monotype voidType;
  
  private static TypeScope globalTypeScope;
  
  /****************************************************************
   * On the fly lookup of java types
   ****************************************************************/

  static JavaTypeConstructor lookup(LocatedString className)
  {
    Class c=null;
    String pkg = null;    
    
    try{ c=Class.forName(className.content); }
    catch(ClassNotFoundException e){ }

    if(c==null)
      for(Iterator i = globalTypeScope.module.listImplicitPackages();
	  i.hasNext();)
	{
	  pkg = (String) i.next();
	  
	  try{ c=Class.forName(pkg+"."+className.content); break; }
	  catch(ClassNotFoundException e){ }
	}
    
    if(c==null)
      return null;

    if(Debug.javaTypes)
      Debug.println("Registering java class "+className);
    
    JavaTypeConstructor res = JavaTypeConstructor.make(c.getName());
    ClassDefinition.addSpecialTypeConstructor(res);
    globalTypeScope.addSymbol(res);
    
    // Remembers the short name as an alias
    if(pkg!=null)
      globalTypeScope.addMapping(className,res);
    
    return res;
  }
}
