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
//$Modified: Wed Jan 26 18:53:25 2000 by Daniel Bonniot $

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
    return make(name,
		gnu.bytecode.ClassType.make(name.content));
  }

  static JavaTypeConstructor make(String name)
  {
    return make(new LocatedString(name,Location.nowhere()));
  }
  
  static JavaTypeConstructor make(String name, gnu.bytecode.Type javaType)
  {
    return make(new LocatedString(name,Location.nowhere()),javaType);
  }
  
  /**
   * Returns a java type constructor with the given name
   * and the given java type.
   * <p>
   * Usefull for primitive types.
   */
  static JavaTypeConstructor make(LocatedString name, 
				  gnu.bytecode.Type javaType)
  {
    Object o=hash.get(name.content);
    if(o!=null)
      return (JavaTypeConstructor) o;
    
    JavaTypeConstructor res=new JavaTypeConstructor(name,javaType);

    hash.put(name.content,res);
    return res;
  }

  private gnu.bytecode.Type javaType;
  
  private static HashMap hash = new HashMap();
  
  private JavaTypeConstructor(LocatedString className, gnu.bytecode.Type javaType)
  {
    super(className);
    
    setVariance(Variance.make(0));
    bossa.typing.Typing.introduce(this);
    try{
      bossa.typing.Typing.assertImp(this,InterfaceDefinition.top(0),true);
    }
    catch(bossa.typing.TypingEx e){
      Internal.error("Impossible");
    }
    
    this.javaType = javaType;
    
    // Searching for java super class
    // This is recursive !
    if(javaType instanceof gnu.bytecode.ClassType)
      {
	//Class ref = javaType.getReflectClass();

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

	gnu.bytecode.ClassType[] itfs = ((gnu.bytecode.ClassType) javaType).getInterfaces();
	if(itfs!=null)
	  for(int i=0; i<itfs.length; i++)
	    {    	
	      JavaTypeConstructor superTC = make(itfs[i].getName(),itfs[i]);
	      
	      try{
		bossa.typing.Typing.initialLeq(this,superTC);
	      }
	      catch(bossa.typing.TypingEx e){
		Internal.error("Invalid java implemented interface "+itfs[i]+" for "+this);
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

    TypeConstructor voidTC = JavaTypeConstructor.make
      (new LocatedString("void",new Location("Base type",true)),
       bossa.SpecialTypes.voidType);

    JavaTypeConstructor.voidType = 
      new MonotypeConstructor(voidTC,null, voidTC.name.location());

    globalTypeScope.addSymbol(voidTC);
    globalTypeScope.addSymbol
      (JavaTypeConstructor.make
       (new LocatedString("boolean",new Location("Base type",true)),
	bossa.SpecialTypes.booleanType));

    globalTypeScope.addMapping
      ("int", JavaTypeConstructor.lookup("gnu.math.IntNum"));
  }

  public static Monotype voidType;
  
  private static TypeScope globalTypeScope;
  
  /****************************************************************
   * On the fly lookup of java types
   ****************************************************************/

  static java.lang.Class lookupJavaClass(String className)
  {
    Class c = null;
    String pkg = null;    
    
    try{ c=Class.forName(className); }
    catch(ClassNotFoundException e){ }

    if(c!=null)
      return c;
    
    if(globalTypeScope.module!=null)
      for(Iterator i = globalTypeScope.module.listImplicitPackages();
	  i.hasNext();)
	{
	  pkg = (String) i.next();
	  
	  try{ c=Class.forName(pkg+"."+className); break; }
	  catch(ClassNotFoundException e){ }
	}
    
    return c;
  }

  static JavaTypeConstructor lookup(String className)
  {
    Class c = lookupJavaClass(className);    
    
    if(c==null)
      return null;

    if(Debug.javaTypes)
      Debug.println("Registering java class "+c.getName());
    
    JavaTypeConstructor res = JavaTypeConstructor.make(c.getName(),gnu.bytecode.Type.make(c));
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
}
