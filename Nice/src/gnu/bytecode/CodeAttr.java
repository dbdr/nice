// Copyright (c) 1997, 1998, 1999, 2001  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.bytecode;
import java.io.*;

/**
  * Represents the contents of a standard "Code" attribute.
  * <p>
  * Most of the actual methods that generate bytecode operation
  * are in this class (typically with names starting with <code>emit</code>),
  * though there are also some in <code>Method</code>.
  * <p>
  * Note that a <code>CodeAttr</code> is an <code>Attribute</code>
  * of a <code>Method</code>, and can in turn contain other
  * <code>Attribute</code>s, such as a <code>LineNumbersAttr</code>.
  *
  * @author      Per Bothner
  */

public class CodeAttr extends Attribute implements AttrContainer
{
  Attribute attributes;
  public final Attribute getAttributes () { return attributes; }
  public final void setAttributes (Attribute attributes)
  { this.attributes = attributes; }
  LineNumbersAttr lines;
  public LocalVarsAttr locals;

  // In hindsight, maintaining stack_types is more hassle than it is worth.
  // Instead, better to just keep track of SP, which should catch most
  // stack errors, while being more general and less hassle.  FIXME.
  Type[] stack_types;

  public int SP;  // Current stack size (in "words")
  private int max_stack;
  private int max_locals;
  int PC;
  // readPC (which is <= PC) is a bound on locations that have been
  // saved into labels or otherwise externally seen.
  // Hence, we cannot re-arrange code upto readPC, but we can
  // rearrange code between readPC and PC.
  int readPC;
  byte[] code;

  // The PC of the previous instruction.
  int previousPC;

  /* The exception handler table, as a vector of quadruples
     (start_pc, end_pc, handler_pc, catch_type).
     Only the first exception_table_length quadrules are defined. */
  short[] exception_table;

  /* The number of (defined) exception handlers (i.e. quadruples)
     in exception_table. */
  int exception_table_length;

  /* A chain of labels.  Unsorted, except that the Label with
     the lowest element in fixups must be the first one. */
  Label labels;

  CodeFragment fragments;

  /** The stack of currently active conditionals. */
  IfState if_stack;

  /** The stack of currently active try statements. */
  TryState try_stack;

  public final TryState getTryStack() { return try_stack; }

  public final Method getMethod() { return (Method) getContainer(); }

  public final int getPC() { return PC; }

  public final ConstantPool getConstants ()
  {
    return getMethod().classfile.constants;
  }

  /* True if we cannot fall through to bytes[PC] -
     the previous instruction was an uncondition control transfer.  */
  boolean unreachable_here;
  /** True if control could reach here. */
  public boolean reachableHere () { return !unreachable_here; }
  public final void setReachable(boolean val) { unreachable_here = !val; }
  public final void setUnreachable() { unreachable_here = true; }

  /** Get the maximum number of words on the operand stack in this method. */
  public int getMaxStack() { return max_stack; }
  /** Get the maximum number of local variable words in this method. */
  public int getMaxLocals() { return max_locals; }

  /** Set the maximum number of words on the operand stack in this method. */
  public void setMaxStack(int n) { max_stack = n; }
  /** Set the maximum number of local variable words in this method. */
  public void setMaxLocals(int n) { max_locals = n; }

  /** Get the code (instruction bytes) of this method.
    * Does not make a copy. */
  public byte[] getCode() { return code; }
  /** Set the code (instruction bytes) of this method.
    * @param code the code bytes (which are not copied).
    * Implicitly calls setCodeLength(code.length). */
  public void setCode(byte[] code) {
    this.code = code; this.PC = code.length; readPC = PC; }
  /** Set the length the the code (instruction bytes) of this method.
    * That is the number of current used bytes in getCode().
    * (Any remaing bytes provide for future growth.) */
  public void setCodeLength(int len) { PC = len; readPC = len;}
  /** Set the current lengthof the code (instruction bytes) of this method. */
  public int getCodeLength() { readPC = PC;  return PC; }

  public CodeAttr (Method meth)
  {
    super ("Code");
    addToFrontOf(meth);
    meth.code = this;
  }

  public final void reserve (int bytes)
  {
    previousPC = PC;
    if (code == null)
      code = new byte[100+bytes];
    else if (PC + bytes > code.length)
      {
	byte[] new_code = new byte[2 * code.length + bytes];
	System.arraycopy (code, 0, new_code, 0, PC);
	code = new_code;
      }

    while (labels != null && labels.fixups != null) {
      int oldest_fixup = labels.fixups[0];
      int threshold = unreachable_here ? 30000 : 32000;
      if (PC + bytes - oldest_fixup > threshold)
	labels.emit_spring (this);
      else
	break;
    }
  }

  /**
   * Write an 8-bit byte to the current code-stream.
   * @param i the byte to write
   */
  public final void put1(int i)
  {
    code[PC++] = (byte) i;
    unreachable_here = false;
  }

  /**
   * Write a 16-bit short to the current code-stream
   * @param i the value to write
   */
  public final void put2(int i)
  {
    code[PC++] = (byte) (i >> 8);
    code[PC++] = (byte) (i);
    unreachable_here = false;
  }
  /**
   * Write a 32-bit int to the current code-stream
   * @param i the value to write
   */
  public final void put4(int i)
  {
    code[PC++] = (byte) (i >> 24);
    code[PC++] = (byte) (i >> 16);
    code[PC++] = (byte) (i >> 8);

    code[PC++] = (byte) (i);
    unreachable_here = false;
  }

  public final void putIndex2 (CpoolEntry cnst)
  {
    put2(cnst.index);
  }

  public final void putLineNumber (String file, int linenumber)
  {
    if (lines == null)
      lines = new LineNumbersAttr(this);
    readPC = PC;
    lines.put(file, linenumber, PC);
  }

  public final void pushType(Type type)
  {
    if (type.size == 0)
      throw new Error ("pushing void type onto stack");
    if (stack_types == null)
      stack_types = new Type[20];
    else if (SP + 1 >= stack_types.length) {
      Type[] new_array = new Type[2 * stack_types.length];
      System.arraycopy (stack_types, 0, new_array, 0, SP);
      stack_types = new_array;
    }
    if (type.size == 8)
      stack_types[SP++] = Type.void_type;
    stack_types[SP++] = type;
    if (SP > max_stack)
      max_stack = SP;
  }

  public final Type popType ()
  {
    if (SP <= 0)
      throw new Error("popType called with empty stack in "+getMethod());
    Type type = stack_types[--SP];
    if (type.size == 8)
      if (! popType().isVoid())
	throw new Error("missing void type on stack");
    return type;
  }

  public final Type topType ()
  {
    if (SP <= 0)
      //throw new Error("popType called with empty stack "+getMethod());
      return null;
    return stack_types[SP - 1];
  }

  /** Compile code to pop values off the stack (and ignore them).
   * @param nvalues the number of values (not words) to pop
   */
  public void emitPop (int nvalues)
  {
    for ( ; nvalues > 0;  --nvalues)
      {
        reserve(1);
	Type type = popType();
	if (type.size > 4)
	  put1(88);  // pop2
	else if (nvalues > 1)
	  { // optimization:  can we pop 2 4-byte words using a pop2
	    Type type2 = popType();
	    if (type2.size > 4)
	      {
		put1(87);  // pop
		reserve(1);
	      }
	    put1(88);  // pop2
	    --nvalues;
	  }
	else
	  put1(87); // pop
      }
  }

  public void emitSwap ()
  {
    reserve(1);
    Type type1 = popType();
    Type type2 = popType();

    if (type1.size > 4 || type2.size > 4)
      {
	// There is no swap instruction in the JVM for this case.
	// Fall back to a more convoluted way.
	pushType(type2);
	pushType(type1);
	emitDupX();
	emitPop(1);
      }
    else
      {
	pushType(type1);
	put1(95);  // swap
	pushType(type2);
      }
  }

  /** Emit code to duplicate the top element of the stack. */
  public void emitDup ()
  {
    reserve(1);

    Type type = topType();
    put1 (type.size <= 4 ? 89 : 92); // dup or dup2
    pushType (type);
  }

  public void emitNop ()
  {
    reserve(1);
    put1 (0); // nop
  }

  /** Emit code to duplicate the top element of the stack
      and place the copy before the previous element. */
  public void emitDupX ()
  {
    reserve(1);

    Type type = popType();
    Type skipedType = popType();

    if (skipedType.size <= 4)
      put1 (type.size <= 4 ? 90 : 93); // dup_x1 or dup2_x1
    else
      put1 (type.size <= 4 ? 91 : 94); // dup_x2 or dup2_x2

    pushType (type);
    pushType (skipedType);
    pushType (type);
  }

