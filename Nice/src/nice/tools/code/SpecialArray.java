package nice.tools.code;

import gnu.bytecode.*;
import gnu.expr.*;

import bossa.util.Debug;

/**
   Arrays that are wrapped into objects implementing Sequence when needed.
 */

public final class SpecialArray extends gnu.bytecode.ArrayType
{
  /**
     Return a SpecialArray holding elements of type <tt>elements</tt>.
  */
  public static Type create(Type elements)
  {
    String prefix;
    if(elements instanceof PrimType)
      prefix = elements.getName();
    else
      prefix = null;
    Type res = Type.lookupType("[" + elements.getSignature());
    if(res != null && res instanceof SpecialArray)
      return res;
    
    return new SpecialArray(elements, prefix, false);
  }

  public static SpecialArray unknownTypeArray()
  {
    if (unknownTypeArray == null)
      unknownTypeArray = new SpecialArray(Type.pointer_type, null, true);
    return unknownTypeArray;
  }
  
  private static SpecialArray unknownTypeArray;

  /** true if the elements have a primitive type. */
  private boolean primitive;
  
  /**
     If true, this type denote array of elements with any type
     (could be primitive or not).
     So the signature for this is Object.
  */
  private boolean unknown;
  
  private SpecialArray (Type elements, String prefix, boolean unknown)
  {
    super (elements);

    this.unknown = unknown;
    this.primitive = prefix != null;
    this.prefix = prefix;

    if (unknown)
      // pretend we are java.lang.Object (for casts, method signatures...)
      setSignature("Ljava/lang/Object;");

    field = new Field(wrappedType);
    field.setName("value");
    field.setType(Type.pointer_type);
    
    if (!unknown)
      {
	Class c = elements.getReflectClass();
	if (c == null)
	  bossa.util.Internal.warning("Null refclass for " + elements.getName());
	else
	  {
	    if (c == Void.TYPE)
	      // It is illegal to construct an array of void values.
	      // This situation most probably originates in a bug 
	      // in the source program, so let's notify it.
	      bossa.util.User.error(null, "Arrays cannot contain void values");

	    c = java.lang.reflect.Array.newInstance(c, 0).getClass();
	    Type.registerTypeForClass(c, this);
	  }
	
	Type.registerTypeForName("[" + elements.getSignature(), this);
      }
  }

  public String getInternalName()
  { 
    if(unknown)
      return "java.lang.Object";
    else
      return getSignature(); 
  }

  /****************************************************************
   * Conversions
   ****************************************************************/

  private void callConvert(CodeAttr code)
  {
    if (convertMethod == null)
      if (primitive)
	convertMethod = wrappedType.getDeclaredMethod("convert_"+prefix, 1);
      else
	convertMethod = wrappedType.getDeclaredMethod("convert", 2);

    code.emitInvokeStatic(convertMethod);
  }

  private void callGenericConvert(CodeAttr code)
  {
    if (genericConvertMethod == null)
      if (primitive)
	 genericConvertMethod = wrappedType.getDeclaredMethod("gconvert_"+prefix, 1);
      else
	genericConvertMethod = wrappedType.getDeclaredMethod("gconvert", 2);

    code.emitInvokeStatic(genericConvertMethod);
  }

  public void emitCoerceFrom (Type fromType, CodeAttr code)
  {
    if (fromType instanceof ArrayType)
      {
	// Any array can fit in the unknownTypeArray.
	if (unknown)
	  return;

	ArrayType from = (ArrayType) fromType;

	// Do nothing either if both arrays are the same.
	if (elements.getSignature().equals(from.elements.getSignature()))
	  return;
	
	code.emitCheckcast(this);
      }
    else if (isCollectionArray(fromType))
      emitCoerceFromCollection(code);
    else
      // The only possibility left is that the value is some kind of array.
      emitCoerceFromArray(code);
  }

