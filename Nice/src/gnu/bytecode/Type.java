// Copyright (c) 1997  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.bytecode;
import java.io.*;

public abstract class Type {
  String signature;
  // Fully-qualified name (in external format, i.e. using '.' to separate).
  String this_name;
  /**
   * Nominal unpromoted size in bytes.
   */
  int size;

  Type () { }

  // Maps java.lang.Class to corresponding Type.
  private static java.util.Hashtable mapClassToType;

  /** Maps Java type name (e.g. "java.lang.String[]") to corresponding Type. */
  static java.util.Hashtable mapNameToType;

  public static Type lookupType (String name)
  {
    if (mapNameToType == null)
      {
	mapNameToType = new java.util.Hashtable();
	mapNameToType.put("byte",    byte_type);
	mapNameToType.put("short",   short_type);
	mapNameToType.put("int",     int_type);
	mapNameToType.put("long",    long_type);
	mapNameToType.put("float",   float_type);
	mapNameToType.put("double",  double_type);
	mapNameToType.put("boolean", boolean_type);
	mapNameToType.put("char",    char_type);
	mapNameToType.put("void",    void_type);
      }
    return (Type) mapNameToType.get(name);
  }

  /** Find an Type with the given name, or create a new one.
   * Use this for "library classes", where you need the field/method types,
   * but not one where you are about to generate code for.
   * @param name the name of the class (e..g. "java.lang.String").
   */
  public static Type getType (String name)
  {
    Type type = lookupType(name);
    if (type == null)
      {
	if (name.endsWith("[]"))
	  {
	    type = getType(name.substring(0, name.length()-2));
	    type = new ArrayType(type, name);
	  }
	else
	  {
	    ClassType cl = new ClassType(name);
	    try
	      {
		cl.reflectClass = Class.forName(name);
		cl.flags |= ClassType.EXISTING_CLASS;
	      }
	    catch (java.lang.ClassNotFoundException ex)
	      {
	      }
	    type = cl;
	  }
	mapNameToType.put(name, type);
      }
    return type;
  }

  /** Register that the Type for class is type. */
  public static void registerTypeForName(String name, Type type)
  {
    mapNameToType.put(name, type);
  }
  
  /** Register that the Type for class is type. */
  public static void registerTypeForClass(Class clas, Type type)
  {
    if (mapClassToType == null)
      mapClassToType = new java.util.Hashtable(100);
    mapClassToType.put(clas, type);
    type.reflectClass = clas;
  }

  public static void flushTypeChanges()
  {
    java.util.Enumeration types = mapNameToType.elements();
    while (types.hasMoreElements())
      {
	// types.nextElement() should be of type Type
	Object t = types.nextElement(); 
	if (t instanceof ClassType)
	  {
	    ClassType type = (ClassType) t;
	    if ((type.flags & ObjectType.ADD_METHODS_DONE) == 0)
	      continue;
	    
	    type.methods = type.last_method = null;
	    type.methods_count = 0;
	    type.addMethods(type.getReflectClass());
	  }
      }
  }
	
  public static Type make(Class reflectClass)
  {
    Type type;
    if (mapClassToType != null)
      {
	Object t = mapClassToType.get(reflectClass);
	if (t != null)
	  return (Type) t;
      }
    if (reflectClass.isArray())
      type = ArrayType.make(Type.make(reflectClass.getComponentType()));
    else if (reflectClass.isPrimitive())
      throw new Error("internal error - primitive type not found");
    else
      {
	String name = reflectClass.getName();
	type = lookupType(name);
	if (type == null)
	  {
	    ClassType cl = new ClassType(name);
	    cl.flags |= ClassType.EXISTING_CLASS;
	    type = cl;
	    mapNameToType.put(name, type);
	  }
	if (reflectClass.isInterface())
	  ((ClassType) type).access_flags |= Access.INTERFACE;
      }
    type.reflectClass = reflectClass;
    registerTypeForClass(reflectClass, type);
    return type;
  }

  public final String getSignature () { return signature; }
  protected void setSignature(String sig) { this.signature = sig.intern(); }

  Type (String nam, String sig) {
    this_name = nam;
    signature = sig.intern();
  }

  public Type promote () {
    return size < 4 ? int_type : this;
  }

  public final int getSize() { return size; }

  public final boolean isVoid () { return size == 0; }

  /** Returns the primitive type corresponding to a signature character.
   * @return a primitive type, or null if there is no such type. */
  public static Type signatureToPrimitive(char sig)
  {
    switch(sig)
      {
      case 'B':  return Type.byte_type;
      case 'C':  return Type.char_type;
      case 'D':  return Type.double_type;
      case 'F':  return Type.float_type;
      case 'S':  return Type.short_type;
      case 'I':  return Type.int_type;
      case 'J':  return Type.long_type;
      case 'Z':  return Type.boolean_type;
      case 'V':  return Type.void_type;
      }
    return null;
  }

