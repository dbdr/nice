// Copyright (c) 1997  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.bytecode;

public class ArrayType extends ObjectType
{
  public Type elements;

  /**
   * @deprecated  Replaced by {@link #make(gnu.bytecode.Type)}
   */
  public ArrayType (Type elements)
  {
    this(elements, elements.getName() + "[]");
  }

  //private ?
  ArrayType (Type elements, String name)
  {
    this_name = name;
    setSignature("[" + elements.getSignature());
    this.elements = elements;
  }

  public Type getImplementationType()
  {
    Type eltype = elements.getImplementationType();
    return elements == eltype ? this : make(eltype);
  }

  /** Find or create an ArrayType for the specified element type. */
  public static ArrayType make(Type elements)
  {
    String name = elements.getName() + "[]";
    ArrayType type = (ArrayType) Type.lookupType(name);
    if (type == null //|| type.elements != elements
	)
      {
	type = new ArrayType(elements, name);
	mapNameToType.put(name, type);
      }
    return type;
  }

  public Type getComponentType() { return elements; }

  public String getInternalName() { return getSignature(); }

  /** Return true if this is a "subtype" of other. */
  public boolean isSubtype (Type other)
  {
    if (!(other instanceof ArrayType))
      return false;
    
    Type otherElements = ((ArrayType) other).elements;
    
    return elements.isSubtype(otherElements);
  }

  public int compare(Type other)
  {
    if (other == nullType)
      return 1;
    if (other instanceof ArrayType)
      return elements.compare(((ArrayType) other).elements);
    else if (other.getName().equals("java.lang.Object"))
      return -1;
    else
      return -3;
  }

  /** @return true if values of this type can be assigned to other
      <b>without widening nor conversion</b>.
  */
  public boolean isAssignableTo(Type other)
  {
    if (other == pointer_type || this == other)
      return true;

    if (! (other instanceof ArrayType))
      return false;

    ArrayType o = (ArrayType) other;
    return elements.getSignature().equals(o.elements.getSignature());
  }

  public void emitCoerceFrom (Type fromType, CodeAttr code)
  {
    if (! (fromType instanceof ArrayType))
      {
	super.emitCoerceFrom (fromType, code);
	return;
      }

    Type from = ((ArrayType) fromType).elements;
    
    if (elements == from)
      // Nothing to do
      return;

    if (! (elements instanceof PrimType && from instanceof PrimType))
      // Java arrays are covariant for reference types. Nothing to do.
      return;

    /*
      Create a new array, then convert all the elements 
      and put them in the new array.
    */

    Scope scope = code.pushScope();

    Variable old = code.addLocal(fromType);
    code.emitStore(old);

    Variable newArray = code.addLocal(this);
    code.emitLoad(old);
    code.emitArrayLength();
    code.emitNewArray(elements, 1);
    code.emitStore(newArray);
    
    // Counter to go though each element.
    Variable elem = code.addLocal(Type.int_type);
    code.emitPushInt(0);
    code.emitStore(elem);

    Label test = new Label(code);
    Label copy = new Label(code);

    code.emitGoto(test);

    copy.define(code);
    
    // Prepare the store in the new array.
    code.emitLoad(newArray);
    code.emitLoad(elem);

    // Load an element from the source array, and convert it.
    code.emitLoad(old);
    code.emitLoad(elem);
    code.emitArrayLoad(from);
    code.emitConvert(from, elements);

    code.emitArrayStore(elements);

    code.emitInc(elem, (short) 1);

    // Check if there are more elements to copy.
    test.define(code);
    code.emitLoad(elem);
    code.emitLoad(old);
    code.emitArrayLength();
    code.emitGotoIfLt(copy);

    // The new array is the result.
    code.emitLoad(newArray);
    code.popScope();
  }
}
