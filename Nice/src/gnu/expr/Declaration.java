package gnu.expr;
import gnu.bytecode.*;
import gnu.mapping.Named;

/**
 * The static information associated with a local variable binding.
 * <p>
 * These are the kinds of Declaration we use:
 * <p>
 * A local variable that is not captured by an inner lambda is stored
 * in a Java local variables slot (register).  The predicate isSimple ()
 * is true, and offset is the number of the local variable slot.
 * <p>
 * If a local variable is captured by an inner lambda, the
 * variable is stored in a field of the LambdaExp's heapFrame variable.
 * (The latter declaration has isSimple and isArtificial true.)
 * The Declaration's field specifies the Field used.
 *
 * If a function takes a fixed number of parameters, at most four,
 * then the arguments are passed in Java registers 1..4.
 * If a parameter is not captured by an inner lambda, the parameter
 * has the flags isSimple and isParameter true.
 * <p>
 * A parameter named "foo" that is captured by an inner lambda is represented
 * using two Declarations, named "foo" and "fooIncoming".
 * The "fooIncoming" declaration is the actual parameter as passed
 * by the caller using a Java local variable slot.  It has isParameter(),
 * isSimple(), and isArtificial set.  The "foo" Declaration has isParameter()
 * set.  The procedure prologue copies "fooIncoming" to "foo", which acts
 * just like a normal captured local variable.
 * <p>
 * If a function takes more than 4 or a variable number of parameters,
 * the arguments are passed in an array (using the applyN virtual method).
 * This array is referenced by the argsArray declaration, which has
 * isSimple(), isParameter(), and isArtificial() true, and its offset is 1.
 * The parameters are copied into the program-named variables by the
 * procedure prologue, so the parameters henceforth act like local variables.
 *
 * @author	Per Bothner
 */

public class Declaration
{
  static int counter;
  /** Unique id number, to ease print-outs and debugging. */
  protected int id = ++counter;

  /** The (interned) name of the new variable.
   * This is the source-level (non-mangled) name. */
  String name;

  public ScopeExp context;

  protected Type type;
  public final Type getType() { return type; }
  public final void setType(Type type)
  { this.type = type;  if (var != null) var.setType(type); }
  public final String getName() { return name; }

  /* Declarations in a ScopeExp are linked together in a linked list. */
  Declaration next;

  public final Declaration nextDecl() { return next; }

  Variable var;
  public Variable getVariable() { return var; }

  public final boolean isSimple()
  { return (flags & IS_SIMPLE) != 0; }

  public final void setSimple(boolean b)
  {
    setFlag(b, IS_SIMPLE);
    if (var != null) var.setSimple(b);
  }

  /** Return the ScopeExp that contains (declares) this Declaration. */
  public final ScopeExp getContext() { return context; }

  /** Used to link Declarations in a LambdaExp's capturedVars list. */
  Declaration nextCapturedVar;

  /** If non-null, field is relative to base.
   * If IS_FLUID, base points to IS_UNKNOWN Binding. */
  public Declaration base;

  public Field field;

  /** If this is a field in some object, load a reference to that object. */
  public void loadOwningObject (Compilation comp)
  {
    if (base != null)
      base.load(comp);
    else
      getContext().currentLambda().loadHeapFrame(comp);
  }

  public void load (Compilation comp)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    if (field == null && getFlag(STATIC_SPECIFIED) && value instanceof LambdaExp)
      {
	LambdaExp lambda = (LambdaExp) value;
	lambda.flags |= LambdaExp.NO_FIELD;
	lambda.compileAsMethod(comp);
	comp.topLambda.addApplyMethod(lambda);
	makeField(comp, value);
      }