  /** Compile code to duplicate with offset.
   * @param size the size of the stack item to duplicate (1 or 2)
   * @param offset where to insert the result (must be 0, 1, or 2)
   * The new words get inserted at stack[SP-size-offset]
   */
  public void emitDup (int size, int offset)
  {
    if (size == 0)
      return;
    reserve(1);
    // copied1 and (optionally copied2) are the types of the duplicated words
    Type copied1 = popType ();
    Type copied2 = null;
    if (size == 1)
      {
	if (copied1.size > 4)
	  throw new Error ("using dup for 2-word type");
      }
    else if (size != 2)
      throw new Error ("invalid size to emitDup");
    else if (copied1.size <= 4)
      {
	copied2 = popType();
	if (copied2.size > 4)
	  throw new Error ("dup will cause invalid types on stack");
      }

    int kind;
    // These are the types of the words (in any) that are "skipped":
    Type skipped1 = null;
    Type skipped2 = null;
    if (offset == 0)
      {
	kind = size == 1 ? 89 : 92;  // dup or dup2
      }
    else if (offset == 1)
      {
	kind = size == 1 ? 90 : 93; // dup_x1 or dup2_x1
	skipped1 = popType ();
	if (skipped1.size > 4)
	  throw new Error ("dup will cause invalid types on stack");
      }
    else if (offset == 2)
      {
	kind = size == 1 ? 91 : 94; // dup_x2 or dup2_x2
	skipped1 = popType();
	if (skipped1.size <= 4)
	  {
	    skipped2 = popType();
	    if (skipped2.size > 4)
	      throw new Error ("dup will cause invalid types on stack");
	  }
      }
    else
      throw new Error ("emitDup:  invalid offset");

    put1(kind);
    if (copied2 != null)
      pushType(copied2);
    pushType(copied1);
    if (skipped2 != null)
      pushType(skipped2);
    if (skipped1 != null)
      pushType(skipped1);
    if (copied2 != null)
      pushType(copied2);
    pushType(copied1);
  }

  /**
   * Compile code to duplicate the top 1 or 2 words.
   * @param size number of words to duplicate
   */
  public void emitDup (int size)
  {
    emitDup(size, 0);
  }

  /** Duplicate the top element of the given type. */
  public void emitDup (Type type)
  {
    emitDup(type.size > 4 ? 2 : 1, 0);
  }

  public void enterScope (Scope scope)
  {
    scope.setStartPC(PC);
    locals.enterScope(scope);
  }

  public Scope pushScope () {
    Scope scope = new Scope ();
    scope.start_pc = PC;
    readPC = PC;
    if (locals == null)
      locals = new LocalVarsAttr(getMethod());
    locals.enterScope(scope);
    if (locals.parameter_scope == null) 
      locals.parameter_scope= scope;
    return scope;
  }

  public Scope getCurrentScope()
  {
    return locals.current_scope;
  }

  /** Return the toplevel scope, corresponding to the current method. */
  public Scope methodScope()
  {
    Scope res = getCurrentScope();
    while (res.parent != null)
      res = res.parent;
    return res;
  }

  public Scope popScope () {
    Scope scope = locals.current_scope;
    locals.current_scope = scope.parent;
    scope.end_pc = PC;  readPC = PC;
    for (Variable var = scope.vars; var != null; var = var.next) {
      if (var.isSimple () && ! var.dead ())
	var.freeLocal(this);
    }
    return scope;
  }

  /** Get the index'th parameter. */
  public Variable getArg (int index)
  {
    return locals.parameter_scope.getVariable(index);
  }

  /**
   * Search by name for a Variable
   * @param name name to search for
   * @return the Variable, or null if not found (in any scope of this Method).
   */
  public Variable lookup (String name)
  {
    Scope scope = locals.current_scope;
    for (; scope != null;  scope = scope.parent)
      {
	Variable var = scope.lookup (name);
	if (var != null)
	  return var;
      }
    return null;
  }

  /** Add a new local variable (in the current scope).
   * @param type type of the new Variable.
   * @return the new Variable. */
  public Variable addLocal (Type type)
  {
    return locals.current_scope.addVariable(this, type, null);
  }

  /** Add a new local variable (in the current scope).
   * @param type type of the new Variable.
   * @param name name of the new Variable.
   * @return the new Variable. */
  public Variable addLocal (Type type, String name)
  {
    return locals.current_scope.addVariable (this, type, name);
  }

  /** Call addLocal for parameters (as implied by method type). */
  public void addParamLocals()
  {
    Method method = getMethod();
    if ((method.access_flags & Access.STATIC) == 0)
      addLocal(method.classfile).setParameter(true);
    int arg_count = method.arg_types.length;
    for (int i = 0;  i < arg_count;  i++)
      addLocal(method.arg_types[i]).setParameter(true);
  }

  public final void emitPushConstant(int val, Type type)
  {
    switch (type.getSignature().charAt(0))
      {
      case 'B':  case 'C':  case 'I':  case 'Z':  case 'S':
	emitPushInt(val);  break;
      case 'J':
	emitPushLong((long)val);  break;
      case 'F':
	emitPushFloat((float)val);  break;
      case 'D':
	emitPushDouble((double)val);  break;
      default:
	throw new Error("bad type to emitPushConstant");
      }
  }

  public final void emitPushConstant (CpoolEntry cnst)
  {
    reserve(3);
    int index = cnst.index;
    if (cnst instanceof CpoolValue2)
      {
      	put1 (20); // ldc2w
	put2 (index);
      }
    else if (index < 256)
      {
	put1(18); // ldc1
	put1(index);
      }
    else
      {
	put1(19); // ldc2
	put2(index);
      }
  }

  public final void emitPushBoolean(boolean b)
  {
    reserve(1);
    put1(b ? 4 : 3);
    pushType(Type.boolean_type);
  }

  public final void emitPushInt(int i)
  {
    reserve(3);
    if (i >= -1 && i <= 5)
      put1(i + 3);  // iconst_m1 .. iconst_5
    else if (i >= -128 && i < 128)
      {
	put1(16); // bipush
	put1(i);
      }
    else if (i >= -32768 && i < 32768)
      {
	put1(17); // sipush
	put2(i);
      }
    else
      {
	emitPushConstant(getConstants().addInt(i));
      }
    pushType(Type.int_type);
  }

  public void emitPushLong (long i)
  {
    if (i == 0 || i == 1)
      {
	reserve(1);
	put1 (9 + (int) i);  // lconst_0 .. lconst_1
      }
    else if ((long) (int) i == i)
      {
	emitPushInt ((int) i);
	reserve(1);
	popType();
	put1 (133); // i2l
      }
    else
      {
	emitPushConstant(getConstants().addLong(i));
      }
    pushType(Type.long_type);
  }

  public void emitPushFloat (float x)
  {
    int xi = (int) x;
    if ((float) xi == x && xi >= -128 && xi < 128)
      {
	if (xi >= 0 && xi <= 2)
	  {
	    reserve(1);
	    put1(11 + xi);  // fconst_0 .. fconst_2
	    if (xi == 0 && Float.floatToIntBits(x) != 0) // x == -0.0
	      {
		reserve(1);
		put1(118);  // fneg
	      }
	  }
	else
	  {
	    // Saves space in the constant pool
	    // Probably faster, at least on modern CPUs.
	    emitPushInt (xi);
	    reserve(1);
	    popType();
	    put1 (134); // i2f
	  }
      }
    else
      {
	emitPushConstant(getConstants().addFloat(x));
      }
    pushType(Type.float_type);
  }

  public void emitPushDouble (double x)
  {
    int xi = (int) x;
    if ((double) xi == x && xi >= -128 && xi < 128)
      {
	if (xi == 0 || xi == 1)
	  {
	    reserve(1);
	    put1(14+xi);  // dconst_0 or dconst_1
	    if (xi == 0 && Double.doubleToLongBits(x) != 0L) // x == -0.0
	      {
		reserve(1);
		put1(119);  // dneg
	      }
	  }
	else
	  {
	    // Saves space in the constant pool
	    // Probably faster, at least on modern CPUs.
	    emitPushInt (xi);
	    reserve(1);
	    popType();
	    put1 (135); // i2d
	  }
      }
    else
      {
	emitPushConstant(getConstants().addDouble(x));
      }
    pushType(Type.double_type);
  }

  public final void emitPushString (String str)
  {
    emitPushConstant(getConstants().addString(str));
    pushType(Type.string_type);
  }

  public void emitPushNull ()
  {
    emitPushNull(Type.pointer_type);
  }

  /**
     @param type the type <code>null</code> is considered to have.
  */
  public void emitPushNull (Type type)
  {
    reserve(1);
    put1(1);  // aconst_null
    pushType(type);
  }

  public final void emitPushThis()
  {
    reserve(1);
    put1(42);  // aload_0
    pushType(getMethod().getDeclaringClass());
  }

  void emitNewArray (int type_code)
  {
    reserve(2);
    put1(188);  // newarray
    put1(type_code);
  }

  public final void emitArrayLength ()
  {
    if (! (popType() instanceof ArrayType))
      throw new Error( "non-array type in emitArrayLength" );
    
    reserve(1);
    put1(190);  // arraylength
    pushType(Type.int_type);
  }

