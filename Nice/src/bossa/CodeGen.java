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

// File    : CodeGen.java
// Created : Mon Jun 05 11:28:10 2000 by Daniel Bonniot
//$Modified: Tue Jul 25 20:46:59 2000 by Daniel Bonniot $

package bossa;

import bossa.util.*;
import mlsub.typing.*;
import bossa.syntax.ConstantExp;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

/**
 * Code generation tools.
 * 
 * @author Daniel Bonniot
 */

public final class CodeGen
{
  /****************************************************************
   * Mapping TCs to gnu.bytecode.Types
   ****************************************************************/

  private static final HashMap tcToGBType = new HashMap();
  
  public static void set(TypeConstructor tc, Type type)
  {
    tcToGBType.put(tc, type);
  }
  
  public static Type get(TypeConstructor tc)
  {
    return (Type) tcToGBType.get(tc);
  }

  public static Type javaType(TypeConstructor tc)
  {
    Type res = get(tc);
    if(res==null)
      return gnu.bytecode.Type.pointer_type;
    else
      return res;
  }
  
  /****************************************************************
   * Mapping monotypes to java types
   ****************************************************************/

  public static Type javaType(mlsub.typing.Monotype m)
  {
    if (!(m instanceof mlsub.typing.MonotypeConstructor))
      return gnu.bytecode.Type.pointer_type;
    
    MonotypeConstructor mc = (MonotypeConstructor) m;
    TypeConstructor tc = m.getTC();
    if(tc == ConstantExp.arrayTC)
      return SpecialTypes.makeArrayType(javaType(m.getTP()[0]));
    else
      return javaType(tc);
  }
  
  public static Type[] javaType(mlsub.typing.Monotype[] ms)
  {
    Type[] res = new Type[ms.length];
    for(int i=0; i<ms.length; i++)
      res[i] = javaType(ms[i]);
    return res;
  }

  public static Type javaType(mlsub.typing.Polytype t)
  {
    return javaType(t.getMonotype());
  }
  
  /****************************************************************
   * Converting a bytecode type (coming from reflection for instance)
   * into a bossa type.
   * Used for automatic declaration of java methods.
   ****************************************************************/
  
  public static Monotype getMonotype(Type javaType)
  throws ParametricClassException
  {
    if(javaType == Type.neverReturnsType)
      return ConstantExp.voidType;
    
    return getMonotype(javaType.getName());
  }
  
  /** 
      Thrown when one tries to get a monotype
      for a class defined to have type parameters.
      The rationale is that methods using that class
      should not be fetched, as the compiler cannot
      guess the correct type parameters.
  */
  public static class ParametricClassException extends Exception { }
  
  public static Monotype getMonotype(String name)
  throws ParametricClassException
  {
    if(name.endsWith("[]"))
      {
	name=name.substring(0,name.length()-2);
	return new MonotypeConstructor
	  (ConstantExp.arrayTC, 
	   new Monotype[]{ getMonotype(name) });
      }
    
    TypeSymbol ts = bossa.syntax.Node.getGlobalTypeScope().lookup(name);
    if(ts==null)
      Internal.error(name+" is not known");

    if(ts instanceof TypeConstructor)
      {
	TypeConstructor tc = (TypeConstructor) ts;

	if(tc.variance!=null && tc.arity()!=0)
	  throw new ParametricClassException();
	
	return new MonotypeConstructor(tc, null);
      }  
    // for primitive types, maybe temporary
    else if(ts instanceof Monotype)
      return (Monotype) ts;
    else
      {
	Internal.error("Bad java type: "+name+" ("+ts.getClass()+")");
	return null;
      }
  }
  
  /****************************************************************
   * Converting string to gnu.bytecode.Type
   ****************************************************************/

  public static final gnu.bytecode.Type type(String s)
  {
    if(s.length()==0)
      return null;
    
    if(s.charAt(0)=='[')
      {
	Type res = type(s.substring(1));
	if(res==null)
	  return null;
	else
	  return bossa.SpecialArray.create(res);
      }
    
    if(s.equals("void")) 	return bossa.SpecialTypes.voidType;
    if(s.equals("int"))  	return bossa.SpecialTypes.intType;
    if(s.equals("long")) 	return bossa.SpecialTypes.longType;
    if(s.equals("boolean")) 	return bossa.SpecialTypes.booleanType;
    
    Class clas = lookupJavaClass(s);
    if (clas == null)
      return null;
    return Type.make(clas);
  }
  
  /****************************************************************
   * On the fly lookup of java types
   ****************************************************************/

  /** Search className in opened packages too */
  public final static java.lang.Class lookupJavaClass(String className)
  {
    Class res = lookupJavaClass0(className);

    if(res==null)
      for(Iterator i=bossa.syntax.Node.getGlobalTypeScope().module.listImplicitPackages(); 
	  i.hasNext();)
	{
	  String pkg = ((bossa.syntax.LocatedString) i.next()).toString();

	  res = lookupJavaClass0(pkg+"."+className);
	  if(res!=null)
	    break;
	}
    return res;
  }
  
  private static final HashMap stringToReflectClass = new HashMap();
  
  /** Do not search in opened packages */
  public final static java.lang.Class lookupJavaClass0(String className)
  {
    if(stringToReflectClass.containsKey(className))
      return (Class) stringToReflectClass.get(className);
    
    Class c = null;

    try{ c=Class.forName(className); }
    catch(ClassNotFoundException e)
      // when the class does not exist
      { }
    catch(NoClassDefFoundError e) 
      // when a class with similar name but with different case exists
      // can occur in Windows
      { }
      
    stringToReflectClass.put(className, c);
    
    return c;
  }

  /****************************************************************
   * Bytecode representation
   ****************************************************************/

  public static String bytecodeRepresentation(TypeConstructor tc)
  {
    if(tc==null) return "_";
    return tc.toString();
  }

  /****************************************************************
   * Default values
   ****************************************************************/

  public static Expression defaultValue(Monotype m)
  {
    TypeConstructor tc = m.getTC();
    
    if(tc!=null)
      if(tc==ConstantExp.primInt ||
	 tc==ConstantExp.primByte ||
	 tc==ConstantExp.primChar ||
	 tc==ConstantExp.primShort ||
	 tc==ConstantExp.primLong)
	return zeroInt;
      else if(tc==ConstantExp.primFloat ||
	      tc==ConstantExp.primDouble)
	return zeroFloat;
    
    return gnu.expr.QuoteExp.nullExp;
  }

  private static gnu.expr.Expression zeroInt = 
    new gnu.expr.QuoteExp(new gnu.math.IntNum(0));
  private static gnu.expr.Expression zeroFloat = 
    new gnu.expr.QuoteExp(new gnu.math.DFloNum(0.0));
}