    if (field != null)
      {
        if (! field.getStaticFlag())
          {
            loadOwningObject(comp);
            code.emitGetField(field);
          }
        else
          code.emitGetStatic(field);
      }
    else
      {
	Variable var = getVariable();
	if (context instanceof ClassExp && var == null && ! getFlag(PROCEDURE))
	  {
	    ClassExp cl = (ClassExp) context;
	    if (cl.isMakingClassPair())
	      {
		String getName = ClassExp.slotToMethodName("get", getName());
		Method getter = cl.type.getDeclaredMethod(getName, 0);
		cl.loadHeapFrame(comp);
		code.emitInvoke(getter);
		return;
	      }
	  }
	if (var == null)
	  {
	    var = allocateVariable(code);
	  }
	code.emitLoad(var);
      }
  }

  /* Compile code to store a value (which must already be on the
     stack) into this variable. */
  public void compileStore (Compilation comp)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    if (isSimple ())
      code.emitStore(getVariable());
    else
      {
        if (! field.getStaticFlag())
          {
            loadOwningObject(comp);
            code.emitSwap();
            code.emitPutField(field);
          }
        else
          code.emitPutStatic(field);          
      }
  }

  /** If non-null, the single expression used to set this variable.
   * If the variable can be set more than once, then value is null. */
  protected Expression value = QuoteExp.undefined_exp;

  public final Expression getValue() { return value; }

  /** If getValue() is a constant, return the constant value, otherwise null. */
  public final Object getConstantValue()
  {
    if (! (value instanceof QuoteExp))
      return null;
    return ((QuoteExp) value).getValue();
  }

  static final int INDIRECT_BINDING = 1;
  static final int CAN_READ = 2;
  static final int CAN_CALL = 4;
  static final int CAN_WRITE = 8;
  static final int IS_FLUID = 0x10;
  static final int PRIVATE = 0x20;
  static final int IS_SIMPLE = 0x40;
  static final int PROCEDURE = 0x80;
  static final int IS_ALIAS = 0x100;
  /** Set if this is just a declaration, not a definition. */
  public static final int NOT_DEFINING = 0x200;
  public static final int EXPORT_SPECIFIED = 0x400;
  public static final int STATIC_SPECIFIED = 0x800;
  public static final int NONSTATIC_SPECIFIED = 0x1000;
  public static final int TYPE_SPECIFIED = 0x2000;
  public static final int IS_CONSTANT = 0x4000;
  public static final int IS_SYNTAX = 0x8000;
  public static final int IS_UNKNOWN = 0x10000;
  public static final int PRIVATE_SPECIFIED = 0x20000;

  // This should be a type property, not a variable property, at some point!
  public static final int IS_SINGLE_VALUE = 0x40000;

  public static final int TRANSIENT = 0x80000;
  public static final int VOLATILE  = 0x100000;

  public static final int NOT_INITIALIZED = 0x200000;

  protected int flags = IS_SIMPLE;

  public final boolean getFlag (int flag)
  {
    return (flags & flag) != 0;
  }

  public final void setFlag (boolean setting, int flag)
  {
    if (setting) flags |= flag;
    else flags &= ~flag;
  }

  public final void setFlag (int flag)
  {
    flags |= flag;
  }

  public final boolean isPublic()
  { return context instanceof ModuleExp && (flags & PRIVATE) == 0; }

  public final boolean isPrivate() { return (flags & PRIVATE) != 0; }

  public final void setPrivate(boolean isPrivate)
  {
    setFlag(isPrivate, PRIVATE);
  }

  public final boolean isSpecifiedPrivate() { return (flags & PRIVATE_SPECIFIED) != 0; }

  public final void setSpecifiedPrivate(boolean isPrivate)
  {
    setFlag(isPrivate, PRIVATE_SPECIFIED);
  }

  public final boolean isAlias() { return (flags & IS_ALIAS) != 0; }
  public final void setAlias(boolean flag) { setFlag(flag, IS_ALIAS); }

  /** True if this is a fluid binding (in a FluidLetExp). */
  public final boolean isFluid () { return (flags & IS_FLUID) != 0; }

  public final void setFluid (boolean fluid) { setFlag(fluid, IS_FLUID); }

  public final boolean isProcedureDecl () { return (flags & PROCEDURE) != 0; }

  public final void setProcedureDecl (boolean val) { setFlag(val, PROCEDURE); }

  /** True if the value of the variable is the contents of a Binding. */
  public final boolean isIndirectBinding()
  { return (flags & INDIRECT_BINDING) != 0; }
  public final void setIndirectBinding(boolean indirectBinding)
  {
    setFlag(indirectBinding, INDIRECT_BINDING);
  }

  /* Note:  You probably want to use !ignorable(). */
  public final boolean getCanRead() { return (flags & CAN_READ) != 0; }
  public final void setCanRead(boolean read)
  {
    setFlag(read, CAN_READ);
  }
  public final void setCanRead()
  {
    setFlag(true, CAN_READ);
    if (base != null)
      base.setCanRead();
  }

  public final boolean getCanCall() { return (flags & CAN_CALL) != 0; }
  public final void setCanCall(boolean called) { setFlag(called, CAN_CALL); }
  public final void setCanCall()
  {
    setFlag(true, CAN_CALL);
    if (base != null)
      base.setCanRead();
  }

  public final boolean getCanWrite()
  { return (flags & CAN_WRITE) != 0; }

  public final void setCanWrite(boolean written)
  {
    if (written) flags |= CAN_WRITE;
    else flags &= ~CAN_WRITE;
  }

  public final void setCanWrite()
  {
    flags |= CAN_WRITE;
    if (base != null)
      base.setCanRead();
  }

  public void setName(String name)
  {
    this.name = name;
  }

  /** True if we never need to access this declaration. */
  // rename to isAccessed?
  public boolean ignorable()
  {
    if (getCanRead() || isPublic())
      return false;
    if (getCanWrite() && getFlag(IS_UNKNOWN))
      return false;
    if (! getCanCall())
      return true;
    Expression value = getValue();
    if (value == null || ! (value instanceof LambdaExp))
      return false;
    LambdaExp lexp = (LambdaExp) value;
    return ! lexp.isHandlingTailCalls() || lexp.getInlineOnly();
  }

  /** Does this variable need to be initialized or is default ok
   */
  public boolean needsInit()
  {
    // This is a kludge.  Ideally, we should do some data-flow analysis.
    // But at least it makes sure require'd variables are not initialized.
    return ! getFlag(NOT_INITIALIZED)
      && ! ignorable()
      && ! (value == QuoteExp.nullExp && base != null);
  }

  public boolean isStatic()
  {
    return (getFlag(STATIC_SPECIFIED)
	    || (context instanceof ModuleExp
		&& ((ModuleExp) context).isStatic()));
  }

  public final boolean isLexical()
  {
    return ! isFluid() && ! isStatic();
  }

  /** List of ApplyExp where this declaration is the function called.
   * The applications are chained using their nextcall fields. */
  public ApplyExp firstCall;

  public void noteValue (Expression value)
  {
    // We allow assigning a real value after undefined ...
    if (this.value == QuoteExp.undefined_exp)
      {
	if (value instanceof LambdaExp)
	  ((LambdaExp) value).nameDecl = this;
	this.value = value;
      }
    else if (this.value != value)
      {
	if (this.value instanceof LambdaExp) 
	  ((LambdaExp) this.value).nameDecl = null;
	this.value = null;
      }
  }

  protected Declaration()
  {
  }

  public Declaration (String name)
  {
    this(name, Type.pointer_type);
  }

  public Declaration (String s, Type type)
  {
    name = s;
    setType(type);
  }

  public Declaration (String name, Field field)
  {
    this.name = name;
    setType(field.getType());
    this.field = field;
    setSimple(false);
  }

  Method makeBindingMethod = null;

  /** Create a Binding object, given that isIndirectBinding().
      Assume the initial value is already pushed on the stack;
      leaves initialized Binding object on stack.  */
  public void pushIndirectBinding (Compilation comp)
  {
    CodeAttr code = comp.getCode();
    code.emitPushString(getName());
    if (makeBindingMethod == null)
      {
	Type[] args = new Type[2];
	args[0] = Type.pointer_type;
	args[1] = Type.string_type;
	makeBindingMethod
	  = Compilation.typeBinding.addMethod("make", args,
					      Compilation.typeBinding,
					      Access.PUBLIC|Access.STATIC);
      }
    code.emitInvokeStatic(makeBindingMethod);
  }

  public final Variable allocateVariable(CodeAttr code)
  {
    if (! isSimple())
      return null;
    if (var == null)
      {
        String vname = null;
        if (name != null)
          vname = Compilation.mangleName(getName());
	if (isAlias() && getValue() instanceof ReferenceExp)
	  {
	    Declaration base = followAliases(this);
	    var = base == null ? null : base.var;
	  }
	else
	  {
	    Type type = isIndirectBinding() ? Compilation.typeLocation
	      : getType();
	    var = context.scope.addVariable(code, type, vname);
	  }
      }
    return var;
  }

  /** Generate code to initialize the location for this.
      Assume the initial value is already pushed on the stack. */
  public void initBinding (Compilation comp)
  {
    if (isIndirectBinding())
      {
	pushIndirectBinding(comp);
	CodeAttr code = comp.getCode();
	code.emitStore(getVariable());
      }
    else
      compileStore(comp);
  }

  String filename;
  int position;

  public final void setFile (String filename)
  {
    this.filename = filename;
  }

  public final void setLine (int lineno, int colno)
  {
    position = (lineno << 12) + colno;
  }

  public final void setLine (int lineno)
  {
    setLine (lineno, 0);
  }

  public final String getFile ()
  {
    return filename;
  }

  /** Get the line number of (the start of) this Expression.
    * The "first" line is line 1. */
  public final int getLine ()
  {
    return position >> 12;
  }

  public final int getColumn ()
  {
    return position & ((1 << 12) - 1);
  }


  public String toString()
  {
    return "Declaration["+getName()+'/'+id+']';
  }


  public static Declaration followAliases (Declaration decl)
  {
    while (decl != null && decl.isAlias())
      {
	Expression declValue = decl.getValue();
	if (! (declValue instanceof ReferenceExp))
	  break;
	ReferenceExp rexp = (ReferenceExp) declValue;
	if (rexp.binding == null)
	  break;
	decl = rexp.binding;
      }
    return decl;
  }

  public void makeField(Compilation comp, Expression value)
  {
    setSimple(false);
    String fname = getName();
    if (getFlag(IS_UNKNOWN))
      fname = "id$" + fname;
    fname = Compilation.mangleNameIfNeeded(fname);
    int fflags = 0;
    boolean isConstant = getFlag(IS_CONSTANT);
    boolean typeSpecified = getFlag(TYPE_SPECIFIED);
    if (isPublic() && ! isConstant && ! typeSpecified)
      setIndirectBinding(true);
    if (isIndirectBinding() || isConstant)
      fflags |= Access.FINAL;
    if (! isPrivate())
      fflags |= Access.PUBLIC;
    if (getFlag(STATIC_SPECIFIED)
	|| (isConstant && value instanceof QuoteExp))
      fflags |= Access.STATIC;
    if (getFlag(IS_UNKNOWN))
      {
	// This is a kludge.  We really should set STATIC only if the module
	// is static, in which case it should be safe to make it always FINAL.
	// But for now we don't support non-static UNKNOWNs.  FIXME.
	fflags |= Access.STATIC;
	if (! (context instanceof ModuleExp
	       && ((ModuleExp) context).isStatic()))
	  fflags &= ~Access.FINAL;
      }
    Type ftype = (isIndirectBinding() ? Compilation.typeBinding
		  : value == null || typeSpecified ? getType()
		  : value.getType());
    
    field = comp.topClass.addField (fname, ftype, fflags);
    setFieldValue(comp);
  }

  void setFieldValue(Compilation comp)
  {
    Type ftype = field.getType();
    if (value instanceof QuoteExp)
      {
	Object val = ((QuoteExp) value).getValue();
	if (val != null && val.getClass().getName().equals(ftype.getName()))
	  {
	    Literal literal = comp.findLiteral(val);
	    if (literal.field == null)
	      literal.assign(field, comp);
	  }
      }
    if (value instanceof QuoteExp
	&& (ftype instanceof PrimType
	    || "java.lang.String".equals(ftype.getName())))
      {
	Object val = ((QuoteExp) value).getValue();
	if (val != null)
	  field.setConstantValue(val, field.getDeclaringClass());
      }
    else if ((isIndirectBinding() || value != null)
	     && ! getFlag(IS_UNKNOWN))
      {
	BindingInitializer init = new BindingInitializer(this, field, value);
	if (field.getStaticFlag())
	  {
	    init.next = comp.topLambda.clinitChain;
	    comp.topLambda.clinitChain = init;
	  }
	else
	  {
	    init.next = comp.mainLambda.initChain; // FIXME why mainLambda?
	    comp.mainLambda.initChain = init;
	  }
      }
  }

  public static Declaration getDeclaration(Named proc)
  {
    return getDeclaration(proc, proc.getName());
  }

  public static Declaration getDeclaration(Object proc, String name)
  {
    if (name != null)
      {
        Class procClass = PrimProcedure.getProcedureClass(proc);
        if (procClass != null)
          {
            ClassType procType = (ClassType) Type.make(procClass);
            String fname = Compilation.mangleName(name);
            gnu.bytecode.Field procField = procType.getDeclaredField(fname);
            if (procField != null)
              {
                int fflags = procField.getModifiers();
                if ((fflags & Access.STATIC) != 0)
                  {
                    Declaration decl = new Declaration(name, procField);
                    decl.noteValue(new QuoteExp(proc));
                    if ((fflags & Access.FINAL) != 0)
                      decl.setFlag(Declaration.IS_CONSTANT);
                    return decl;
                  }
              }
          }
      }
    return null;
  }
}