  /* Returns an integer in the range 0 (for 'int') through 4 (for object
     reference) to 7 (for 'short') which matches the pattern of how JVM
     opcodes typically depend on the operand type. */

  private int adjustTypedOp  (char sig)
  {
    switch (sig)
      {
      case 'I':  return 0;  // int
      case 'J':  return 1;  // long
      case 'F':  return 2;  // float
      case 'D':  return 3;  // double
      default:   return 4;  // object
      case 'B':
      case 'Z':  return 5;  // byte or boolean
      case 'C':  return 6;  // char
      case 'S':  return 7;  // short
      }
  }

  private int adjustTypedOp  (Type type)
  {
    return adjustTypedOp(type.getSignature().charAt(0));
  }

  private void emitTypedOp (int op, Type type)
  {
    reserve(1);
    put1(op + adjustTypedOp(type));
  }

  private void emitTypedOp (int op, char sig)
  {
    reserve(1);
    put1(op + adjustTypedOp(sig));
  }

  /** Store into an element of an array.
   * Must already have pushed the array reference, the index,
   * and the new value (in that order).
   * Stack:  ..., array, index, value => ...
   */
  public void emitArrayStore (Type element_type)
  {
    popType();  // Pop new value
    popType();  // Pop index
    popType();  // Pop array reference
    emitTypedOp(79, element_type);
  }

  /** Load an element from an array.
   * Must already have pushed the array and the index (in that order):
   * Stack:  ..., array, index => ..., value */
  public void emitArrayLoad (Type element_type)
  {
    popType();  // Pop index
    popType();  // Pop array reference
    emitTypedOp(46, element_type);
    pushType(element_type);
  }

  /** Load an element from an array.
   * Must already have pushed the array and the index (in that order):
   * Stack:  ..., array, index => ..., value */
  public void emitArrayLoad ()
  {
    popType();  // Pop index
    ArrayType arrayType = (ArrayType) popType();  // Pop array reference
    Type elementType = arrayType.getComponentType();
    emitTypedOp(46, elementType);
    pushType(elementType);
  }

  /**
   * Invoke new on a class type.
   * Does not call the constructor!
   * @param type the desired new object type
   */
  public void emitNew (ClassType type)
  {
    reserve(3);
    put1(187); // new
    putIndex2(getConstants().addClass(type));
    pushType(type);
  }

  /** Compile code to allocate a new array.
   * The size should have been already pushed on the stack.
   * @param type type of the array elements
   */
  public void emitNewArray (Type element_type, int dims)
  {
    Type top = popType ().promote ();
    if (top != Type.int_type)
      throw new Error ("non-int dim. spec. in emitNewArray");

    if (element_type instanceof PrimType)
      {
	int code;
	switch (element_type.getSignature().charAt(0))
	  {
	  case 'B':  code =  8;  break;
	  case 'S':  code =  9;  break;
	  case 'I':  code = 10;  break;
	  case 'J':  code = 11;  break;
	  case 'F':  code =  6;  break;
	  case 'D':  code =  7;  break;
	  case 'Z':  code =  4;  break;
	  case 'C':  code =  5;  break;
	  default:   throw new Error("bad PrimType in emitNewArray");
	  }
	emitNewArray(code);
      }
    else if (element_type instanceof ArrayType)
    {
      reserve(4);
      put1(197); // multianewarray
      putIndex2 (getConstants ().addClass (new ArrayType (element_type)));
      if (dims < 1 || dims > 255)
	throw new Error ("dims out of range in emitNewArray");
      put1(dims);
      while (-- dims > 0) // first dim already popped
	{
	  top = popType ().promote ();
	  if (top != Type.int_type)
	    throw new Error ("non-int dim. spec. in emitNewArray");
	}
    }
    else if (element_type instanceof ObjectType)
      {
	reserve(3);
	put1(189); // anewarray
	putIndex2(getConstants().addClass((ObjectType) element_type));
      }
    else
      throw new Error ("unimplemented type in emitNewArray");

    pushType (new ArrayType (element_type));
  }

  public void emitNewArray (Type element_type)
  {
    emitNewArray (element_type, 1);
  }

  // We may want to deprecate this, because it depends on popType.
  private void emitBinop (int base_code)
  {
    Type type2 = popType().promote();
    Type type1_raw = popType();
    Type type1 = type1_raw.promote();
    if (type1 != type2 || ! (type1 instanceof PrimType))
      throw new Error ("non-matching or bad types in binary operation");
    emitTypedOp(base_code, type1);
    pushType(type1_raw);
  }

  private void emitBinop (int base_code, char sig)
  {
    popType();
    popType();
    emitTypedOp(base_code, sig);
    pushType(Type.signatureToPrimitive(sig));
  }

  private void emitBinop (int base_code, Type type)
  {
    popType();
    popType();
    emitTypedOp(base_code, type);
    pushType(type);
  }

  // public final void emitIntAdd () { put1(96); popType();}
  // public final void emitLongAdd () { put1(97); popType();}
  // public final void emitFloatAdd () { put1(98); popType();}
  // public final void emitDoubleAdd () { put1(99); popType();}

  public final void emitAdd(char sig) { emitBinop (96, sig); }
  public final void emitAdd(PrimType type) { emitBinop (96, type); }
  /** @deprecated */
  public final void emitAdd () { emitBinop (96); }

  public final void emitSub(char sig) { emitBinop (100, sig); }
  public final void emitSub(PrimType type) { emitBinop (100, type); }
  /** @deprecated */
  public final void emitSub () { emitBinop (100); }

  public final void emitMul () { emitBinop (104); }
  public final void emitDiv () { emitBinop (108); }
  public final void emitRem () { emitBinop (112); }
  public final void emitAnd () { emitBinop (126); }
  public final void emitIOr () { emitBinop (128); }
  public final void emitXOr () { emitBinop (130); }

  public final void emitShl () { emitShift (120); }
  public final void emitShr () { emitShift (122); }
  public final void emitUshr() { emitShift (124); }

  private void emitShift (int base_code)
  {
    Type type2 = popType().promote();
    Type type1_raw = popType();
    Type type1 = type1_raw.promote();

    if (type2 != Type.int_type)
      throw new Error ("the amount of shift must be an int");
    if (type1 != Type.int_type && type1 != Type.long_type)
      throw new Error ("the value shifted must be an int or a long");
    emitTypedOp(base_code, type1);
    pushType(type1_raw);
  }

  /** Unary numerical negation (-). */
  public final void emitNeg ()
  {
    Type type = popType().promote();
    emitTypedOp(116, type);
    pushType (type);
  }
  
  public final void emitNot(Type type)
  {
    emitPushConstant(1, type);
    emitAdd();
    emitPushConstant(1, type);
    emitAnd();
  }

  public void emitPrimop (int opcode, int arg_count, Type retType)
  {
    reserve(1);
    while (-- arg_count >= 0)
      popType();
    put1(opcode);
    pushType(retType);
  }

  void emitMaybeWide (int opcode, int index)
  {
    if (index >= 256)
      {
	put1(196); // wide
	put1(opcode);
	put2(index);
      }
    else
      {
	put1(opcode);
	put1(index);
      }
  }

  /**
   * Comple code to push the contents of a local variable onto the statck.
   * @param var The variable whose contents we want to push.
   */
  public final void emitLoad (Variable var)
  {
    if (var.dead())
      throw new Error("attempting to push dead variable");
    int offset = var.offset;
    if (offset < 0 || !var.isSimple())
      throw new Error ("attempting to load from unassigned variable "+var
		       +" simple:"+var.isSimple()+", offset: "+offset);
    Type type = var.getType().promote();
    reserve(4);
    int kind = adjustTypedOp(type);
    if (offset <= 3)
      put1(26 + 4 * kind + offset);  // [ilfda]load_[0123]
    else
      emitMaybeWide(21 + kind, offset); // [ilfda]load
    pushType(var.getType());
  }

  public void emitStore (Variable var)
  {
    if (var.dead ())
      throw new Error ("attempting to push dead variable" + var);
    int offset = var.offset;
    if (offset < 0 || !var.isSimple ())
      throw new Error ("attempting to store in unassigned variable "+var.getName()
		       +" simple:"+var.isSimple()+", offset: "+offset);
    Type type = var.getType().promote ();
    reserve(4);
    popType();
    int kind = adjustTypedOp(type);
    if (offset <= 3)
      put1(59 + 4 * kind + offset);  // [ilfda]store_[0123]
    else
      emitMaybeWide(54 + kind, offset); // [ilfda]store
  }


  public void emitInc (Variable var, short inc)
  {
    if (var.dead ())
      throw new Error ("attempting to increment dead variable");
    int offset = var.offset;
    if (offset < 0 || !var.isSimple ())
      throw new Error ("attempting to increment unassigned variable"+var.getName()
		       +" simple:"+var.isSimple()+", offset: "+offset);
    Type type = var.getType().promote ();
    reserve(6);
    if (type != Type.int_type)
      throw new Error("attempting to increment non-int variable");

    boolean wide = offset > 255 || inc > 255 || inc < -256;

    if (wide)
    {
      put1(196); // wide
      put1(132); // iinc
      put2(offset);
      put2(inc);
    }
    else
    {
      put1(132); // iinc
      put1(offset);
      put1(inc);
    }
  }
  

