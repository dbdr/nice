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

  private static HashMap tcToGBType;
  
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
    m = equivalent(m);
    
    if (m instanceof TupleType)
      setBytecodeType(((TupleType) m).getComponents());

    TypeConstructor tc = m.head();
    if (tc == null)
      return;

    TypeConstructor rigidTC;
    if (tc == ConstantExp.arrayTC)
      rigidTC = tc;
    else
      {
	if(Types.get(tc) != null)
	  // OK, nothing to do
	  return;
	
	// We try to approximate a polymorphic type by a monomorphic one.
	// So any instance will do, but a lower one is better.
	rigidTC = Typing.lowestInstance(tc);

	if (rigidTC == null)
	  // We don't know
	  return;
      }
    
    // Only for arrays, we are interested in the components too
    if (rigidTC == ConstantExp.arrayTC)
      {
	Monotype param = ((MonotypeConstructor) m).getTP()[0];

	setBytecodeType(param);
	if (tc != ConstantExp.arrayTC)
	  Types.set(tc, SpecialTypes.array(javaType(param)));
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

  public static void setBytecodeType(Polytype t)
  {
    Constraint cst = t.getConstraint();
    if (Constraint.hasBinders(cst))
      {
	Typing.enter();
	try{
	  Constraint.assert(cst);
	  setBytecodeType(t.getMonotype());
	}
	catch(TypingEx e) {}
	finally{
	  try{
	    Typing.leave();
	  }
	  catch(TypingEx e) {}
	}
      }
    else
      setBytecodeType(t.getMonotype());
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
    boolean maybe = isMaybe(m.equivalent());
    Type res = rawJavaType(equivalent(m));

    if (maybe) 
      return equivalentObjectType(res);
    else
      return res;
  }

  private static Type rawJavaType(Monotype m)
  {
    if (m instanceof TupleType)
      // not SpecialArray
      return ArrayType.make(componentType((TupleType) m));
    
    if (m instanceof FunType)
      return gnu.expr.Compilation.typeProcedure;

    if (!(m instanceof MonotypeConstructor))
      return gnu.bytecode.Type.pointer_type;
    
    MonotypeConstructor mc = (MonotypeConstructor) m;
    TypeConstructor tc = mc.getTC();

    if (tc == ConstantExp.arrayTC)
      return SpecialTypes.array(javaType(mc.getTP()[0]));
    else
      return javaType(tc);
  }
  
  public static Type[] javaType(Monotype[] ms)
  {
    if (ms == null)
      return null;

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
    return lowestCommonSupertype(t.getComponents());
 }
  
  /**
     @return the most precise bytecode type able to store values of the given
       Nice types
  */
  public static Type lowestCommonSupertype(Monotype[] types)
  {
    Type res = javaType(types[0]);
    for (int i = 1; res!=null && i<types.length; i++)
      res = Type.lowestCommonSuperType(res, javaType(types[i]));
    
    if (res == null)
      return Type.pointer_type;
    else
      return res;
  }
  
  /****************************************************************
   * Converting a bytecode type (coming from reflection for instance)
   * into a Nice type.
   * Used for automatic declaration of java methods.
   ****************************************************************/
  
  public static Monotype monotype(Type javaType, boolean sure)
    throws ParametricClassException, NotIntroducedClassException
  {
    Monotype res = getMonotype(javaType);
    if (sure)
      return bossa.syntax.Monotype.sure(res);
    else
      return bossa.syntax.Monotype.maybe(res);
  }

  public static Monotype monotype(Type javaType)
    throws ParametricClassException, NotIntroducedClassException
  {
    Monotype res = getMonotype(javaType);
    if (javaType instanceof ObjectType)
      return bossa.syntax.Monotype.maybe(res);
    else
      // the sure is already there in getMonotype
      return res;
  }

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
	 new Monotype[]{monotype(((ArrayType) javaType).getComponentType())});
    
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
      {
	Internal.warning(name + " is not known");
	throw new NotIntroducedClassException(ts);
      }

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
          return SpecialArray.create(res);
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
  
  public static final 
  gnu.bytecode.Type typeRepresentationToBytecode(String type, 
						 bossa.util.Location loc)
  {
    if(type.charAt(0)=='[')
      {
	Type res = typeRepresentationToBytecode(type.substring(1), loc);
	if (res == null)
	  return null;
	else
          return SpecialArray.create(res);
      }
    
    TypeConstructor sym = 
      bossa.syntax.Node.getGlobalTypeScope().globalLookup(type, loc);

    return get(sym);
  }
  
  /****************************************************************
   * Manipulating bytecode types
   ****************************************************************/

  public static Type equivalentObjectType(Type t)
  {
    if (t instanceof ObjectType)
      return (ObjectType) t;

    if (t == Type.boolean_type) return Type.boolean_ctype;
    else if (t == Type.double_type || t == Type.float_type ||
	     t == Type.long_type || t == Type.int_type || 
	     t == Type.short_type || t == Type.byte_type) 
      return PrimType.number_type;
    else if (t == Type.char_type) return PrimType.char_ctype;
    // Fix-up.
    else if (t == Type.void_type) return PrimType.void_type;
    
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
  
  private static HashMap stringToReflectClass;
  
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

    try{ c = classLoader.loadClass(className); }
    catch(ClassNotFoundException e)
      // when the class does not exist
      { }
    catch(NoClassDefFoundError e) 
      // when a class with similar name but with different case exists
      // can occur on case-insensitive file-systems (e.g. FAT)
      { }

    if (c != null && bossa.util.Debug.javaTypes)
      Debug.println("Loaded " + className + " from " + 
		    classLoader.getResource(className.replace('.','/') + ".class"));

    stringToReflectClass.put(className, c);
    
    return c;
  }

  private static ClassLoader classLoader;
  private static String currentClasspath = "NOT INITIALIZED";

  public static void setClasspath(String classpath)
  {
    /* Cache: do not reset the classloader if the classpath is unchanged.
       This it especially important as it seems the previous classloader
       and its classes do not get garbage collected.
    */
    if (currentClasspath.equals(classpath))
      return;

    currentClasspath = classpath;

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
	      components.add(f.getCanonicalFile().toURL());
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
	  catch(java.io.IOException e){
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
    if (!(m instanceof MonotypeConstructor))
      return QuoteExp.nullExp;
    
    TypeConstructor tc = rawType(m).head();

    if (tc == null)
      return QuoteExp.nullExp;

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
    
    return QuoteExp.nullExp;
  }

  private static Expression zeroInt = new QuoteExp(new Integer(0));
  private static Expression zeroFloat = new QuoteExp(new Float(0.0));
  private static Expression zeroChar = new QuoteExp(new Character((char) 0));

  /****************************************************************
   * Predicates
   ****************************************************************/

  public static boolean isVoid(mlsub.typing.Monotype m)
  {
    // The test to void should be more high-level than string comparison
    String rep = m.toString();
    return rep.equals("nice.lang.void");
  }

  public static boolean isVoid(mlsub.typing.Polytype t)
  {
    return isVoid(t.getMonotype());
  }

  public static boolean isPrimitive(TypeConstructor tc)
  {
    return javaType(tc) instanceof PrimType;
  }

  /****************************************************************
   * Manipulations on nice types
   ****************************************************************/

  static boolean isMaybe(Monotype m)
  {
    // This is prob. laxist, since getTC() might be different but equivalent to maybeTC (?)
    return (m instanceof MonotypeConstructor)
      && ((MonotypeConstructor) m).getTC() == ConstantExp.maybeTC;
  }

  static Monotype equivalent(Monotype m)
  {
    return rawType(m).equivalent();
  }

  public static void setMarkedKind(Monotype m)
  {
    m.setKind(ConstantExp.maybeTC.variance);
  }

  public static void makeMarkedType(MonotypeVar m)
  {
    m.setPersistentHeadLeq(ConstantExp.maybeTC);
  }

  /** return the type with nullness markers removed */
  public static Monotype rawType(Monotype m)
  {
    m = m.equivalent();
    if (!(m instanceof MonotypeConstructor))
      {
	// It is probably a bug if this happens
	//Internal.warning("Not kinded monotype: " + m);
	return m;
      }
    else
      return ((MonotypeConstructor) m).getTP()[0];
  }

  /** @return the codomain of a functional polytype with nullness marker */
  public static Monotype codomain(Polytype type)
  {
    return ((FunType) rawType(type.getMonotype())).codomain();
  }

  /** 
      Transforms \forall T:K.U into \forall T:K.sure<U>
  */
  public static Polytype addSure(Polytype type)
  {
    return new Polytype
      (type.getConstraint(), 
       bossa.syntax.Monotype.sure(type.getMonotype()));
  }

  /****************************************************************
   * Reset the state for a new compilation.
   ****************************************************************/

  public static void reset()
  {
    tcToGBType = new HashMap();
    stringToReflectClass = new HashMap();
  }
}
