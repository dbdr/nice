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
    
    if (primitive)
       {
	 convertMethod = wrappedType.getDeclaredMethod("convert_"+prefix, 1);
	 genericConvertMethod = wrappedType.getDeclaredMethod("gconvert_"+prefix, 1);
       }
    else
      {
	convertMethod = wrappedType.getDeclaredMethod("convert", 2);
	genericConvertMethod = wrappedType.getDeclaredMethod("gconvert", 2);
      }

    if (convertMethod == null)
      bossa.util.Internal.error(this + " has no array conversion method");
  }

  public String getInternalName()
  { 
    if(unknown)
      return "java.lang.Object";
    else
      return getSignature(); 
  }

  public void emitCoerceFromObject (CodeAttr code)
  {
    Label cast = new Label(code), end = new Label(code), isArray = new Label(code);

    // first we get the field if the array is wrapped
    code.emitDup();
    code.emitInstanceof(wrappedType);
    code.emitGotoIfIntEqZero(isArray);

    code.emitCheckcast(wrappedType);
    code.emitGetField(field);
    
    // now we have some kind of array
    isArray.define(code);

    // if this array type is the unknown, we just had to do the unwrapping
    if (unknown)
      return;
    
    code.emitDup();
    code.emitInstanceof(this);
    code.emitGotoIfIntNeZero(cast);
    
    code.emitCheckcast(objectArray);

    // For non primitive arrays, we pass a string that represents
    // the type of the elements, so that the correct array type
    // can be created
    if (!primitive)
      code.emitPushString
	(((ObjectType) elements).getInternalName().replace('/', '.'));
    code.emitInvokeStatic(convertMethod);
    // non primitive need the cast
    if (primitive)
      code.emitGoto(end);
    
    cast.define(code);
    code.emitCheckcast(this);
    
    end.define(code);
  }

  // not called for the moment
  public void emitCoerceFrom (Type fromType, CodeAttr code)
  {
    if (fromType instanceof ArrayType)
      code.emitCheckcast(this);
    else
      emitCoerceFromObject(code);
  }

  public void emitCoerceTo (Type toType, CodeAttr code)
  {
    if (toType instanceof ArrayType)
      coerce(code, this, (ArrayType) toType);
    else if(toType != Type.pointer_type)
      emitCoerceToObject(code);
  }
  
  public void emitCoerceToObject (CodeAttr code)
  {
    code.emitInvokeStatic(makeMethod);
  }

  private static void coerce (CodeAttr code, ArrayType from, ArrayType to)
  {
    Type elements = to.getComponentType();
    if (elements instanceof PrimType)
      {
	code.emitInvokeStatic(((SpecialArray) SpecialArray.create(elements)).genericConvertMethod);
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
	  code.emitInvokeStatic(unknownTypeArray.genericConvertMethod);
	else
	  code.emitInvokeStatic(unknownTypeArray.convertMethod);

	// Assert what we now guarantee.
	code.emitCheckcast(to);
      }
  }

  public boolean isSubtype (Type other)
  {
    return other instanceof ArrayType &&
    (other == unknownTypeArray ||
     elements.isSubtype(((ArrayType) other).getComponentType()));
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

  public String toString()
  {
    if (unknown)
      return "Array with unknown element type";
    else
      return "SpecialArray(" + elements + ")";
  }
}