  private final void emitFieldop (Field field, int opcode)
  {
    reserve(3);
    put1(opcode);
    putIndex2(getConstants().addFieldRef(field));
  }

  /** Compile code to get a static field value.
   * Stack:  ... => ..., value */

  public final void emitGetStatic(Field field)
  {
    pushType(field.type);
    emitFieldop (field, 178);  // getstatic
  }

  /** Compile code to get a non-static field value.
   * Stack:  ..., objectref => ..., value */

  public final void emitGetField(Field field)
  {
    popType();
    pushType(field.type);
    emitFieldop(field, 180);  // getfield
  }

  /** Compile code to put a static field value.
   * Stack:  ..., value => ... */

  public final void emitPutStatic (Field field)
  {
    popType();
    emitFieldop(field, 179);  // putstatic
  }

  /** Compile code to put a non-static field value.
   * Stack:  ..., objectref, value => ... */

  public final void emitPutField (Field field)
  {
    popType();
    popType();
    emitFieldop(field, 181);  // putfield
  }

  /** Comptes the number of stack words taken by a list of types. */
  private int words(Type[] types)
  {
    int res = 0;
    for (int i=types.length; --i >= 0; )
      if (types[i].size > 4)
	res+=2;
      else
	res++;
    return res;
  }

  public void emitInvokeMethod (Method method, int opcode)
  {
    reserve(opcode == 185 ? 5 : 3);
    int arg_count = method.arg_types.length;
    boolean is_invokestatic = opcode == 184;
    if (is_invokestatic != ((method.access_flags & Access.STATIC) != 0))
      throw new Error
	("emitInvokeXxx static flag mis-match method.flags="+method.access_flags);
    Type receiverType = null;

    while (--arg_count >= 0)
      popType();
    if (!is_invokestatic)
      {
        arg_count++;
        receiverType = popType();
        // Don't change anything if the call is an invokespecial
        if (opcode != 183 && receiverType != method.getDeclaringClass())
          {
            // We try to find a more precise method, given the known
            // type of the receiver.
            Method preciseMethod = receiverType.refineMethod(method);

            if (preciseMethod != null &&
                // Don't choose a more precise method if it is an
                // interface method, and the original is a class method
                // (can happen if the original class is Object, like in
                // Object.equals, refined in List.equals).
                ((! preciseMethod.getDeclaringClass().isInterface()) ||
                 method.getDeclaringClass().isInterface()))
              {
                method = preciseMethod;
                // It could be that the call was an invokeinterface, and
                // now became an invokevirtual.
                if (opcode == 185 && ! method.getDeclaringClass().isInterface())
                  opcode = 182;
              }
          }
      }

    if (! Access.legal(getMethod().getDeclaringClass(), method, receiverType))
      throw new VerificationError
        ("Method " + method.toString() + " is not accessible");

    put1(opcode);  // invokevirtual, invokespecial, or invokestatic
    putIndex2(getConstants().addMethodRef(method));
    if (opcode == 185)  // invokeinterface
      {
	put1(words(method.arg_types)+1); // 1 word for 'this'
	put1(0);
      }
    if (method.return_type.size != 0)
      pushType(method.return_type);
  }

  public void emitInvoke (Method method)
  {
    int opcode;
    if ((method.access_flags & Access.STATIC) != 0)
      opcode = 184;   // invokestatic
    else if (method.classfile.isInterface())
      opcode = 185;   // invokeinterface
    else
      opcode = 182;   // invokevirtual
    emitInvokeMethod(method, opcode);
  }

  /** Compile a virtual method call.
   * The stack contains the 'this' object, followed by the arguments in order.
   * @param method the method to invoke virtually
   */
  public void emitInvokeVirtual (Method method)
  {
    emitInvokeMethod(method, 182);  // invokevirtual
  }

  public void emitInvokeSpecial (Method method)
  {
    emitInvokeMethod(method, 183);  // invokespecial
  }

  /** Compile a static method call.
   * The stack contains the the arguments in order.
   * @param method the static method to invoke
   */
  public void emitInvokeStatic (Method method)
  {
    emitInvokeMethod(method, 184);  // invokestatic
  }

  public void emitInvokeInterface (Method method)
  {
    emitInvokeMethod(method, 185);  // invokeinterface
  }
  
  final void emitTransfer (Label label, int opcode)
  {
    put1(opcode);
    label.emit(this);
  }

  /** Compile an unconditional branch (goto) or a jsr.
   * @param label target of the branch (must be in this method).
   */
  public final void emitGoto (Label label, int opcode)
  {
    reserve(5);
    if (label.defined ())
      {
	readPC = PC;
	int delta = label.position - PC;
	if (delta < -32768)
	  {
	    put1(opcode-167);  // goto_w or jsr_w
	    put4(delta);
	  }
	else
	  {
	    put1(opcode); // goto or jsr
	    put2(delta);
	  }
      }
    else
      emitTransfer (label, opcode); // goto label or jsr label
  }

  /** Compile an unconditional branch (goto).
   * @param label target of the branch (must be in this method).
   */
  public final void emitGoto (Label label)
  {
    emitGoto(label, 167);
    setUnreachable();
  }

  public final void emitJsr (Label label)
  {
    emitGoto(label, 168);
  }

  public final void emitGotoIfEq (Label label, boolean invert)
  {
    Type type2 = popType().promote();
    Type type1 = popType().promote();
    reserve(4);
    int opcode;
    char sig1 = type1.getSignature().charAt(0);
    char sig2 = type2.getSignature().charAt(0);
    if (sig1 == 'I' && sig2 == 'I')
      opcode = 159;  // if_icmpeq (inverted: if_icmpne)
    else if (sig1 == 'J' && sig2 == 'J')
      {
	put1(148);   // lcmp
	opcode = 153;  // ifeq (inverted: ifne)
      }
    else if (sig1 == 'F' && sig2 == 'F')
      {
	put1(149);   // fcmpl
	opcode = 153;  // ifeq (inverted: ifne)
      }
    else if (sig1 == 'D' && sig2 == 'D')
      {
	put1(151);   // dcmpl
	opcode = 153;  // ifeq (inverted: ifne)
      }
    else if ((sig1 == 'L' || sig1 == '[')
	     && (sig2 == 'L' || sig2 == '['))
      opcode = 165;  // if_acmpeq (inverted: if_acmpne)
    else
      throw new Error ("non-matching types to emitGotoIfEq");
    if (invert)
      opcode++;
    emitTransfer (label, opcode);
  }

  /** Compile a conditional transfer if 2 top stack elements are equal. */
  public final void emitGotoIfEq (Label label)
  {
    emitGotoIfEq(label, false);
  }

  /** Compile conditional transfer if 2 top stack elements are not equal. */
  public final void emitGotoIfNE (Label label)
  {
    emitGotoIfEq(label, true);
  }

  public final void emitGotoIfCompare1 (Label label, int opcode)
  {
    popType();
    reserve(3);
    emitTransfer (label, opcode);
  }

  public final void emitGotoIfIntEqZero(Label label)
  { emitGotoIfCompare1(label, 153); }
  public final void emitGotoIfIntNeZero(Label label)
  { emitGotoIfCompare1(label, 154); }
  public final void emitGotoIfIntLtZero(Label label)
  { emitGotoIfCompare1(label, 155); }
  public final void emitGotoIfIntGeZero(Label label)
  { emitGotoIfCompare1(label, 156); }
  public final void emitGotoIfIntGtZero(Label label)
  { emitGotoIfCompare1(label, 157); }
  public final void emitGotoIfIntLeZero(Label label)
  { emitGotoIfCompare1(label, 158); }

  public final void emitGotoIfNull(Label label)
  { emitGotoIfCompare1(label, 198); } //ifnull
  public final void emitGotoIfNotNull(Label label)
  { emitGotoIfCompare1(label, 199); } //ifnonnull


  public final void emitGotoIfCompare2 (Label label, int logop)
  { 
    if( logop < 155 || logop > 158 )
      throw new Error ("emitGotoIfCompare2: logop must be one of iflt, ifgt, ifle, ifge");
    
    Type type2 = popType().promote();
    Type type1 = popType().promote();
    reserve(4);
    char sig1 = type1.getSignature().charAt(0);
    char sig2 = type2.getSignature().charAt(0);

    boolean cmpg = (logop == 155 || logop == 158); // iflt,ifle

    if (sig1 == 'I' && sig2 == 'I')
      logop += 6;  // iflt -> if_icmplt etc.
    else if (sig1 == 'J' && sig2 == 'J')
      put1(148);   // lcmp
    else if (sig1 == 'F' && sig2 == 'F')
      put1(cmpg ? 149 : 150);   // fcmpl/fcmpg
    else if (sig1 == 'D' && sig2 == 'D')
      put1(cmpg ? 151 : 152);   // dcmpl/dcmpg
    else
      throw new Error ("non-matching types to emitGotoIfCompare2");

    emitTransfer (label, logop);
  }

