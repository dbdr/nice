// Copyright (c) 1997, 2000  Per M.A. Bothner.
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

  /** The type used to implement types not natively understood by the JVM.

   * Usually, the identity function.  However, a language might handle
   * union types or template types or type expression scalculated at
   * run time.  In that case return the type used at the JVM level,
   * and known at compile time.
   */
  public Type getImplementationType()
  {
    return this;
  }

  public static void free(java.util.Map t)
  {
    for (java.util.Iterator e = t.entrySet().iterator(); e.hasNext();)
      {
        java.util.Map.Entry k = (java.util.Map.Entry) e.next();
        if (((Type) k.getValue()).collectable)
          e.remove();
      }
  }

  public boolean collectable;

  public static void reset()
  {
    mapClassToType.clear();
    free(mapNameToType);
  }

  // Maps java.lang.Class to corresponding Type.
  private static java.util.Map mapClassToType;

  /** Maps Java type name (e.g. "java.lang.String[]") to corresponding Type. */
  static java.util.Hashtable mapNameToType;

  static java.util.Hashtable getMapNameToType()
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
    return mapNameToType;
  }

  public static Type lookupType (String name)
  {
    return (Type) getMapNameToType().get(name);
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
	    type = nice.tools.code.SpecialArray.create(type);
	  }
	else
	  {
	    type = loadFromClasspath(name);
	    if (type == null)
	      type = new ClassType(name, ClassType.EXISTING_CLASS);
	  }
	mapNameToType.put(name, type);
      }
    return type;
  }

  private static nice.tools.code.ClassLoader loader;
  static {
    String runtime = nice.tools.code.TypeImport.getRuntime();
    loader = new nice.tools.code.ClassLoader
      (runtime, 
       new nice.tools.code.ClassLoader.Registrar() {
	 public void register(String name, ClassType type)
	 {
	   getMapNameToType().put(name, type);
	 }
       });
  }

  public static Type loadFromClasspath(String name)
  {
    Type type = lookupType(name);
    if (type != null)
      return type;

    if (name.startsWith("java.util."))
      return loader.load(name);
    else
      return null;
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
      mapClassToType = new java.util.HashMap(100);
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

    type = loadFromClasspath(reflectClass.getName());
    if (type != null)
      return type;

    if (reflectClass.isArray())
      type = nice.tools.code.SpecialArray.create(Type.make(reflectClass.getComponentType()));
    else if (reflectClass.isPrimitive())
      throw new Error("internal error - primitive type not found");
    else
      {
	String name = reflectClass.getName();
	type = lookupType(name);
	if (type == null
            || (type.reflectClass != reflectClass
                && type.reflectClass != null))
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

  public boolean isArray () { return false; }

  /** Returns the primitive type corresponding to a signature character.
   * @return a primitive type, or null if there is no such type. */
  public static PrimType signatureToPrimitive(char sig)
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
	return type == null ? null : nice.tools.code.SpecialArray.create(type);
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

  /** Get a Type corresponding to the given full signature string
      starting from offset[0]. 
      Set offset[0] to the end index of the signature.
   */
  public static Type fullSignatureToType(String sig, int[] offset)
  {
    char c = sig.charAt(offset[0]++);
    Type type = signatureToPrimitive(c);
    if (type != null)
      return type;

    if (c == '[')
      {
	type = fullSignatureToType(sig, offset);
	return type == null ? null : nice.tools.code.SpecialArray.create(type);
      }

    if (c == 'L')
      {
	int colon = sig.indexOf(';', offset[0]);
	int angle = sig.indexOf('<', offset[0]);
	int end = colon < angle || angle == -1 ? colon : angle;
	type = getType(sig.substring(offset[0], end).replace('/', '.'));
	offset[0] = end + 1;

	if (end == colon)
	  return type;

	type = new ParameterizedType((ClassType) type, parseArgs(sig, offset));
	// Skip ';'
	offset[0]++;
	return type;
      }

    if (c == 'T')
      {
	int end = sig.indexOf(';', offset[0]);
	type = TypeVariable.lookup(sig.substring(offset[0], end));

	/* This happens for F-bounded types. For instance
	   java.util.Collections.reverse:
	   <T:Ljava/lang/Object;:Ljava/lang/Comparable<TT;>;>(Ljava/util/List<TT;>;TT;)I
	   
	   Ignore for now. This give a smaller (less restrictive) type.

	if (type == null)
	  throw new Error("Could not look up " + 
			  sig.substring(offset[0], end));
	*/

	offset[0] = end + 1;
	return type;
      }

    offset[0]--;
    return null;
  }

  /** Get a Type[] corresponding to the given full signature string
      starting from offset[0]. 
      Set offset[0] to the end index of the signature.
   */
  private static Type[] parseArgs(String sig, int[] offset)
  {
    java.util.Stack args = new java.util.Stack();
    do {
      args.push(fullSignatureToType(sig, offset));
    }
    while (sig.charAt(offset[0]) != '>');
    // Skip the '>'
    offset[0]++;

    Type[] res = new Type[args.size()];
    for (int i = args.size();  --i >= 0; )
      res[i] = (Type) args.pop();

    return res;

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
    int comp = compare(other);
    return comp == -1 || comp == 0;
  }

  /** @return true if values of this type can be assigned to other
      <b>without widening nor conversion</b>.
  */
  public abstract boolean isAssignableTo(Type other);

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

    // Optimization
    if (t1 == t2)
      return t1;

    if (t1.isSubtype(t2))
      return t2;
    else if (t2.isSubtype(t1))
      return t1;
    else if (t1 == char_type && t2.isSubtype(int_type))
      return int_type;
    else if (t2 == char_type && t1.isSubtype(int_type))
      return int_type;
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
   *     (i.e. a ClassCastException or overflow or major loss of information),
   *     but not vice versa.
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

  /** Compile code to convert a object of this type on the stack to Object. */
  public void emitCoerceToObject (CodeAttr code)
  {
  }

  /** Compile code to convert an object (on the stack) of this Type 
      to toType. */
  public void emitCoerceTo (Type toType, CodeAttr code)
  {
    // Default implementation.
    emitCoerceToObject(code);
  }

  /** Compile code to convert an object (on the stack) of that Type 
      to this Type. */
  public void emitCoerceFrom (Type fromType, CodeAttr code)
  {
    // Default implementation: do nothing.
  }

  /** Compile code to coerce/convert from Object to this type. */
  public void emitCoerceFromObject (CodeAttr code)
  {
    throw new Error ("unimplemented emitCoerceFromObject for "+this);
  }

  /**
     Return an equivalent method when the receiver is of this type.
     Return null if no more precise method exists.
  */
  Method refineMethod (Method method)
  {
    return null;
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
  // place never-returns first, so that void is registered for Void.TYPE
  /** The "return type" of an expression that never returns, e.g. a throw. */
  public static final PrimType neverReturnsType = 
    new PrimType ("(never-returns)", "V", 0, java.lang.Void.TYPE);
  public static final PrimType void_type
    = new PrimType ("void", "V", 0, java.lang.Void.TYPE);

  public static ClassType int_ctype  = ClassType.make("java.lang.Integer");
  public static ClassType long_ctype = ClassType.make("java.lang.Long");
  public static ClassType char_ctype = ClassType.make("java.lang.Character");
  public static ClassType float_ctype = ClassType.make("java.lang.Float");
  public static ClassType double_ctype = ClassType.make("java.lang.Double");

  /** The magic type of null. */
  public static final ObjectType nullType = new ObjectType("(type of null)");

  static public ClassType string_type = ClassType.make("java.lang.String");
  /* The String type. but coercion is handled by toString. */
  static public ClassType tostring_type = new ClassType("java.lang.String");

  static public ClassType pointer_type = ClassType.make("java.lang.Object");
  static public ClassType boolean_ctype = ClassType.make("java.lang.Boolean");
  static public ClassType throwable_type = ClassType.make("java.lang.Throwable");

  static public ClassType short_ctype = ClassType.make("java.lang.Short");
  static public ClassType byte_ctype = ClassType.make("java.lang.Byte");

  static public Type[] typeArray0 = new Type[0];
  static public Method toString_method
  = pointer_type.getDeclaredMethod("toString", 0);
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

  static public ClassType character_type = 
    ClassType.make("java.lang.Character");
  static public Method charValue_method = 
    character_type.addMethod ("charValue", typeArray0,
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
