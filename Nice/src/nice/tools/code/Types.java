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

package nice.tools.code;

import bossa.util.*;
import mlsub.typing.*;
import bossa.syntax.ConstantExp;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;
import java.io.File;
import java.net.URL;

/**
   Conversion between Nice and bytecode types.
   
   @version $Date$
   @author Daniel Bonniot
 */

public final class Types
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
    if (res == null)
      return gnu.bytecode.Type.pointer_type;
    else
      return res;
  }
  
  /**
     Prepare <tt>m</tt> to be given a precise bytecode type if possible.
     
     Be careful: 
     Type components of <tt>m</tt> must be valid.
     That is, englobing constraints must be asserted.
  */
  public static void setBytecodeType(Monotype m)
  {
    m = m.equivalent();
    
    if (m instanceof TupleType)
      setBytecodeType(((TupleType) m).getComponents());
    
    if (!(m instanceof MonotypeConstructor))
      return;

    MonotypeConstructor mc = (MonotypeConstructor) m;
    
    TypeConstructor tc = mc.getTC();

    TypeConstructor rigidTC;
    if (tc == ConstantExp.arrayTC)
      rigidTC = tc;
    else
      {
	if(Types.get(tc) != null)
	  // OK, nothing to do
	  return;
	
	rigidTC = Typing.lowestRigidSuperTC(tc);

	if (rigidTC == null)
	  // We don't know
	  return;
      }
    
    // Only for arrays, we are interested in the components too
    if (rigidTC == ConstantExp.arrayTC)
      {
	Monotype param = mc.getTP()[0];

	setBytecodeType(param);
	if (tc != ConstantExp.arrayTC)
	  Types.set(tc, SpecialTypes.makeArray(javaType(param)));
      }
    else
      Types.set(tc, Types.get(rigidTC));
  }

  /**
     Iter setBytecodeType.
  */
  public static void setBytecodeType(Monotype[] ms)
  {
    for (int i = 0; i<ms.length; i++)
      setBytecodeType(ms[i]);
  }
  
  /****************************************************************
   * Mapping monotypes to java types
   ****************************************************************/

  /**
     Return the bytecode type used to represent objects of this type.
     
     You might want use the {@link #setBytecodeType(mlsub.typing.Monotype)}
     method before to make result more precise.
   */
  public static Type javaType(Monotype m)
  {
    m = m.equivalent();

    if (m instanceof TupleType)
      // not SpecialArray
      return ArrayType.make(componentType((TupleType) m));
    
    if (!(m instanceof MonotypeConstructor))
      return gnu.bytecode.Type.pointer_type;
    
    MonotypeConstructor mc = (MonotypeConstructor) m;
    TypeConstructor tc = mc.getTC();
    if(tc == ConstantExp.arrayTC)
      return SpecialTypes.makeArray(javaType(mc.getTP()[0]));
    else
      return javaType(tc);
  }
  
  public static Type[] javaType(Monotype[] ms)
  {
    Type[] res = new Type[ms.length];
    for(int i=0; i<ms.length; i++)
      res[i] = javaType(ms[i]);
    return res;
  }

  public static Type javaType(Polytype t)
  {
    return javaType(t.getMonotype());
  }

  /** Returns the common bytecode type used for elements of this tuple. */
  public static Type componentType(TupleType t)
  {
    return lowestCommonSuperType(javaType(t.getComponents()));
  }
  
  private static Type lowestCommonSuperType(Type[] types)
  {
    Type res = types[0];
    for (int i = 1; res!=null && i<types.length; i++)
      res = Type.lowestCommonSuperType(res, types[i]);
    
    if (res == null)
      return Type.pointer_type;
    else
      return res;
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
      return new MonotypeConstructor
	(ConstantExp.arrayTC, 
	 new Monotype[]
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
      Internal.error(name + " is not known");

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
          //return SpecialArray.create(res);
	  return ArrayType.make(res);
      }
    
    if(s.equals("void")) 	return SpecialTypes.voidType;
    if(s.equals("boolean")) 	return SpecialTypes.booleanType;
    if(s.equals("byte")) 	return SpecialTypes.byteType;
    if(s.equals("short")) 	return SpecialTypes.shortType;
    if(s.equals("int")) 	return SpecialTypes.intType;
    if(s.equals("long")) 	return SpecialTypes.longType;
    if(s.equals("char")) 	return SpecialTypes.charType;
    if(s.equals("float")) 	return SpecialTypes.floatType;
    if(s.equals("double")) 	return SpecialTypes.doubleType;
    
    Class clas = lookupJavaClass(s);
    if (clas == null)
      return null;
    return Type.make(clas);
  }
  
  /****************************************************************
   * Manipulating bytecode types
   ****************************************************************/

  public static ObjectType equivalentObjectType(Type t)
  {
    if (t instanceof ObjectType)
      return (ObjectType) t;
    
    if (t == Type.boolean_type) return Type.boolean_ctype;
    else if (t == Type.double_type) return PrimType.double_ctype;
    else if (t == Type.float_type) return PrimType.float_ctype;
    else if (t == Type.long_type) return PrimType.long_ctype;
    else if (t == Type.int_type) return PrimType.int_ctype;
    else if (t == Type.short_type) return Type.short_ctype;
    else if (t == Type.byte_type) return Type.byte_ctype;
    else if (t == Type.char_type) return PrimType.char_ctype;
    
    bossa.util.Internal.error
      ("Equivalent type for " + t + " is not defined yet");
    return null;
  }
  
  /****************************************************************
   * On the fly lookup of java types
   ****************************************************************/

  /** Search className in opened packages too */
  public final static java.lang.Class lookupJavaClass(String className)
  {
    Class res = lookupQualifiedJavaClass(className);

    if (res != null)
      return res;
    
    String[] pkgs = bossa.syntax.Node.getGlobalTypeScope().module.listImplicitPackages();
    for (int i = 0; i < pkgs.length; i++)
	{
	  res = lookupQualifiedJavaClass(pkgs[i] + "." + className);
	  if(res != null)
	    return res;
	}
    return null;
  }
  
  private static final HashMap stringToReflectClass = new HashMap();
  
  /** 
      Searches a native class given by its fully qualified name
      in the user classpath.
      
      This is to be prefered to Class.forName, which searches 
      in compiler's runtime classpath.
      
      This method does not search in opened packages.
      It uses a hash-table, to speed up multiple lookups on the same name.

      @return the java.lang.Class object corresponding to the class name,
      or null if the class does not exists or is ill-formed.
  */
  public final static Class lookupQualifiedJavaClass(String className)
  {
    if (stringToReflectClass.containsKey(className))
      return (Class) stringToReflectClass.get(className);

    Class c = null;

    if (classLoader == null)
      setClasspath(null);
    
    try{ c = classLoader.loadClass(className); }
    catch(ClassNotFoundException e)
      // when the class does not exist
      { }
    catch(NoClassDefFoundError e) 
      // when a class with similar name but with different case exists
      // can occur on case-insensitive file-systems (e.g. FAT)
      { }
      
    stringToReflectClass.put(className, c);
    
    return c;
  }

  private static ClassLoader classLoader;

  static 
  {
    setClasspath(null);
  }
  
  public static void setClasspath(String classpath)
  {
    if (classpath == null)
      {
	classLoader = ClassLoader.getSystemClassLoader();
	return;
      }

    LinkedList components = new LinkedList();
    
    int start = 0;
    // skip starting separators
    while (start<classpath.length() && 
	   classpath.charAt(start) == File.pathSeparatorChar)
      start++;
    
    while(start<classpath.length())
      {
	int end = classpath.indexOf(File.pathSeparatorChar, start);
	if (end == -1)
	  end = classpath.length();
	    
	String pathComponent = classpath.substring(start, end);
	if (pathComponent.length() > 0)
	  try{
	    File f = nice.tools.util.System.getFile(pathComponent);
	    if (f.canRead())
	      components.add(f.toURL());
	    else
	      {
		if (!f.exists())
		  User.warning("Classpath component " + pathComponent + " does not exist");
		else
		  User.warning("Classpath component " + pathComponent + " is not readable");
	      }
	  }
	  catch(java.net.MalformedURLException e){
	    User.warning("Classpath component " + pathComponent + " is invalid");
	  }
	start = end+1;
      }

    classLoader = new java.net.URLClassLoader
      ((URL[]) components.toArray(new URL[components.size()]), 
       /* no parent */ null);
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
      {
	if(tc == ConstantExp.primInt ||
	   tc == ConstantExp.primByte ||
	   tc == ConstantExp.primShort ||
	   tc == ConstantExp.primLong)
	  return zeroInt;
	if(tc == ConstantExp.primFloat ||
	   tc == ConstantExp.primDouble)
	  return zeroFloat;
	if(tc == ConstantExp.primBool)
	  return QuoteExp.falseExp;
	if(tc == ConstantExp.primChar)
	  return zeroChar;
      }
    
    return QuoteExp.nullExp;
  }

  private static Expression zeroInt = new QuoteExp(new Integer(0));
  private static Expression zeroFloat = new QuoteExp(new Float(0.0));
  private static Expression zeroChar = new QuoteExp(new Character((char) 0));
}