  // binary comparisons
  public final void emitGotoIfLt(Label label)
  { emitGotoIfCompare2(label, 155); }
  public final void emitGotoIfGe(Label label)
  { emitGotoIfCompare2(label, 156); }
  public final void emitGotoIfGt(Label label)
  { emitGotoIfCompare2(label, 157); }
  public final void emitGotoIfLe(Label label)
  { emitGotoIfCompare2(label, 158); }


  /** Compile start of a conditional:  if (!(x OPCODE 0)) ...
   * The value of x must already have been pushed. */
  public final void emitIfCompare1 (int opcode)
  {
    IfState new_if = new IfState(this);
    if (popType().promote() != Type.int_type)
      throw new Error ("non-int type to emitIfCompare1");
    reserve(3);
    emitTransfer (new_if.end_label, opcode);
    new_if.start_stack_size = SP;
  }

  /** Compile start of conditional:  if (x != 0) ...
   * Also use this if you have pushed a boolean value:  if (b) ... */
  public final void emitIfIntNotZero()
  {
    emitIfCompare1(153); // ifeq
  }

  /** Compile start of conditional:  if (x == 0) ...
   * Also use this if you have pushed a boolean value:  if (!b) ... */
  public final void emitIfIntZero()
  {
    emitIfCompare1(154); // ifne
  }

  /** Compile start of conditional:  if (x <= 0) */
  public final void emitIfIntLEqZero()
  {
    emitIfCompare1(157); // ifgt
  }

  /** Compile start of a conditional:  if (!(x OPCODE null)) ...
   * The value of x must already have been pushed and must be of
   * reference type. */
  public final void emitIfRefCompare1 (int opcode)
  {
    IfState new_if = new IfState(this);
    if (! (popType() instanceof ObjectType))
      throw new Error ("non-ref type to emitIfRefCompare1");
    reserve(3);
    emitTransfer (new_if.end_label, opcode);
    new_if.start_stack_size = SP;
  }  
  
  /** Compile start of conditional:  if (x != null) */
  public final void emitIfNotNull()
  {
    emitIfRefCompare1(198); // ifnull
  }

  /** Compile start of conditional:  if (x == null) */
  public final void emitIfNull()
  {
    emitIfRefCompare1(199); // ifnonnull
  }  
  
  /** Compile start of a conditional:  if (!(x OPCODE y)) ...
   * The value of x and y must already have been pushed. */
  public final void emitIfIntCompare(int opcode)
  {
    IfState new_if = new IfState(this);
    popType();
    popType();
    reserve(3);
    emitTransfer(new_if.end_label, opcode);
    new_if.start_stack_size = SP;
  }

  /* Compile start of a conditional:  if (x < y) ... */
  public final void emitIfIntLt()
  {
    emitIfIntCompare(162);  // if_icmpge
  }

  /** Compile start of a conditional:  if (x != y) ...
   * The values of x and y must already have been pushed. */
  public final void emitIfNEq ()
  {
    IfState new_if = new IfState (this);
    emitGotoIfEq(new_if.end_label);
    new_if.start_stack_size = SP;
  }

  /** Compile start of a conditional:  if (x == y) ...
   * The values of x and y must already have been pushed. */
  public final void emitIfEq ()
  {
    IfState new_if = new IfState (this);
    emitGotoIfNE(new_if.end_label);
    new_if.start_stack_size = SP;
  }

  /** Compile start of a conditional:  if (x < y) ...
   * The values of x and y must already have been pushed. */
  public final void emitIfLt ()
  {
    IfState new_if = new IfState (this);
    emitGotoIfGe(new_if.end_label);
    new_if.start_stack_size = SP;
  }

  /** Compile start of a conditional:  if (x >= y) ...
   * The values of x and y must already have been pushed. */
  public final void emitIfGe ()
  {
    IfState new_if = new IfState (this);
    emitGotoIfLt(new_if.end_label);
    new_if.start_stack_size = SP;
  }

  /** Compile start of a conditional:  if (x > y) ...
   * The values of x and y must already have been pushed. */
  public final void emitIfGt ()
  {
    IfState new_if = new IfState (this);
    emitGotoIfLe(new_if.end_label);
    new_if.start_stack_size = SP;
  }

  /** Compile start of a conditional:  if (x <= y) ...
   * The values of x and y must already have been pushed. */
  public final void emitIfLe ()
  {
    IfState new_if = new IfState (this);
    emitGotoIfGt(new_if.end_label);
    new_if.start_stack_size = SP;
  }

  /** Emit a 'ret' instruction.
    * @param var the variable containing the return address */
  public void emitRet (Variable var)
  {
    int offset = var.offset;
    if (offset < 256)
      {
	reserve(2);
	put1(169);  // ret
	put1(offset);
      }
    else
      {
	reserve(4);
	put1(196);  // wide
	put1(169);  // ret
	put2(offset);
      }
  }

  public final void emitIfThen ()
  {
    new IfState(this, null);
  }

  /** Compile start of else clause. */
  public final void emitElse ()
  {
    Label else_label = if_stack.end_label;
    Label end_label = new Label (this);
    if_stack.end_label = end_label;
    if (reachableHere ())
      {
	int growth = SP-if_stack.start_stack_size;
	if_stack.stack_growth = growth;
	if (growth > 0)
	  {
	    if_stack.then_stacked_types = new Type[growth];
	    System.arraycopy (stack_types, if_stack.start_stack_size,
			      if_stack.then_stacked_types, 0, growth);
	  }
	else
	  if_stack.then_stacked_types = new Type[0];  // ???
	emitGoto (end_label);
      }
    while (SP > if_stack.start_stack_size)
      popType();
    SP = if_stack.start_stack_size;
    if (else_label != null)
      else_label.define (this);
    if_stack.doing_else = true;    
  }

  /** Compile end of conditional. */
  public final void emitFi ()
  {
    boolean make_unreachable = false;
    if (! if_stack.doing_else)
      { // There was no 'else' clause.
	if (reachableHere ()
	    && SP != if_stack.start_stack_size)
	  throw new Error("at PC "+PC+" then clause grows stack with no else clause");
      }
    else if (if_stack.then_stacked_types != null)
      {
	int then_clause_stack_size
	  = if_stack.start_stack_size + if_stack.stack_growth;
	if (! reachableHere ())
	  {
	    if (if_stack.stack_growth > 0)
	      System.arraycopy (if_stack.then_stacked_types, 0,
				stack_types, if_stack.start_stack_size,
				if_stack.stack_growth);
	    SP = then_clause_stack_size;
	  }
	else if (SP != then_clause_stack_size)
	  {
	    for(int i=0; i<if_stack.then_stacked_types.length; i++)
	      System.out.println("Stack["+i+"] = "+if_stack.then_stacked_types[i]);
	    throw new Error("at PC "+PC+": SP at end of 'then' was " +
			    then_clause_stack_size
			    + " while SP at end of 'else' was " + SP);
	  }
        else
          {
            // Reconcile the types on the stack.
            for (int i = 0; i < if_stack.then_stacked_types.length; i++)
              {
                Type t1 = if_stack.then_stacked_types[i];
                Type t2 = stack_types[if_stack.start_stack_size + i];

                Type common = Type.lowestCommonSuperType(t1, t2);
                if (common == null)
                  common = Type.pointer_type;

                stack_types[if_stack.start_stack_size + i] = common;
              }
          }
      }
    else if (unreachable_here)
      make_unreachable = true;

    if (if_stack.end_label != null)
      if_stack.end_label.define (this);
    if (make_unreachable)
      setUnreachable();
    // Pop the if_stack.
    if_stack = if_stack.previous;
  }

  public final void emitConvert (Type from, Type to)
  {
    String to_sig = to.getSignature();
    String from_sig = from.getSignature();
    int op = -1;
    if (to_sig.length() == 1 || from_sig.length() == 1)
      {
	char to_sig0 = to_sig.charAt(0);
	char from_sig0 = from_sig.charAt(0);
	if (from_sig0 == to_sig0)
	  return;
	if (from.size < 4)
	  from_sig0 = 'I';
	if (to.size < 4)
	  {
	    emitConvert(from, Type.int_type);
	    from_sig0 = 'I';
	  }
	if (from_sig0 == to_sig0)
	  return;
	switch (from_sig0)
	  {
	  case 'I':
	    switch (to_sig0)
	      {
	        case 'B':  op = 145;  break;  // i2b
	        case 'C':  op = 146;  break;  // i2c
	        case 'S':  op = 147;  break;  // i2s
		case 'J':  op = 133;  break;  // i2l
		case 'F':  op = 134;  break;  // i2f
		case 'D':  op = 135;  break;  // i2d
	      }
	    break;
	  case 'J':
	    switch (to_sig0)
	      {
		case 'I':  op = 136;  break;  // l2i
		case 'F':  op = 137;  break;  // l2f
		case 'D':  op = 138;  break;  // l2d
	      }
	    break;
	  case 'F':
	    switch (to_sig0)
	      {
		case 'I':  op = 139;  break;  // f2i
		case 'J':  op = 140;  break;  // f2l
		case 'D':  op = 141;  break;  // f2d
	      }
	    break;
	  case 'D':
	    switch (to_sig0)
	      {
		case 'I':  op = 142;  break;  // d2i
		case 'J':  op = 143;  break;  // d2l
		case 'F':  op = 144;  break;  // d2f
	      }
	    break;
	  }
      }
    if (op < 0)
      throw new Error ("unsupported CodeAttr.emitConvert from "+
		       from + " to " + to);
    reserve(1);
    popType();
    put1(op);
    pushType(to);
  }