  /** @return true if the given type is one of the types an array can have
      when considered as a collection. 
  */
  private boolean isCollectionArray(Type t)
  {
    /* We list explicitely the type between Collection and Array.
       If the hierarchy was changed, this code should be modified.
       An alternative is to use 
       <code>t.isSubType(ClassType.make("nice.lang.Collection")) &&
             wrappedType.isSubType(t)
       </code>.
       However it would only work if we ensured that the class hierarchy
       at the gnu.bytecode level is correctly set up before any call 
       to this code. (Additionally it is less efficient)
    */
    String name = t.getName();
    return 
      "nice.lang.Collection".equals(name) || 
      "nice.lang.Sequence".equals(name);
  }

  public void emitCoerceFromObject (CodeAttr code)
  {
    bossa.util.Internal.warning
      ("SpecialArray.coerceFrom should probably be called instead of this");
    emitCoerceFromArray(code);
  }

  private void emitCoerceFromCollection (CodeAttr code)
  {
    code.emitCheckcast(wrappedType);
    code.emitGetField(field);

    emitCoerceFromArray(code);
  }

  private void emitCoerceFromArray (CodeAttr code)
  {
    // If this array type is the unknown, we have nothing to do.
    if (unknown)
      return;
    
    Label cast = new Label(code), end = new Label(code);

    code.emitDup();
    code.emitInstanceof(this);
    code.emitGotoIfIntNeZero(cast);
    
    code.emitCheckcast(objectArray);

    // For non primitive arrays, we pass a string that represents
    // the type of the elements, so that the correct array type
    // can be created.
    if (!primitive)
      code.emitPushString
	(((ObjectType) elements).getInternalName().replace('/', '.'));
    callConvert(code);
    // non primitive need the cast
    if (primitive)
      code.emitGoto(end);
    
    cast.define(code);
    code.emitCheckcast(this);
    
    end.define(code);
  }

  public void emitCoerceTo (Type toType, CodeAttr code)
  {
    if (toType instanceof ArrayType)
      coerce(code, this, (ArrayType) toType);
    else if (toType != Type.pointer_type)
      emitCoerceToObject(code);
  }
  
  public void emitCoerceToObject (CodeAttr code)
  {
    code.emitInvokeStatic(makeMethod);
  }

  private static void coerce (CodeAttr code, ArrayType from, ArrayType to)
  {
    // Any array can fit in the unknownTypeArray
    if (to == unknownTypeArray)
      return;

    Type elements = to.getComponentType();
    if (elements instanceof PrimType)
      {
	((SpecialArray) SpecialArray.create(elements)).callGenericConvert(code);
      }
    else
      {
	boolean generic = ! from.getSignature().startsWith("[L");

	// For non primitive arrays, we pass a string that represents
	// the type of the elements, so that the correct array type
	// can be created
	code.emitPushString
	  (((ObjectType) elements).getInternalName().replace('/', '.'));

	if (generic)
	  unknownTypeArray.callGenericConvert(code);
	else
	  unknownTypeArray.callConvert(code);

	// Assert what we now guarantee.
	code.emitCheckcast(to);
      }
  }

  /****************************************************************
   * Typing
   ****************************************************************/

  public Type getImplementationType()
  {
    if (unknown)
      return Type.pointer_type;
    else
      return this;
  }

  public boolean isSubtype (Type other)
  {
    return other instanceof ArrayType &&
    (other == unknownTypeArray ||
     elements.isSubtype(((ArrayType) other).getComponentType()));
  }
  
  public boolean isAssignableTo (Type other)
  {
    return
      other == Type.pointer_type || 
      other instanceof ArrayType &&
      (other == unknownTypeArray ||
       elements.isAssignableTo(((ArrayType) other).getComponentType()));
  }
  
  /****************************************************************
   * Fields
   ****************************************************************/

  private static ClassType wrappedType = ClassType.make("nice.lang.rawArray");
  public static ClassType wrappedType()
  {
    return wrappedType;
  }
  
  private Field field;

  private static ArrayType objectArray = new ArrayType(Type.pointer_type);  

  private Method convertMethod;
  private Method genericConvertMethod;
  private static Method makeMethod = wrappedType.getDeclaredMethod("make", 1);
  private String prefix;

  public String toString()
  {
    if (unknown)
      return "Array with unknown element type";
    else
      return "SpecialArray(" + elements + ")";
  }
}
