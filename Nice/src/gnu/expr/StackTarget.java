// Copyright (c) 1999  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.expr;
import gnu.bytecode.*;
import gnu.mapping.Values;

public class StackTarget extends Target
{
  Type type;
  public StackTarget(Type type) { this.type = type; }

  public Type getType() { return type; }

  public static Target getInstance(Type type)
  {
    return (type == Type.pointer_type ? Target.pushObject
            : new StackTarget(type));
  }

  protected boolean compileFromStack0(Compilation comp, Type stackType)
  {
    return compileFromStack0(comp, stackType, type);
  }

  static boolean compileFromStack0(Compilation comp, Type stackType, Type type)
  {
    if (type == stackType)
      return true;

    CodeAttr code = comp.getCode();
    if (! code.reachableHere())
      return true;

    if (stackType.isVoid())
      {
	// The value will be ignored anyway.
	comp.compileConstant(null);
	stackType = Type.pointer_type;
      }
    else if (stackType instanceof PrimType && type instanceof PrimType)
      {
	code.emitConvert(stackType, type);
	return true;
      }

    // NICE
    if (stackType.isSubtype(type))
      return true;

    stackType.emitCoerceTo(type, code);
    
    // checks if emitCoerceToObject did a good job
    return code.topType().isSubtype(type);

    /* KAWA
    stackType.emitCoerceToObject(code);
    return type instanceof ClassType && stackType.isSubtype(type);
    */
  }

  public static void convert(Compilation comp, Type stackType, Type targetType)
  {
    if (! compileFromStack0(comp, stackType, targetType))
      targetType.emitCoerceFromObject(comp.getCode());
  }

  public void compileFromStack(Compilation comp, Type stackType)
  {
    if (! compileFromStack0(comp, stackType))
      type.emitCoerceFromObject(comp.getCode());
  }
}