  private void emitCheckcast (Type type, int opcode)
  {
    reserve(3);
    popType();
    put1(opcode);
//      if (type instanceof ArrayType)
//        {
//  	ArrayType atype = (ArrayType) type;
//  	CpoolUtf8 name = getConstants().addUtf8(atype.signature);
//  	putIndex2(getConstants().addClass(name));
//        } else
    if (type instanceof ObjectType)
      {
	putIndex2(getConstants().addClass((ObjectType) type));
      }
    else
      throw new Error ("unimplemented type " + type
		       + " in emitCheckcast/emitInstanceof");
  } 

  public void emitCheckcast (Type type)
  {
    emitCheckcast(type, 192);
    pushType(type);
  }

  public void emitInstanceof (Type type)
  {
    emitCheckcast(type, 193);
    pushType(Type.boolean_type);
  }

  public final void emitThrow ()
  {
    popType();
    reserve(1);
    put1 (191);  // athrow
    setUnreachable();
  }

  public final void emitMonitorEnter ()
  {
    popType();
    reserve(1);
    put1 (194);  // monitorenter
  }

  public final void emitMonitorExit ()
  {
    popType();
    reserve(1);
    put1 (195);  // monitorexit
  }

  private void callFinallyBlocks()
  {
    TryState state = try_stack;

    /* If a value is returned, it must be saved to a local variable,
       to prevent a verification error because of inconsistent stack sizes.
    */
    boolean saveResult = ! getMethod().getReturnType().isVoid();
    Variable result = null;

    while (state != null)
      {
	if (state.finally_subr != null         // there is a finally block
	    && state.finally_ret_addr == null) // 'return' is not inside it
	  {
	    if (saveResult && result == null)
	      {
                // We store the result in a method-level variable, to make sure
                // that the finally blocks do not use the same slot.
		result = methodScope().addVariable(this, topType(), null);
		emitStore(result);
	      }
	    emitJsr(state.finally_subr);
	  }

	state = state.previous;
      }

    if (result != null)
      emitLoad(result);
  }
	    
  /**
   * Compile a method return.
   */
  public final void emitReturn ()
  {
    callFinallyBlocks();
    if (!checkPostcondition())
      emitRealReturn();
  }

  private void emitRealReturn()
  {
    if (getMethod().getReturnType().size == 0)
      {
	reserve(1);
	put1(177); // return
      }
    else
      emitTypedOp (172, popType().promote());
    setUnreachable();
  }

  /** Add an exception handler. */
  public void addHandler (int start_pc, int end_pc,
			  int handler_pc, int catch_type)
  {
    int index = 4 * exception_table_length;
    if (exception_table == null)
      {
	exception_table = new short[20];
      }
    else if (exception_table.length <= index)
      {
	short[] new_table = new short[2 * exception_table.length];
	System.arraycopy(exception_table, 0, new_table, 0, index);
	exception_table = new_table;
      }
    exception_table[index++] = (short) start_pc;
    exception_table[index++] = (short) end_pc;
    exception_table[index++] = (short) handler_pc;
    exception_table[index++] = (short) catch_type;
    exception_table_length++;
  }

  /** Add an exception handler. */
  public void addHandler (int start_pc, int end_pc, int handler_pc,
			  ClassType catch_type, ConstantPool constants)
  {
    int catch_type_index;
    if (catch_type == null)
      catch_type_index = 0;
    else
      catch_type_index = constants.addClass(catch_type).index;
    addHandler(start_pc, end_pc, handler_pc, catch_type_index);
  }


  public void emitTryStart(boolean has_finally, Type result_type)
  {
    Variable[] savedStack;
    if (SP > 0)
      {
	savedStack = new Variable[SP];
	int i = 0;
	while (SP > 0)
	  {
	    Variable var = addLocal(topType());
	    emitStore(var);
	    savedStack[i++] = var;
	  }
      }
    else
      savedStack = null;
    TryState try_state = new TryState(this);
    try_state.savedStack = savedStack;
    if (result_type != null && result_type.size == 0) // void
      result_type = null;
    if (result_type != null || SP > 0)
      {
	pushScope();
	if (result_type != null)
	  try_state.saved_result = addLocal(result_type);
      }
    if (has_finally)
      try_state.finally_subr = new Label(this);
  }

  public void emitTryEnd()
  {
    if (try_stack.end_label == null)
      {
	if (try_stack.saved_result != null)
	  emitStore(try_stack.saved_result);
	try_stack.end_label = new Label(this);
	if (reachableHere())
	  {
	    if (try_stack.finally_subr != null)
	      emitGoto(try_stack.finally_subr, 168);  // jsr
	    emitGoto(try_stack.end_label);
	  }
	readPC = PC;
	try_stack.end_pc = PC;
      }
  }

  public void emitCatchStart(Variable var)
  {
    emitTryEnd();
    SP = 0;
    if (try_stack.try_type != null)
      emitCatchEnd();
    ClassType type = var == null ? null : (ClassType) var.getType();
    try_stack.try_type = type;
    readPC = PC;
    addHandler(try_stack.start_pc, try_stack.end_pc,
	       PC, type, getConstants());
    if (var != null)
      {
	pushType(type);
	emitStore(var);
      }
    else
      pushType(Type.throwable_type);
  }

  public void emitCatchStart(ClassType type)
  {
    emitTryEnd();
    SP = 0;
    if (try_stack.try_type != null)
      emitCatchEnd();
    try_stack.try_type = type;
    readPC = PC;
    addHandler(try_stack.start_pc, try_stack.end_pc,
	       PC, type, getConstants());
    pushType(type);
  }

  public void emitCatchEnd()
  {
    if (reachableHere())
      {
	if (try_stack.saved_result != null)
	  emitStore(try_stack.saved_result);
	if (try_stack.finally_subr != null)
	  emitGoto(try_stack.finally_subr, 168); // jsr
	emitGoto(try_stack.end_label);
      }
    try_stack.try_type = null;
  }

  public void emitFinallyStart()
  {
    emitTryEnd();
    if (try_stack.try_type != null)
      emitCatchEnd();
    readPC = PC;
    SP = 0;
    try_stack.end_pc = PC;

    pushScope();
    Type except_type = Type.pointer_type;
    Variable except = addLocal(except_type);
    emitCatchStart((Variable) null);
    emitStore(except);
    emitGoto(try_stack.finally_subr, 168); // jsr
    emitLoad(except);
    emitThrow();
    
    try_stack.finally_subr.define(this);
    Type ret_addr_type = Type.pointer_type;
    try_stack.finally_ret_addr = addLocal(ret_addr_type);
    pushType(ret_addr_type);
    emitStore(try_stack.finally_ret_addr);
  }

  public void emitFinallyEnd()
  {
    emitRet(try_stack.finally_ret_addr);
    setUnreachable();
    popScope();
    try_stack.finally_subr = null;
  }

  public void emitTryCatchEnd()
  {
    if (try_stack.finally_subr != null)
      emitFinallyEnd();
    try_stack.end_label.define(this);
    Variable[] vars = try_stack.savedStack;
    if (vars != null)
      {
	for (int i = vars.length;  --i >= 0; )
	  {
	    Variable v = vars[i];
	    if (v != null) {
	      emitLoad(v);
	    }
	  }
      }
    if (try_stack.saved_result != null)
	emitLoad(try_stack.saved_result);
    if (try_stack.saved_result != null || vars != null)
	popScope();
    try_stack = try_stack.previous;
  }

  public final boolean isInTry()
  {
    // This also return true if we're in  a catch clause, but that is
    // good enough for now.
    return try_stack != null;
  }