  /** Get a Type corresponding to the given signature string. */
  public static Type signatureToType(String sig, int off, int len)
  {
    if (len == 0)
      return null;
    char c = sig.charAt(off);
    Type type;
    if (len == 1)
      {
	type = signatureToPrimitive(c);
	if (type != null)
	  return type;
      }
    if (c == '[')
      {
	type = signatureToType(sig, off+1, len-1);
	return type == null ? null : new ArrayType(type);
      }
    if (c == 'L' && len > 2 && sig.indexOf(';', off) == len-1+off)
      return getType(sig.substring(off+1,len-1+off).replace('/', '.'));
    return null;
  }

  /** Get a Type corresponding to the given signature string. */
  public static Type signatureToType(String sig)
  {
    return signatureToType(sig, 0, sig.length());
  }

  /** Return the length of the signature starting at a given string position.
   * Returns -1 for an invalid signature. */
  public static int signatureLength (String sig, int pos)
  {
    int len = sig.length();
    if (len <= pos)
      return -1;
    char c = sig.charAt(pos);
    int arrays = 0;
    while (c == '[')
      {
	arrays++;
	pos++;
	c = sig.charAt(pos);
      }
    if (signatureToPrimitive(c) != null)
      return arrays+1;
    if (c == 'L')
      {
	int end = sig.indexOf(';', pos);
	if (end > 0)
	  return arrays + end + 1 - pos;
      }
    return -1;
  }

  public static int signatureLength (String sig)
  {
    return signatureLength(sig, 0);
  }

  /** Returns the Java-level type name from a given signature.
   * Returns null for an invalid signature. */
  public static String signatureToName(String sig)
  {
    int len = sig.length();
    if (len == 0)
      return null;
    char c = sig.charAt(0);
    Type type;
    if (len == 1)
      {
	type = signatureToPrimitive(c);
	if (type != null)
	  return type.getName();
      }
    if (c == '[')
      {
	int arrays = 1;
	if (arrays < len && sig.charAt(arrays) == '[')
	  arrays++;
	sig = signatureToName(sig.substring(arrays));
	if (sig == null)
	  return null;
	StringBuffer buf = new StringBuffer(50);
	buf.append(sig);
	while (--arrays >= 0)
	  buf.append("[]");
	return buf.toString();
      }
    if (c == 'L' && len > 2 && sig.indexOf(';') == len-1)
      return sig.substring(1,len-1).replace('/', '.');
    return null;
  }

  public String getName ()
  {
    return this_name;
  }

  public static boolean isValidJavaTypeName (String name)
  {
    boolean in_name = false;
    int i;
    int len = name.length();
    while (len > 2 && name.charAt(len-1) == ']'
	   && name.charAt(len-2) == '[')
      len -= 2;
    for (i = 0;  i < len; i++)
      {
	char ch = name.charAt(i);
	if (ch == '.')
	  {
	    if (in_name)
	      in_name = false;
	    else
	      return false;
	  }
	else if (in_name ? Character.isJavaIdentifierPart(ch)
		 : Character.isJavaIdentifierStart(ch))
	  in_name = true;
	else
	  return false;
      }
    return i == len;
  }

  public boolean isInstance (Object obj)
  {
    return getReflectClass().isInstance(obj);
  }

  /** Return true if this is a "subtype" of other. */
  public boolean isSubtype (Type other)
  {
    Class thisClass = getReflectClass();
    Class otherClass = other.getReflectClass();
    if (thisClass != null && otherClass != null)
      return otherClass.isAssignableFrom(thisClass);
    if (other == Type.pointer_type && this instanceof ObjectType)
      return true;

    if (this instanceof ClassType && other instanceof ClassType)
      {
	ClassType me = (ClassType) this;
	ClassType he = (ClassType) other;
	
	if(he.isInterface())
	  return me.doesImplement(he);
	else
	  return me.isSubclass(he);
      }
    
    return false;
    /*
    int comp = compare(other);
    return comp == -1 || comp == 0;
    */
  }

  /**
   * Computes the common supertype
   *
   * Interfaces are not taken into account.
   * This would be difficult, since interfaces allow multiple-inheritance.
   * This means that there may exists multiple common supertypes
   * to t1 and t2 that are not comparable.
   *
   * @return the lowest type that is both above t1 and t2,
   *  or null if t1 and t2 have no common supertype.
   */
  public static Type lowestCommonSuperType(Type t1, Type t2)
  {
    if (t1 == neverReturnsType)
      return t2;
    if (t2 == neverReturnsType)
      return t1;
    if (t1 == null || t2 == null)
     return null;

    if (t1.isSubtype(t2))
      return t2;
    else if (t2.isSubtype(t1))
      return t1;
    else
      {
       // the only chance left is that t1 and t2 are ClassTypes.
       if (!(t1 instanceof ClassType && t2 instanceof ClassType))
         return null;
       ClassType c1 = (ClassType) t1;
       ClassType c2 = (ClassType) t2;
       if (c1.isInterface())
         return Type.pointer_type;
       if (c2.isInterface())
         return Type.pointer_type;

       return lowestCommonSuperType(c1.getSuperclass(), c2.getSuperclass());
      }
  }

