package bossa;

import gnu.bytecode.*;
import gnu.math.IntNum;
import gnu.math.DFloNum;
import gnu.expr.*;
import kawa.lang.*;

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
      prefix = "Object";
    Type res = Type.lookupType("[" + elements.getSignature());
    if(res != null && res instanceof SpecialArray)
      return res;
    
    return new SpecialArray(elements, prefix);
  }
  
  private SpecialArray (Type elements, String prefix)
  {
    super (elements);

    if(elements == Type.pointer_type)
      specialObjectArray = this;
    
    className = "nice.lang." + prefix + "Array";
    setSignature("[" + elements.getSignature());

    classType = ClassType.make(className);
    
    field = new Field(classType);
    field.setName("value");
    field.setType(this);

    Type.registerTypeForClass(java.lang.reflect.Array.newInstance
			      (elements.getReflectClass(), 0).getClass(), 
			      this);
    Type.registerTypeForName("[" + elements.getSignature(), this);
    
    makeMethod = classType.getDeclaredMethod("make", 1);
    
  }

  public Object coerceFromObject (Object obj)
  {
    return super.coerceFromObject(obj);
  }

  public void emitCoerceFromObject (CodeAttr code)
  {
    code.emitCheckcast(classType);
    code.emitGetField(field);
  }

  // not called for the moment
  public void emitCoerceFrom (Type fromType, CodeAttr code)
  {
    if (fromType instanceof ArrayType)
      code.emitCheckcast(this);
    else
      emitCoerceFromObject(code);
  }

  public Object coerceToObject (Object obj)
  {
    return super.coerceToObject(obj);
  }

  public void emitCoerceTo (Type toType, CodeAttr code)
  {
    if (toType instanceof ArrayType)
      code.emitCheckcast(toType);
    else if(toType != Type.pointer_type)
      emitCoerceToObject(code);
  }
  
  public void emitCoerceToObject (CodeAttr code)
  {
    code.emitInvokeStatic(makeMethod());
  }

  public boolean isSubtype (Type other)
  {
    return other instanceof ArrayType &&
      elements.isSubtype(((ArrayType) other).getComponentType());
  }
  
  /****************************************************************
   * Fields
   ****************************************************************/

  private ClassType classType;
  public static ClassType objectArrayType = ClassType.make("nice.lang.ObjectArray");
  public static ClassType arrayType = ClassType.make("nice.lang.AbstractArray");

  private static SpecialArray specialObjectArray;
  public static SpecialArray specialObjectArray()
  {
    if(specialObjectArray == null)
      specialObjectArray = (SpecialArray) create(Type.pointer_type);
    
    return specialObjectArray;
  }
  
  private Field field;

  private Method makeMethod;
  public Method makeMethod()
  {
    return makeMethod;
  }

  private String className;
  
  public String toString()
  {
    return "SpecialArray(" + elements + ")";
  }
}