  /** Compile a tail-call to position 0 of the current procewure.
   * @param pop_args if true, copy argument registers (except this) from stack.
   * @param scope Scope whose start we jump back to. */
  public void emitTailCall (boolean pop_args, Scope scope)
  {
    if (pop_args)
      {
	Method meth = getMethod();
	int arg_slots = ((meth.access_flags & Access.STATIC) != 0) ? 0 : 1;
	for (int i = meth.arg_types.length;  --i >= 0; )
	  arg_slots += meth.arg_types[i].size > 4 ? 2 : 1;
	for (int i = meth.arg_types.length;  --i >= 0; )
	  {
	    arg_slots -= meth.arg_types[i].size > 4 ? 2 : 1;
	    emitStore(locals.used [arg_slots]);
	  }
      }
    reserve(5);
    int start_pc = scope.start_pc;
    int delta = start_pc - PC;
    if (delta < -32768)
      {
	put1(200);  // goto_w
	put4(delta);
      }
    else
      {
	put1(167); // goto
	put2(delta);
      }
    setUnreachable();
  }

  /* Make sure the label with oldest fixup is first in labels. */
  void reorder_fixups ()
  {
    Label prev = null;
    Label cur;
    Label oldest = null;
    Label oldest_prev = null;
    int oldest_fixup = PC+100;
    for (cur = labels;  cur != null;  cur = cur.next)
      {
	if (cur.fixups != null && cur.fixups[0] < oldest_fixup)
	  {
	    oldest = cur;
	    oldest_prev = prev;
	    oldest_fixup = cur.fixups[0];
	  }
	prev = cur;
      }
    if (oldest != labels && oldest != null)
      {
	oldest_prev.next = oldest.next;
	oldest.next = labels;
	labels = oldest;
      }
  }

  public void finalize_labels ()
  {
    while (labels != null && labels.fixups != null)
      labels.emit_spring (this);
    for (Label label = labels;  label != null;  label = label.next)
      {
	if (label.fixups != null || label.wide_fixups != null)
	  throw new Error ("undefined label");
    }
  }

  public void assignConstants (ClassType cl)
  {
    super.assignConstants(cl);
    for (;;)
      {
	CodeFragment frag = fragments;
	if (frag == null)
	  break;
	fragments = frag.next;
	frag.emit(this);
      }
    if (locals != null && locals.container == null && ! locals.isEmpty())
      locals.addToFrontOf(this);
    Attribute.assignConstants(this, cl);
    finalize_labels();
  }

  public final int getLength()
  {
    return 12 + getCodeLength() + 8 * exception_table_length
      + Attribute.getLengthAll(this);
  }

  public void write (DataOutputStream dstr) throws java.io.IOException
  {
    dstr.writeShort (max_stack);
    dstr.writeShort (max_locals);
    dstr.writeInt (PC);
    dstr.write (code, 0, PC);

    dstr.writeShort (exception_table_length);
    int count = exception_table_length;
    for (int i = 0;  --count >= 0;  i += 4)
      {
	dstr.writeShort(exception_table[i]);
	dstr.writeShort(exception_table[i+1]);
	dstr.writeShort(exception_table[i+2]);
	dstr.writeShort(exception_table[i+3]);
      }

    Attribute.writeAll(this, dstr);
  }

  public void print (ClassTypeWriter dst) 
  {
    dst.print("Attribute \"");
    dst.print(getName());
    dst.print("\", length:");
    dst.print(getLength());
    dst.print(", max_stack:");
    dst.print(max_stack);
    dst.print(", max_locals:");
    dst.print(max_locals);
    dst.print(", code_length:");
    int length = getCodeLength();
    dst.println(length);
    disAssemble(dst, 0, length);
    if (exception_table_length > 0)
      {
	dst.print("Exceptions (count: ");
	dst.print(exception_table_length);
	dst.println("):");
	int count = exception_table_length;
	for (int i = 0;  --count >= 0;  i += 4)
	  {
	    dst.print("  start: ");
	    dst.print(exception_table[i] & 0xffff);
	    dst.print(", end: ");
	    dst.print(exception_table[i+1] & 0xffff);
	    dst.print(", handler: ");
	    dst.print(exception_table[i+2] & 0xffff);
	    dst.print(", type: ");
	    int catch_type_index = exception_table[i+3] & 0xffff;
	    if (catch_type_index == 0)
	      dst.print("0 /* finally */");
	    else
	      {
		dst.printOptionalIndex(catch_type_index);
		dst.printConstantTersely(catch_type_index, ConstantPool.CLASS);
	      }
	    dst.println();
	  }
      }
    dst.printAttributes(this);
  }

  public void disAssemble (ClassTypeWriter dst, int offset, int length)
  {
    boolean wide = false;
    for (int i = offset;  i < length; )
      {
	int oldpc = i++;
	int op = code[oldpc] & 0xff;
	String str = Integer.toString(oldpc);
	int printConstant = 0;
	int j = str.length();
	while (++j <= 3) dst.print(' ');
	dst.print(str);
	dst.print(": ");
	// We do a rough binary partition of the opcodes.
	if (op < 120)
	  {
	    if (op < 87)
	      {
		if (op < 3)  print("nop;aconst_null;iconst_m1;", op, dst);
		else if (op < 9) { dst.print("iconst_");  dst.print(op-3); }
		else if (op < 16) // op >= 9 [lconst_0] && op <= 15 [dconst_1]
		  {
		    char typ;
		    if (op < 11) { typ = 'l';  op -= 9; }
		    else if (op < 14) { typ = 'f';  op -= 11; }
		    else { typ = 'd';  op -= 14; }
		    dst.print(typ);  dst.print("const_");  dst.print(op);
		  }
		else if (op < 21)
		  {
		    if (op < 18)  // op >= 16 [bipush] && op <= 17 [sipush]
		      {
			print("bipush ;sipush ;", op-16, dst);
			int constant;
			if (op == 16)  constant = code[i++];
			else { constant = (short) readUnsignedShort(i);  i+=2;}
			dst.print(constant);
		      }
		    else // op >= 18 [ldc] && op <= 20 [ldc2_w]
		      {
			printConstant = op == 18 ? 1 : 2;
			print("ldc;ldc_w;ldc2_w;", op-18, dst);
		      }
		  }
		else // op >= 21 && op < 87
		  {
		    String load_or_store;
		    if (op < 54) { load_or_store = "load"; }
		    else { load_or_store = "store"; op -=(54-21); }
		    int index;  // -2 if array op;  -1 if index follows
		    if (op < 26) { index = -1; op -= 21; }
		    else if (op < 46) { op -= 26;  index = op % 4;  op >>= 2; }
		    else { index = -2; op -= 46; }
		    dst.print("ilfdabcs".charAt(op));
		    if (index == -2) dst.write('a');
		    dst.print(load_or_store);
		    if (index >= 0) { dst.write('_');  dst.print(index); }
		    else if (index == -1)
		      {
			if (wide) { index = readUnsignedShort(i); i += 2; }
			else { index = code[i] & 0xff; i++; }
			wide = false;
			dst.print(' ');
			dst.print(index);
		      }
		  }
	      }
	    else // op >= 87 && op < 120
	      {
		if (op < 96)
		  print("pop;pop2;dup;dup_x1;dup_x2;dup2;dup2_x1;dup2_x2;swap;"
			, op-87, dst);
		else // op >= 96 [iadd] && op <= 119 [dneg]
		  {
		    dst.print("ilfda".charAt((op-96) % 4));
		    print("add;sub;mul;div;rem;neg;", (op-96)>>2, dst);
		  }
	      }
	  }
	else // op >= 120
	  {
	    if (op < 170)
	      {
		if (op < 132) // op >= 120 [ishl] && op <= 131 [lxor]
		  {
		    dst.print((op & 1) == 0 ? 'i' : 'l');
		    print("shl;shr;ushr;and;or;xor;", (op-120)>>1, dst);
		  }
		else if (op == 132) // iinc
		  {
		    int var_index;
		    int constant;
		    dst.print("iinc");
		    if (! wide)
		      {
			var_index = 0xff & code[i++];
			constant = code[i++];
		      }
		    else
		      {
			var_index = readUnsignedShort(i);
			i += 2;
			constant = (short) readUnsignedShort(i);
			i += 2;
			wide = false;
		      }
		    dst.print(' ');  dst.print(var_index);
		    dst.print(' ');  dst.print(constant);
		  }
		else if (op < 148) // op >= 133 [i2l] && op <= 147 [i2s]
		  {
		    dst.print("ilfdi".charAt((op-133) / 3));
		    dst.print('2');
		    dst.print("lfdifdildilfbcs".charAt(op-133));
		  }
		else if (op < 153) // op >= 148 [lcmp] && op <= 152 [dcmpg]
		  print("lcmp;fcmpl;fcmpg;dcmpl;dcmpg;", op-148, dst);
		else if (op < 169)
		  {
		    if (op < 159)
		      {
			dst.print("if");
			print("eq;ne;lt;ge;gt;le;", op-153, dst);
		      }
		    else if (op < 167)
		      {
			if (op < 165) { dst.print("if_icmp"); }
			else { dst.print("if_acmp"); op -= 165-159; }
			print("eq;ne;lt;ge;gt;le;", op-159, dst);
		      }
		    else
		      print("goto;jsr;", op-167, dst);
		    int delta = (short) readUnsignedShort(i);
		    i += 2;
		    dst.print(' ');  dst.print(oldpc+delta);
		  }
		else
		  {
		    int index;
		    dst.print("ret ");
		    if (wide) { index = readUnsignedShort(i); index += 2; }
		    else { index = code[i] & 0xff; i++; }
		    wide = false;
		    dst.print(index);
		  }
	      }
	    else
	      {
		if (op < 172) //  [tableswitch] or [lookupswitch]
		  {
		    i = (i + 3) & ~3; // skip 0-3 byte padding.
		    int code_offset = readInt(i);  i += 4;
		    if (op == 170)
		      {
			dst.print("tableswitch");
			int low = readInt(i);  i += 4;
			int high = readInt(i);  i += 4;
			dst.print(" low: "); dst.print(low);
			dst.print(" high: "); dst.print(high);
			dst.print(" default: "); dst.print(oldpc+code_offset);
			for (;  low <= high;  low++)
			  {
			    code_offset = readInt(i);  i += 4;
			    dst.println();
			    dst.print("  ");  dst.print(low);
			    dst.print(": ");  dst.print(oldpc + code_offset); 
			  }
		      }
		    else
		      {
			dst.print("lookupswitch");
			int npairs = readInt(i);  i += 4;
			dst.print(" npairs: "); dst.print(npairs);
			dst.print(" default: "); dst.print(oldpc+code_offset);
			while (--npairs >= 0)
			  {
			    int match = readInt(i);  i += 4;
			    code_offset = readInt(i);  i += 4;
			    dst.println();
			    dst.print("  ");  dst.print(match);
			    dst.print(": ");  dst.print(oldpc + code_offset); 
			  }
		      }
		  }
		else if (op < 178) // op >= 172 [ireturn] && op <= 177 [return]
		  {
		    if (op < 177) dst.print("ilfda".charAt(op-172));
		    dst.print("return");
		  }
		else if (op < 182) // op >= 178 [getstatic] && op <= 181 [putfield]
		  {
		    print("getstatic;putstatic;getfield;putfield;", op-178, dst);
		    printConstant = 2;
		  }
		else if (op < 185) // op >= 182 && op <= 185 [invoke*]
		  {
		    dst.print("invoke");
		    print("virtual;special;static;", op-182, dst);
		    printConstant = 2;
		  }
		else if (op == 185) // invokeinterface
		  {
		    dst.print("invokeinterface (");
		    int index = readUnsignedShort(i);
		    i += 2;
		    int args = 0xff & code[i];
		    i += 2;
		    dst.print(args + " args)");
		    dst.printConstantOperand(index);
		  }
		else if (op < 196)
		  {
		    print("186;new;newarray;anewarray;arraylength;athrow;checkcast;instanceof;monitorenter;monitorexit;", op-186, dst);
		    if (op == 187 || op == 189 || op == 192 || op == 193)
		      printConstant = 2;
		    else if (op == 188)  // newarray
		      {
			int type = code[i++];
			dst.print(' ');
			if (type >= 4 && type <= 11)
			  print("boolean;char;float;double;byte;short;int;long;",
				type-4, dst);
			else
			  dst.print(type);
		      }
			
		  }
		else if (op == 196) // wide
		  {
		    dst.print("wide");
		    wide = true;
		  }
		else if (op == 197)
		  {
		    dst.print("multianewarray");
		    int index = readUnsignedShort(i);
		    i += 2;
		    dst.printConstantOperand(index);
		    int dims = 0xff & code[i++];
		    dst.print(' ');
		    dst.print(dims);
		  }
		else if (op < 200)
		  {
		    print("ifnull;ifnonnull;", op-198, dst);
		    int delta = (short) readUnsignedShort(i);
		    i += 2;
		    dst.print(' ');  dst.print(oldpc+delta);
		  }
		else if (op < 202)
		  {
		    print("goto_w;jsr_w;", op-200, dst);
		    int delta = readInt(i);
		    i += 4;
		    dst.print(' ');  dst.print(oldpc+delta);
		  }
		else
		  dst.print(op);
	      }
	  }
	if (printConstant > 0)
	  {
	    int index;
	    if (printConstant == 1) index = 0xff & code[i++];
	    else { index = readUnsignedShort(i);  i += 2; }
	    dst.printConstantOperand(index);
	  }
	dst.println();
      }
  }