  /** Return a numeric code showing "subtype" relationship:
   *  1: if other is a pure subtype of this;
   *  0: if has the same values;
   * -1: if this is a pure subtype of other;
   * -2: if they have values in common but neither is a subtype of the other;
   * -3: if the types have no values in common.
   * "Same member" is rather loose;  by "A is a subtype of B"
   * we mean that all instance of A can be "widened" to B.
   * More formally, A.compare(B) returns:
   *  1: all B values can be converted to A without a coercion failure
   *     (i.e. a ClassCastException or overflow), but not vice versa.
   *  0: all A values can be converted to B without a coercion failure
   *     and vice versa;
   * -1: all A values can be converted to B without a coercion failure
   *     not not vice versa;
   * -2: there are (potentially) some A values that can be converted to B,
   *     and some B values can be converted to A;
   * -3: there are no A values that can be converted to B, and neither
   *     are there any B values that can be converted to A.
   */
  public abstract int compare(Type other);

  /** Change result from compare to compensate for argument swapping. */
  protected static int swappedCompareResult(int code)
  {
    return code == 1 ? -1 : code == -1 ? 1 : code;
  }

  /** Return true iff t1[i].isSubtype(t2[i]) for all i. */
  public static boolean isMoreSpecific (Type[] t1, Type[] t2)
  {
    if (t1.length != t2.length)
      return false;
    for (int i = t1.length; --i >= 0; )
      {
	if (! t1[i].isSubtype(t2[i]))
	  return false;
      }
    return true;
  }

  public void emitIsInstance (CodeAttr code)
  {
    code.emitInstanceof(this);
  }

  /** Convert an object to a value of this Type.
   * Throw a ClassCastException when this is not possible. */
  public abstract Object coerceFromObject (Object obj);

  public Object coerceToObject (Object obj)
  {
    return obj;
  }

  /** Compile code to convert an object (on the stack) to this Type. */
  public void emitCoerceToObject (CodeAttr code)
  {
  }

  /** Compile code to convert an object (on the stack) of this Type 
      to toType. */
  public void emitCoerceTo (Type toType, CodeAttr code)
  {
    // default implementation
    emitCoerceToObject(code);
  }

  /** Compile code to coerce/convert from Object to this type. */
  public void emitCoerceFromObject (CodeAttr code)
  {
    throw new Error ("unimplemented emitCoerceFromObject for "+this);
  }

  public static final PrimType byte_type
    = new PrimType ("byte", "B", 1, java.lang.Byte.TYPE);
  public static final PrimType short_type
    = new PrimType ("short", "S", 2, java.lang.Short.TYPE);
  public static final PrimType int_type
    = new PrimType ("int", "I", 4, java.lang.Integer.TYPE);
  public static final PrimType long_type
    = new PrimType ("long", "J", 8, java.lang.Long.TYPE);
  public static final PrimType float_type
    = new PrimType ("float", "F", 4, java.lang.Float.TYPE);
  public static final PrimType double_type
    = new PrimType ("double", "D", 8, java.lang.Double.TYPE);
  public static final PrimType boolean_type
    = new PrimType ("boolean", "Z", 1, java.lang.Boolean.TYPE);
  public static final PrimType char_type
    = new PrimType ("char", "C", 2, java.lang.Character.TYPE);
  public static final PrimType void_type
    = new PrimType ("void", "V", 0, java.lang.Void.TYPE);

  /** The "return type" of an expression that never returns, e.g. a throw. */
  public static final PrimType neverReturnsType
    = new PrimType ("(never-returns)", "V", 0, java.lang.Void.TYPE);

  /** The magic type of null. */
  public static final ObjectType nullType = new ObjectType("(type of null)");

  static public ClassType pointer_type = ClassType.make("java.lang.Object");
  static public ClassType string_type = ClassType.make("java.lang.String");
  static public ClassType boolean_ctype = ClassType.make("java.lang.Boolean");
  static public ClassType throwable_type = ClassType.make("java.lang.Throwable");

  static public Type[] typeArray0 = new Type[0];
  static public Method toString_method
  = pointer_type.addMethod ("toString", typeArray0,
			     string_type, Access.PUBLIC);
  static public ClassType number_type = ClassType.make("java.lang.Number");
  static public Method intValue_method
  = number_type.addMethod ("intValue", typeArray0,
			    int_type, Access.PUBLIC);
  static public Method longValue_method
  = number_type.addMethod ("longValue", typeArray0,
			    long_type, Access.PUBLIC);
  static public Method floatValue_method
  = number_type.addMethod ("floatValue", typeArray0,
			    float_type, Access.PUBLIC);
  static public Method doubleValue_method
  = number_type.addMethod ("doubleValue", typeArray0,
			    double_type, Access.PUBLIC);
  static public Method booleanValue_method
  = boolean_ctype.addMethod ("booleanValue", typeArray0,
			      boolean_type, Access.PUBLIC);

  static public ClassType character_type = ClassType.make("java.lang.Character");
  static public Method charValue_method
  = character_type.addMethod ("charValue", typeArray0,
			       char_type, Access.PUBLIC);
  
  protected Class reflectClass;

  /** Get the java.lang.Class object for the representation type. */
  public java.lang.Class getReflectClass()
  {
    return reflectClass;
  }

  public String toString()
  {
    return "Type " + getName();
  }
}
