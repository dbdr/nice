package bossa;
import gnu.bytecode.*;
import gnu.math.IntNum;
import gnu.math.DFloNum;
import gnu.expr.*;
import kawa.lang.*;

/**
   Arrays that are wrapped into objects implementing Sequence when needed.
 */

public final class SpecialArray extends gnu.bytecode.ArrayType
{
  private SpecialArray (Type elements, String prefix)
  {
    super (elements);

    if(elements == Type.pointer_type)
      specialObjectArray = this;
    
    className = prefix+"Array";
    setSignature("L"+className+";");
    //setSignature("["+elements.getSignature());

    classType = ClassType.make(className);
    
    ClassType sequence = ClassType.make("nice.lang.Sequence");
    sequence.access_flags = Access.PUBLIC|Access.INTERFACE;
    classType.setInterfaces(new ClassType[]{sequence});
    
    field = new Field(classType);
    field.setName("value");
    field.setType(ArrayType.make(elements));

    try{
      Type.registerTypeForClass(Class.forName(className), this);
    }
    catch(ClassNotFoundException e){
      bossa.util.Internal.error("Nice class for " + 
				elements + " arrays not found");
    }
    Type.registerTypeForName(className, this);
  }
  
  /**
     Return a SpecialArray holding elements of type <tt>elements</tt>.
   */
  public static Type create(Type elements)
  {
    String prefix;
    if(elements instanceof PrimType)
      prefix = elements.getName();
    else
      {
	prefix = "Object";
	elements = Type.pointer_type;
      }
    Type res = Type.lookupType(prefix + "Array");
    if(res!=null && res instanceof SpecialArray)
      return res;
    
    return new SpecialArray(elements, prefix);
  }
  
  public String getNameOrSignature()
  {
    return className;
  }
  
  public String getName()
  {
    return className;
  }
  
  public Object coerceFromObject (Object obj)
  {
    return super.coerceFromObject(obj);
  }

  public void emitCoerceFromObject (CodeAttr code)
  {
    code.emitInvokeStatic(makeMethod());
  }

  public Object coerceToObject (Object obj)
  {
    return super.coerceToObject(obj);
  }

  public void emitCoerceToObject (CodeAttr code)
  {
    code.emitCheckcast(classType);
    code.emitGetField(field);
  }

  public boolean isSubtype (Type other)
  {
    return other instanceof SpecialArray || classType.isSubtype(other);
  }
  
  /****************************************************************
   * Fields
   ****************************************************************/

  private ClassType classType;
  public static ClassType objectArrayType = ClassType.make("ObjectArray");
  public static ClassType arrayType = ClassType.make("Array");
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
    if(makeMethod == null)
      {
	Type[] args = new Type[] { ArrayType.make(elements) };
	makeMethod = classType.getDeclaredMethod("make", args, this);
	
	if(makeMethod == null)
	  bossa.util.Internal.error(this + " does not have a make method");
      }
    return makeMethod;
  }

  private String className;
  
  public String toString()
  {
    return "Special "+super.toString();
  }
}
