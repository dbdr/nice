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
    //setSignature("L_Array;");
    //this_name = "_Array";
  }
  
  public Object coerceFromObject (Object obj)
  {
    return super.coerceFromObject(obj);
  }

  public void emitCoerceFromObject (CodeAttr code)
  {
    code.emitCheckcast(_ArrayType);
    code.emitGetField(_ArrayField);
  }

  public Object coerceToObject (Object obj)
  {
    return super.coerceToObject(obj);
  }

  public void emitCoerceToObject (CodeAttr code)
  {
    code.emitInvokeStatic(_ArrayMakeMethod);
  }

  public boolean isSubtype (Type other)
  {
    if(other instanceof SpecialArray)
      return true;
    
    return false;
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
    Type[] args = new Type[1];
    args[0] = new ArrayType(Type.pointer_type);
    _ArrayMakeMethod = _ArrayType.addMethod ("make", args, _ArrayType,
					   Access.PUBLIC|Access.STATIC);
    _ArrayField = new Field(_ArrayType);
    _ArrayField.setName("value");
    _ArrayField.setType(args[0]);
  }
  
  public String toString()
  {
    return "Special "+super.toString();
  }
}