  private int readUnsignedShort(int offset)
  {
    return ((0xff & code[offset]) << 8) | (0xff & code[offset+1]);
  }

  private int readInt(int offset)
  {
    return (readUnsignedShort(offset) << 16) | readUnsignedShort(offset+2);
  }

  /*
  public saveStack (ClassType into)
  {
    Field[] flds = new Field[SP];
    while (SP > 0)
      {
	Field fld = ?;
	emitStore(fld);
	flds[SP...]
      }
  }
  */

  /* Print the i'th ';'-delimited substring of str on dst. */
  private void print (String str, int i, PrintWriter dst)
  {
    int last = 0;
    int pos = -1;
    for (; i >= 0; i--)
      {
	last = ++pos;
	pos = str.indexOf(';', last);
      }
    dst.write(str, last, pos-last);
  }

  /** Return an object encapsulating the type state of the JVM stack. */
  public Type[] saveStackTypeState(boolean clear)
  {
    if (SP == 0)
      return null;
    Type[] typeState = new Type[SP];
    System.arraycopy(stack_types, 0, typeState, 0, SP);
    if (clear)
      SP = 0;
    return typeState;
  }

  /** Restore a type state as saved by saveStackTypeState. */
  public void restoreStackTypeState (Type[] save)
  {
    if (save == null)
      SP = 0;
    else
      {
	SP = save.length;
	System.arraycopy(save, 0, stack_types, 0, SP);
      }
  }

  CodeFragment fragmentStack = null;

  public void beginFragment(boolean isHandler)
  {
    CodeFragment frag = new CodeFragment(this);
    frag.next = fragmentStack;
    fragmentStack = frag;
    frag.length = PC;
    frag.unreachable_save = unreachable_here;
    unreachable_here = false;
    if (isHandler)
      frag.handlerIndex = exception_table_length - 1;
  }

  public void endFragment()
  {
    CodeFragment frag = fragmentStack;
    fragmentStack = frag.next;

    frag.next = fragments;
    fragments = frag;
    int startPC = frag.length;
    frag.length = PC - startPC;
    frag.insns = new byte[frag.length];
    System.arraycopy(code, startPC, frag.insns, 0, frag.length);
    PC = startPC;
    unreachable_here = frag.unreachable_save;

    if (lines != null)
      {
	int l = 2 * lines.linenumber_count;
	int j = l;
	short[] linenumbers = lines.linenumber_table;
	while (j > 0 && linenumbers[j - 2] >= startPC)
	  j -= 2;
	l -= j;
	if (l > 0)
	  {
	    short[] fraglines = new short[l];
	    for (int i = 0;  i < l;  i += 2)
	      {
		fraglines[i] = (short) ((linenumbers[j+i] & 0xffff) - startPC);
		fraglines[i+1] = linenumbers[j+i+1];
	      }
	    frag.linenumbers = fraglines;
	    lines.linenumber_count = j >> 1;
	  }
      }
  }

  /****************************************************************
   * Postcondition
   ****************************************************************/

  public void preparePostcondition(Field assertionEnabled, boolean hasPostCond)
  {
    this.assertionEnabled = assertionEnabled;
    if (hasPostCond)
      postconditionStart = new Label(this);
  }

  public void startPrecondition()
  {
    preconditionEnd = new Label(this);
    ifAssertionsDisabledGoto(assertionEnabled, preconditionEnd);
  }

  public void endPrecondition()
  {
    preconditionEnd.define(this);
  }

  public void startPostcondition()
  {
    postconditionEnd = new Label(this);
    postconditionStart.define(this);
    Type type = getMethod().getReturnType();
    if (! type.isVoid())
    {
      // We expect the returned value on the stack.
      pushType(type);
      ifAssertionsDisabledGoto(assertionEnabled, postconditionEnd);
      resultVar = addLocal(type);
      emitStore(resultVar);
    }
    else
      ifAssertionsDisabledGoto(assertionEnabled, postconditionEnd);
  }
  
  public void pushRetType(){
    Type type = getMethod().getReturnType();
    if (! type.isVoid())
      pushType(type);
  }


  public void endPostcondition()
  {
    if (resultVar != null)
      loadResult();
    postconditionEnd.define(this);
    emitRealReturn();
  }

  public void loadResult()
  {
    emitLoad(resultVar);
  }

  boolean checkPostcondition()
  {
    if (postconditionStart == null)
      return false;
    emitGoto(postconditionStart);
    return true;
  }

  public void ifAssertionsDisabledGoto(Field assertionEnabled, Label l)
  {
    emitGetStatic(assertionEnabled);
    emitGotoIfIntEqZero(l);
  }

  private Label postconditionStart, postconditionEnd, preconditionEnd;
  private Variable resultVar;
  private Field assertionEnabled;
}
