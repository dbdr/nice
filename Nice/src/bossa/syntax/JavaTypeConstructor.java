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
//$Modified: Mon Nov 15 20:03:23 1999 by bonniot $

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
    Object o=hash.get(name);
    if(o!=null)
      return (JavaTypeConstructor) o;
    
    JavaTypeConstructor res=new JavaTypeConstructor(name);
    res.javaType=gnu.bytecode.ClassType.make(name.content);

    hash.put(name,res);
    return res;
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
    
    JavaTypeConstructor res=new JavaTypeConstructor(new LocatedString(name,Location.nowhere()));
    res.javaType=javaType;

    hash.put(name,res);
    return res;
  }

  private gnu.bytecode.Type javaType;
  
  private static HashMap hash = new HashMap();
  
  private JavaTypeConstructor(LocatedString className)
  {
    super(className);
    setVariance(new Variance(0));
    bossa.typing.Typing.introduce(this);
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
    // We need to remeber it in order to add new java classes on the fly (see "lookup" below)
    JavaTypeConstructor.globalTypeScope=globalTypeScope;
    
    globalTypeScope.addSymbol(JavaTypeConstructor.make("void",gnu.bytecode.Type.void_type));
  }

  private static TypeScope globalTypeScope;
  
  /****************************************************************
   * On the fly lookup of java types
   ****************************************************************/

  static JavaTypeConstructor lookup(LocatedString className)
  {
    Class c=null;
    try{
      c=Class.forName(className.content);
    }
    catch(ClassNotFoundException e){
      return null;
    }

    if(Debug.javaTypes)
      Debug.println("Registering java class "+className);
    
    JavaTypeConstructor res = JavaTypeConstructor.make(className);
    ClassDefinition.addSpeciaTypeConstructor(res);
    globalTypeScope.addSymbol(res);
    
    return res;
  }
}
