package bossa;
import gnu.bytecode.*;
import gnu.math.IntNum;
import gnu.math.DFloNum;
import gnu.expr.*;
import kawa.lang.*;

import _Array;

/**
 * Use to implement some special types that convert differently. 
 * Based on kawa.lang.SpecialType
 */

public class SpecialArray extends gnu.bytecode.ArrayType
{
  public SpecialArray (Type elements)
  {
    super (elements);
    setSignature("L_Array;");
  }
  
  public String getNameOrSignature()
  {
    return "_Array";
  }
  
  public String getName()
  {
    return "_Array";
  }
  
  public Object coerceFromObject (Object obj)
  {
    return super.coerceFromObject(obj);
  }

  public void emitCoerceFromObject (CodeAttr code)
  {
    if(_ArrayMakeMethod == null)
      {
	Type[] args = new Type[1];
	args[0] = ArrayType.make(Type.pointer_type);
	_ArrayMakeMethod = _ArrayType.getDeclaredMethod("make", args, _ArrayType);
      }
    
    code.emitInvokeStatic(_ArrayMakeMethod);
  }

  public Object coerceToObject (Object obj)
  {
    return super.coerceToObject(obj);
  }

  public void emitCoerceToObject (CodeAttr code)
  {
    code.emitCheckcast(_ArrayType);
    code.emitGetField(_ArrayField);
  }

  public boolean isSubtype (Type other)
  {
    boolean res = other instanceof SpecialArray || _ArrayType.isSubtype(other);
    if(!res)
      bossa.util.Debug.println("SpecialArray is not subtype of "+other);
    
    return res;
  }
  
  /****************************************************************
   * _Array components
   ****************************************************************/

  private static ClassType _ArrayType;
  private static Field _ArrayField;
  public static Method _ArrayMakeMethod;

  static
  {
    _ArrayType = ClassType.make("_Array");
    
    ClassType sequence = ClassType.make("bossa.lang.Sequence");
    sequence.access_flags = Access.PUBLIC|Access.INTERFACE;
    _ArrayType.setInterfaces(new ClassType[]{sequence});
    
    _ArrayField = new Field(_ArrayType);
    _ArrayField.setName("value");
    _ArrayField.setType(ArrayType.make(Type.pointer_type));
  }
  
  public String toString()
  {
    return "Special "+super.toString();
  }
}
