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
//$Modified: Wed Aug 02 19:36:51 2000 by Daniel Bonniot $

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
    m = m.equivalent();

    if (m instanceof mlsub.typing.TupleType)
      return SpecialTypes.arrayType;
    
    if (!(m instanceof mlsub.typing.MonotypeConstructor))
      return gnu.bytecode.Type.pointer_type;
    
    MonotypeConstructor mc = (MonotypeConstructor) m;
    TypeConstructor tc = mc.getTC();
    if(tc == ConstantExp.arrayTC)
      return SpecialTypes.makeArrayType(javaType(mc.getTP()[0]));
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
    throws ParametricClassException, NotIntroducedClassException
  {
    if(javaType.isVoid())
      return ConstantExp.voidType;
    if(javaType == SpecialTypes.intType)
      return ConstantExp.intType;
    if(javaType == SpecialTypes.booleanType)
      return ConstantExp.boolType;
    if(javaType == SpecialTypes.charType)
      return ConstantExp.charType;
    if(javaType == SpecialTypes.byteType)
      return ConstantExp.byteType;    
    if(javaType == SpecialTypes.shortType)
      return ConstantExp.shortType;
    if(javaType == SpecialTypes.longType)
      return ConstantExp.longType;
    if(javaType == SpecialTypes.floatType)
      return ConstantExp.floatType;
    if(javaType == SpecialTypes.doubleType)
      return ConstantExp.doubleType;

    if (javaType instanceof ArrayType)
      return new mlsub.typing.MonotypeConstructor
	(ConstantExp.arrayTC, 
	 new mlsub.typing.Monotype[]
	  {getMonotype(((ArrayType) javaType).getComponentType())});
    
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
  
  /**
     Thrown when the type would contain elements that
     are not valid for typechecking.
     This happens for native classes that are discovered
     after the context rigidification (due to ClassExp expressions).
  */
  public static class NotIntroducedClassException extends Exception 
  {
    /** The invalid type element. */
    public TypeSymbol symbol;
    
    NotIntroducedClassException(TypeSymbol symbol)
    {
      this.symbol = symbol;
    }
  }

  public static Monotype getMonotype(String name)
  throws ParametricClassException, NotIntroducedClassException
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

	if (tc.variance != null && tc.arity() != 0)
	  throw new ParametricClassException();

	if (tc.getId() == -1)
	  throw new NotIntroducedClassException(tc);
	
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
    m = m.equivalent();
    
    TypeConstructor tc = null;

    if(m instanceof MonotypeConstructor)
      tc = ((MonotypeConstructor) m).getTC();
    
    if (tc != null)
      if(tc == ConstantExp.primInt ||
	 tc == ConstantExp.primByte ||
	 tc == ConstantExp.primChar ||
	 tc == ConstantExp.primShort ||
	 tc == ConstantExp.primLong)
	return zeroInt;
      else if(tc == ConstantExp.primFloat ||
	      tc == ConstantExp.primDouble)
	return zeroFloat;
    
    return QuoteExp.nullExp;
  }

  private static gnu.expr.Expression zeroInt = 
    new QuoteExp(new gnu.math.IntNum(0));
  private static gnu.expr.Expression zeroFloat = 
    new QuoteExp(new gnu.math.DFloNum(0.0));
}
