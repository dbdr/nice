package nice.tools.code;

import gnu.bytecode.*;
import gnu.expr.*;

import bossa.util.Debug;

/**
   Arrays that are wrapped on the fly into objects implementing java.util.List 
   when needed.
 */

public class SpecialArray extends gnu.bytecode.ArrayType
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
    
    return new SpecialArray(elements, prefix, false, true);
  }

  public static SpecialArray unknownTypeArray()
  {
    if (unknownTypeArray == null)
      unknownTypeArray = new SpecialArray(objectType, null, true, true);
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
  
  protected SpecialArray (Type elements)
  {
    this(elements, 
         elements instanceof PrimType ? elements.getName() : null,
         false, false);
  }

  private SpecialArray (Type elements, String prefix, boolean unknown,
                        boolean register)
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
    field.setType(objectType);
    
    if (register && !unknown)
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

  // Only valid for primitive types.
  private void callConvert(CodeAttr code)
  {
    if (convertMethod == null)
      convertMethod = wrappedType.getDeclaredMethod("convert_" + prefix, 1);

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
       <code>t.isSubType(ClassType.make("java.util.Collection")) &&
             wrappedType.isSubType(t)
       </code>.
       However it would only work if we ensured that the class hierarchy
       at the gnu.bytecode level is correctly set up before any call 
       to this code. (Additionally it is less efficient)
    */
    String name = t.getName();
    return 
      "java.util.Collection".equals(name) || 
      "java.util.List".equals(name);
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
    
    Label convert = new Label(code), end = new Label(code);

    code.emitDup();
    code.emitInstanceof(this);
    code.emitGotoIfIntEqZero(convert);

    code.emitCheckcast(this);
    code.emitGoto(end);
    
    convert.define(code);
    code.emitCheckcast(objectArray);

    if (primitive)
      callConvert(code);
    else
      convertReferenceArray(code, this, elements);
    
    end.define(code);
  }

  private static void convertReferenceArray
    (CodeAttr code, ArrayType toType, Type elements)
  {
    code.emitDup();
    code.emitIfNotNull();

    code.emitDup();
    code.emitArrayLength();

    code.pushScope();
    Variable len = code.addLocal(Type.int_type);
    code.emitStore(len);

    code.emitPushInt(0);

    code.emitLoad(len);
    code.emitNewArray(elements);

    Variable dest = code.addLocal(toType);
    code.emitDup();
    code.emitStore(dest);

    code.emitPushInt(0);

    code.emitLoad(len);
	
    code.emitInvokeStatic(arraycopy);
    code.emitLoad(dest);
    code.emitCheckcast(toType);

    code.popScope();

    code.emitElse();

    code.emitPop(1);
    code.emitPushNull(toType);

    code.emitFi();
  }

  public static final Method arraycopy = ClassType.make("java.lang.System").
    getDeclaredMethod("arraycopy", 5);

  public void emitCoerceTo (Type toType, CodeAttr code)
  {
    if (toType instanceof ArrayType)
      coerce(code, this, (ArrayType) toType);
    else if (toType != objectType)
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

	if (generic) 
	  {
	    // For generic arrays, we pass a string that represents
	    // the type of the elements, so that the correct array type
	    // can be created
	    code.emitPushString
	      (((ObjectType) elements).getInternalName().replace('/', '.'));

	    unknownTypeArray.callGenericConvert(code);

	    // Assert what we now guarantee.
	    code.emitCheckcast(to);
	  }
	else
	  convertReferenceArray(code, to, elements);
      }
  }

  /****************************************************************
   * Typing
   ****************************************************************/

  public Type getImplementationType()
  {
    if (unknown)
      return objectType;
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
    if (this == other)
      return true;

    if (unknown)
      return other == objectType;

    return
      other == objectType || 
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

  private static ClassType objectType = ClassType.make("java.lang.Object");
  private static ArrayType objectArray = new ArrayType(objectType);

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
