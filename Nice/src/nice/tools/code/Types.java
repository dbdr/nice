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

import mlsub.typing.*;
import bossa.util.Debug;
import bossa.util.Internal;
import bossa.syntax.PrimitiveType;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

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
  
  private static void set(Monotype m, Type type)
  {
    tcToGBType.put(m, type);
  }
  
  public static Type get(TypeConstructor tc)
  {
    return (Type) tcToGBType.get(tc);
  }

  public static Type get(Monotype m)
  {
    return (Type) tcToGBType.get(m);
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
    
    if (m instanceof mlsub.typing.TupleType)
      setBytecodeType(((mlsub.typing.TupleType) m).getComponents());

    TypeConstructor tc = m.head();
    if (tc == null)
      return;

    TypeConstructor rigidTC;
    if (tc == PrimitiveType.arrayTC)
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
    
    /* Only for arrays, we are interested in the components too.
       It that case we associate the bytecode type to the Nice monotype,
       not to the type constructor.
    */
    if (rigidTC == PrimitiveType.arrayTC)
      {
	Monotype param = ((MonotypeConstructor) m).getTP()[0];

	setBytecodeType(param);
	Types.set(m, SpecialTypes.array(javaTypeOrNull(param)));
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
	  Constraint.enter(cst);
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
    Type res = javaTypeOrNull(m);
    if (res != null)
      return res;
    else
      return Type.pointer_type;
  }

  /**
     @return the java type of a monotype, or null when no precise type can
     be given (for an unconstrained type parameter, for instance).

     It is useful to distinguish the "no precise type" from the Object type,
     in the case where Object is explicitely mentioned in the source,
     because Object[] is Object[] in bytecode, while T[] is Object.
  */
  private static Type javaTypeOrNull(Monotype m)
  {
    Type res = rawJavaType(equivalent(m));
    if (res == null)
      return null;

    boolean maybe = isMaybe(m.equivalent());
    if (maybe) 
      return equivalentObjectType(res);
    else
      return res;    
  }

  private static Type rawJavaType(Monotype m)
  {
    Type res = get(m);
    if (res != null)
      return res;

    if (m instanceof mlsub.typing.TupleType)
      return new TupleType(javaType(((mlsub.typing.TupleType) m).getComponents()));
    
    if (m instanceof FunType)
      return gnu.expr.Compilation.typeProcedure;

    if (!(m instanceof MonotypeConstructor))
      return null;
    
    MonotypeConstructor mc = (MonotypeConstructor) m;
    TypeConstructor tc = mc.getTC();

    if (tc == PrimitiveType.arrayTC)
      return SpecialTypes.array(javaTypeOrNull(mc.getTP()[0]));
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

  static Type lowestCommonSupertype(Type[] types)
  {
    Type res = types[0];
    for (int i = 1; res != null && i < types.length; i++)
      res = Type.lowestCommonSuperType(res, types[i]);
    
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

  public static TypeConstructor typeConstructor (Type javaType)
    throws NotIntroducedClassException
  {
    TypeConstructor tc = bossa.syntax.Node.getGlobalTypeScope().
      globalLookup(javaType.getName(), null);

    if (tc == null)
      {
	Internal.warning(javaType + " is not known");
	throw new NotIntroducedClassException(tc);
      }

    if (tc.getId() == -1)
      throw new NotIntroducedClassException(tc);
	
    return tc;
  }

  public static Monotype[] monotype(Type[] javaTypes,
				    TypeVariable[] typeVariables,
				    TypeSymbol[] niceTypeVariables)
    throws ParametricClassException, NotIntroducedClassException
  {
    int len = javaTypes.length;
    Monotype[] res = new Monotype[len];
    for (int i = 0; i < len; i++)
      res[i] = monotype(javaTypes[i], typeVariables, niceTypeVariables);
    return res;
  }

  public static Monotype monotype(Type javaType, boolean sure)
    throws ParametricClassException, NotIntroducedClassException
  {
    return monotype(javaType, sure, null, null);
  }

  public static Monotype monotype(Type javaType, boolean sure,
				  TypeVariable[] typeVariables,
				  TypeSymbol[] niceTypeVariables)
    throws ParametricClassException, NotIntroducedClassException
  {
    return monotype(javaType, sure, typeVariables, niceTypeVariables, false);
  }

  public static Monotype monotype(Type javaType)
    throws ParametricClassException, NotIntroducedClassException
  {
    return monotype(javaType, null, null);
  }

  public static Monotype monotype(Type javaType,
				  TypeVariable[] typeVariables,
				  TypeSymbol[] niceTypeVariables)
    throws ParametricClassException, NotIntroducedClassException
  {
    Monotype res = getMonotype(javaType, typeVariables, niceTypeVariables);
    if (javaType instanceof ObjectType &&
	! (javaType instanceof TypeVariable))
      return bossa.syntax.Monotype.maybe(res);
    else
      // the sure is already there in getMonotype
      return res;
  }

  /**
    Special version for use in Import.java,
    it always checks whether nullness info may be added.
    And can handle arrays different(not yet implemented)
  */
  public static Monotype monotype(Type javaType, boolean sure,
				  TypeVariable[] typeVariables,
				  TypeSymbol[] niceTypeVariables,
				  boolean arraySure)
    throws ParametricClassException, NotIntroducedClassException
  {
    Monotype res = getMonotype(javaType, typeVariables, niceTypeVariables,
			       arraySure);
    //primitivetypes and typevariables should not get nullness info
    if (javaType instanceof ObjectType &&
	! (javaType instanceof TypeVariable))
      {
	if (sure)
	  return bossa.syntax.Monotype.sure(res);
    	else
	  return bossa.syntax.Monotype.maybe(res);
      }
    return res;
  }
  private static Monotype getMonotype(Type javaType,
				      TypeVariable[] typeVariables,
				      TypeSymbol[] niceTypeVariables)
    throws ParametricClassException, NotIntroducedClassException
  {
    return getMonotype(javaType, typeVariables, niceTypeVariables, false);
  }

  private static Monotype getMonotype(Type javaType,
				      TypeVariable[] typeVariables,
				      TypeSymbol[] niceTypeVariables,
				      boolean arraySure)
    throws ParametricClassException, NotIntroducedClassException
  {
    if(javaType.isVoid())
      return PrimitiveType.voidType;
    if(javaType == SpecialTypes.intType)
      return PrimitiveType.intType;
    if(javaType == SpecialTypes.booleanType)
      return PrimitiveType.boolType;
    if(javaType == SpecialTypes.charType)
      return PrimitiveType.charType;
    if(javaType == SpecialTypes.byteType)
      return PrimitiveType.byteType;    
    if(javaType == SpecialTypes.shortType)
      return PrimitiveType.shortType;
    if(javaType == SpecialTypes.longType)
      return PrimitiveType.longType;
    if(javaType == SpecialTypes.floatType)
      return PrimitiveType.floatType;
    if(javaType == SpecialTypes.doubleType)
      return PrimitiveType.doubleType;

    if (javaType instanceof ArrayType)
      if (arraySure)
	//making the component of an array sure
	return new MonotypeConstructor
	  (PrimitiveType.arrayTC, 
	   new Monotype[]{
	     monotype(((ArrayType) javaType).getComponentType(), true, 
		      typeVariables, niceTypeVariables, true)
	   });
      else
	return new MonotypeConstructor
	(PrimitiveType.arrayTC, 
	 new Monotype[]{
	   monotype(((ArrayType) javaType).getComponentType(), 
		    typeVariables, niceTypeVariables)
	 });
    
    if (javaType instanceof ParameterizedType)
      {
	ParameterizedType p = (ParameterizedType) javaType;
	try {
	  return new MonotypeConstructor
	    (typeConstructor(p.main), 
	     monotype(p.parameters, typeVariables, niceTypeVariables));
	}
	catch (BadSizeEx ex) {
	  // This can happen, at least if a bytecode method is using
	  // a generic class incorrectly.
	  throw new ParametricClassException(javaType.toString());
	}
      }

    if (javaType instanceof TypeVariable)
      {
	if (typeVariables != null)
	  for (int i = 0; i < typeVariables.length; i++)
	    if (typeVariables[i] == javaType)
	      return (Monotype) niceTypeVariables[i];

	Internal.warning("Type variable " + javaType.getName() + 
			 " is not known");
	throw new NotIntroducedClassException(null);
      }

    return getMonotype(javaType.getName());
  }
  
  /** 
      Thrown when one tries to get a monotype
      for a class defined to have type parameters.
      The rationale is that methods using that class
      should not be fetched, as the compiler cannot
      guess the correct type parameters.
  */
  public static class ParametricClassException extends Exception 
  { 
    ParametricClassException (String message) { super(message); }
  }
  
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

  private static Monotype getMonotype(String name)
  throws ParametricClassException, NotIntroducedClassException
  {
    if(name.endsWith("[]"))
      {
	name=name.substring(0,name.length()-2);
	return new MonotypeConstructor
	  (PrimitiveType.arrayTC, 
	   new Monotype[]{ getMonotype(name) });
      }
    
    TypeConstructor tc = bossa.syntax.Node.getGlobalTypeScope().
      globalLookup(name, null);

    if (tc == null)
      {
	Internal.warning(name + " is not known");
	throw new NotIntroducedClassException(tc);
      }

    if (tc.variance != null && tc.arity() != 0)
      throw new ParametricClassException(tc.toString());

    if (tc.getId() == -1)
      throw new NotIntroducedClassException(tc);
	
    return new MonotypeConstructor(tc, null);
  }
  
  /****************************************************************
   * Converting string to gnu.bytecode.Type
   ****************************************************************/

  public static final gnu.bytecode.Type type(bossa.syntax.LocatedString name)
  {
    return type(name.toString(), name.location());
  }

  public static final gnu.bytecode.Type type(String s, bossa.util.Location loc)
  {
    if(s.length()==0)
      return null;
    
    if(s.charAt(0)=='[')
      {
	Type res = type(s.substring(1), loc);
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
    
    return TypeImport.lookup(s, loc);
  }
  
  /**
     The type is not necessarily fully qulified. 
  */
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
    
    TypeConstructor sym = bossa.syntax.Node.getGlobalTypeScope().
      globalLookup(type, loc);

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
   * Default values
   ****************************************************************/

  public static Expression defaultValue(Monotype m)
  {
    if (!(m instanceof MonotypeConstructor))
      return QuoteExp.nullExp;
    
    TypeConstructor tc = rawType(m).head();

    if (tc == null)
      return QuoteExp.nullExp;

    if(tc == PrimitiveType.intTC ||
       tc == PrimitiveType.byteTC ||
       tc == PrimitiveType.shortTC ||
       tc == PrimitiveType.longTC)
      return zeroInt;
    if(tc == PrimitiveType.floatTC ||
       tc == PrimitiveType.doubleTC)
      return zeroFloat;
    if(tc == PrimitiveType.boolTC)
      return QuoteExp.falseExp;
    if(tc == PrimitiveType.charTC)
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
    return equivalent(m).head() == PrimitiveType.voidTC;
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

  public static boolean isMaybe(Monotype m)
  {
    // This is prob. laxist, since getTC() might be different but equivalent to maybeTC (?)
    return (m instanceof MonotypeConstructor)
      && ((MonotypeConstructor) m).getTC() == PrimitiveType.maybeTC;
  }

  public static boolean isSure(Monotype m)
  {
    // see comment by isMaybe (e?)
    return (m instanceof MonotypeConstructor)
      && ((MonotypeConstructor) m).getTC() == PrimitiveType.sureTC;
  }

  public static Monotype equivalent(Monotype m)
  {
    return rawType(m).equivalent();
  }

  public static void setMarkedKind(Monotype m)
  {
    m.setKind(NullnessKind.instance);
  }

  public static void makeMarkedType(MonotypeVar m)
  {
    m.setPersistentKind(NullnessKind.instance);
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

  /** return the type with nullness markers removed */
  public static Monotype rawType(MonotypeConstructor mc)
  {
    return mc.getTP()[0];
  }

  /** @return the domain of a functional monotype with nullness marker */
  public static Monotype[] domain(Monotype type)
  {
    return rawType(type).domain();
  }

  /** @return the domain of a functional polytype with nullness marker */
  public static Monotype[] domain(Polytype type)
  {
    return rawType(type.getMonotype()).domain();
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
    TypeImport.stringToReflectClass = new HashMap();
  }
}
