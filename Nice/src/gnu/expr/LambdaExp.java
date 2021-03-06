// Copyright (c) 1999, 2000, 2001  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.expr;
import gnu.bytecode.*;
import gnu.mapping.*;
import java.util.Vector;

/**
 * Class used to implement Scheme lambda expressions.
 * @author	Per Bothner
 */

public class LambdaExp extends ScopeExp
{
  public String name;
  public Expression body;
  public int min_args;
  // Maximum number of actual arguments;  -1 if variable.
  public int max_args;

  /** Set of visible top-level LambdaExps that need apply methods. */
  Vector applyMethods;

  //  public int plainArgs;
  Variable argsArray;
  // First argument that goes into argsArray.
  private Declaration firstArgsArrayArg;

  public Keyword[] keywords;
  public Expression[] defaultArgs;

  public java.util.Stack[] parameterCopies;

  static int counter;
  /** Unique id number, to ease print-outs and debugging. */
  int id = ++counter;

  /** A list of Declarations, chained using Declaration's nextCapturedVar.
    * All the Declarations are allocated in the current heapFrame. */
  Declaration capturedVars;

  /** A local variable that points to the heap-allocated part of the frame.
   * Each captured variable is a field in the heapFrame.  A procedure has
   * a heapFrame iff it has a parameter or local variable that is
   * referenced ("captured") by a non-inline inferior procedure.
   * (I.e there is a least one non-inline procedure that encloses the
   * reference but not the definition.)  Note that an inline procedure may
   * have a heapFrame if it encloses a non-inline procedure.  This is
   * necessary because we represent loops as tail-recursive inline procedures.
   */
  Variable heapFrame;

  public LambdaExp firstChild;
  public LambdaExp nextSibling;

  /** A magic value to indicate there is no unique return continuation. */
  final static ApplyExp unknownContinuation = new ApplyExp ((Expression) null, null);

  /** The unique caller that calls this lambda.
      The value is null, if no callers have been seen.
      A value of unknownContinuation means there are multiple call sites.
      Tail-recursive calls do not count as multiple call sites. (With a
      little more analysis, we could also allow multiple non-self tail-calls
      as long as they all are ultimately called from the same place.)
      This is used to see if we can inline the function at its unique
      call site. */
  public ApplyExp returnContinuation;

  public void forceGeneration()
  {
    returnContinuation = unknownContinuation;
  }

  /** If non-null, a Declaration whose value is (only) this LambdaExp. */
  public Declaration nameDecl;

  /** If non-null, this is a Field that is used for implementing lexical closures.
   * If getName() is "closureEnv", it is our parent's heapFrame,
   * which is an instance of one of our siblings.
   * (Otherwise, we use "this" as the implicit "closureEnv" field.) */
  public Field closureEnvField;

  /** Field in heapFrame.getType() that contains the static link.
   * It is used by child functions to get to outer environments.
   * Its value is this function's closureEnv value. */
  public Field staticLinkField;

  /** A variable that points to the closure environment passed in.
   * It can be any one of:
   * null, if no closure environment is needed;
   * this, if this object is its parent's heapFrame;
   * a local variable initialized from this.closureEnv;
   * a parameter (only if !getCanRead()); or
   * a copy of our caller's closureEnv or heapFrame (only if getInlineOnly()).
   * See declareClosureEnv and closureEnvField. */
  Variable closureEnv;

  static final int INLINE_ONLY = 1;
  static final int CAN_READ = 2;
  static final int CAN_CALL = 4;
  static final int IMPORTS_LEX_VARS = 8;
  static final int NEEDS_STATIC_LINK = 16;
  /* Used (future) by FindTailCalls. */
  static final int CANNOT_INLINE = 32;
  static final int CLASS_METHOD = 64;
  static final int METHODS_COMPILED = 128;
  static final int NO_FIELD = 256;
  static final int DEFAULT_CAPTURES_ARG = 512;
  public static final int SEQUENCE_RESULT = 1024;
  protected static final int NEXT_AVAIL_FLAG = 2048;

  /** True iff this lambda is only "called" inline. */
  public final boolean getInlineOnly() { return (flags & INLINE_ONLY) != 0; }
  public final void setInlineOnly(boolean inlineOnly)
  { setFlag(inlineOnly, INLINE_ONLY); }

  public final boolean getNeedsClosureEnv ()
  { return (flags & (NEEDS_STATIC_LINK|IMPORTS_LEX_VARS)) != 0; }

  /** True if a child lambda uses lexical variables from outside.
      Hence, a child heapFrame needs a staticLink to outer frames. */
  public final boolean getNeedsStaticLink ()
  { return (flags & NEEDS_STATIC_LINK) != 0; }

  public final void setNeedsStaticLink(boolean needsStaticLink)
  {
    if (needsStaticLink) flags |= NEEDS_STATIC_LINK;
    else flags &= ~NEEDS_STATIC_LINK;
  }

  /** True iff this lambda "captures" (uses) lexical variables from outside. */
  public final boolean getImportsLexVars ()
  { return (flags & IMPORTS_LEX_VARS) != 0; }

  public final void setImportsLexVars(boolean importsLexVars)
  {
    if (importsLexVars) flags |= IMPORTS_LEX_VARS;
    else flags &= ~IMPORTS_LEX_VARS;
  }

  public final void setImportsLexVars()
  {
    int old = flags;
    flags |= IMPORTS_LEX_VARS;

    // If this needs an environment (closure), then its callers do too.
    if ((old & IMPORTS_LEX_VARS) == 0 && nameDecl != null)
      setCallersNeedStaticLink();
  }

  public final void setNeedsStaticLink()
  {
    int old = flags;
    flags |= NEEDS_STATIC_LINK;

    // If this needs an environment (closure), then its callers do too.
    if ((old & NEEDS_STATIC_LINK) == 0 && nameDecl != null)
      setCallersNeedStaticLink();
  }

  void setCallersNeedStaticLink()
  {
    LambdaExp outer = outerLambda();
    for (ApplyExp app = nameDecl.firstCall;  app != null;  app = app.nextCall)
      {
        LambdaExp caller = app.context;
        for (; caller != outer; caller = caller.outerLambda())
          caller.setNeedsStaticLink();
      }
  }

  public final boolean getCanRead()
  { return (flags & CAN_READ) != 0; }
  public final void setCanRead(boolean read)
  {
    if (read) flags |= CAN_READ; 
    else flags &= ~CAN_READ;
  }

  public final boolean getCanCall()
  { return (flags & CAN_CALL) != 0; }
  public final void setCanCall(boolean called)
  {
    if (called) flags |= CAN_CALL;
    else flags &= ~CAN_CALL;
  }

  /** True if this is a method in an ClassExp. */
  public final boolean isClassMethod()
  { return (flags & CLASS_METHOD) != 0; }

  public final void setClassMethod(boolean isMethod)
  {
    if (isMethod) flags |= CLASS_METHOD;
    else flags &= ~CLASS_METHOD;
  }
  /** The name to give to a dummy implicit function that surrounds a file. */
  public static String fileFunctionName = "atFileLevel";

  /** True iff this is the dummy top-level function of a module body. */
  public final boolean isModuleBody () { return this instanceof ModuleExp; }

  /** True if a class is generated for this procedure.
   * We don't need a class if this is only called inline.
   * We also don't need a class if all callers are known, and we can
   * invoke a method for this procedure.
   * However, the last optimization is not available when using tail calls.
   */
  public final boolean isClassGenerated ()
  {
    return (! getInlineOnly()
	    && (isModuleBody() || this instanceof ClassExp));
  }

  public final boolean isHandlingTailCalls ()
  {
    return (isModuleBody() && ! ((ModuleExp) this).isStatic())
      || (Compilation.usingTailCalls && ! isModuleBody() && ! isClassMethod());
  }

  public final boolean variable_args () { return max_args < 0; }

  ClassType type = Compilation.typeProcedure;

  /** Return the ClassType of the Procedure this is being compiled into. */
  public ClassType getCompiledClassType(Compilation comp)
  {
    if (type == Compilation.typeProcedure)
      throw new Error("internal error: getCompiledClassType");
    return type;
  }

  public Type getType()
  {
    return type;
  }

  public Type[] getArgTypes()
  {
    Type[] res = new Type[max_args];
    int n = 0;
    for (Declaration d = firstDecl(); d != null; d = d.nextDecl())
      res[n++] = d.getType();
    return res;
  }


  /** Number of argument variable actually passed by the caller.
   * For functions that accept more than 4 argument, or take a variable number,
   * this is 1, since in that all arguments are passed in a single array. */
  public int incomingArgs ()
  {
    // The max_args > 0 is a hack to handle LambdaProecdure, which
    // currently always uses a single array argument.
    return min_args == max_args && max_args <= 4 && max_args > 0 ? max_args : 1;
  }

  /** If non-zero, the selector field of the ModuleMethod for this. */
  int selectorValue;

  int getSelectorValue(Compilation comp)
  {
    if (selectorValue == 0)
      selectorValue = ++comp.maxSelectorValue;
    return selectorValue;
  }

  /** Methods used to implement this functions.
   * primMethods[0] is used if the argument count is min_args;
   * primMethods[1] is used if the argument count is min_args+1;
   * primMethods[primMethods.length-1] is used otherwise.
   */
  Method[] primMethods;

  /** Select the method used given an argument count. */
  public final Method getMethod(int argCount)
  {
    if (primMethods == null || (max_args >= 0 && argCount > max_args))
      return null;
    int index = argCount - min_args;
    if (index < 0)
      return null; // Too few arguments.
    int length = primMethods.length;
    return primMethods[index < length ? index : length - 1];
  }

  /** Get the method that contains the actual body of the procedure.
   * (The other methods are just stubs that call that method.) */
  public Method getMainMethod()
  {
    Method[] methods = primMethods;
    return methods == null ? null : methods[methods.length-1];
  }

  /** Return the parameter type of the "keyword/rest" parameters. */
  public final Type restArgType()
  {
    if (min_args == max_args)
      return null;
    if (primMethods == null)
      throw new Error("internal error - restArgType");
    Method[] methods = primMethods;
    if (max_args >= 0 && methods.length > max_args - min_args)
      return null;
    Type[] types = methods[methods.length-1].getParameterTypes();
    return types[types.length-1];
  }

  public void setName (String name)
  {
    this.name = name;
  }

  public String getName () { return name; }

  public LambdaExp outerLambda ()
  {
    return outer == null ? null : outer.currentLambda ();
  }

  /** Return the closest outer non-inlined LambdaExp. */

  public LambdaExp outerLambdaNotInline ()
  {
    for (ScopeExp exp = this; (exp = exp.outer) != null; )
      {
	if (exp instanceof LambdaExp)
	  {
	    LambdaExp result = (LambdaExp) exp;
	    if (! result.getInlineOnly())
	      return result;
	  }
      }
    return null;
  }

  /** Return the englobing ClassExp or null. */

  public ClassExp outerClass ()
  {
    for (ScopeExp exp = this; exp != null; exp = exp.outer)
      {
	if (exp instanceof ClassExp)
	  return (ClassExp) exp;
      }
    return null;
  }

  /** For an INLINE_ONLY function, return the function it gets inlined in. */
  public LambdaExp getCaller ()
  {
    return returnContinuation.context;
  }

  Variable thisVariable;

  public Variable declareThis(ClassType clas)
  {
    if (thisVariable == null)
      {
        thisVariable = new Variable("this");
	scope.addVariableAfter(null, thisVariable);
	thisVariable.setParameter (true);  thisVariable.setArtificial (true);
      }
    if (thisVariable.getType() == null)
      thisVariable.setType(clas);
    return thisVariable;
  }

  public Variable declareClosureEnv()
  {
    if (closureEnv == null && getNeedsClosureEnv())
      {
	LambdaExp parent = outerLambda();
	if (parent instanceof ClassExp)
	  parent = parent.outerLambda();
	Variable parentFrame = parent.heapFrame != null ?  parent.heapFrame
	  : parent.closureEnv;
	if (isClassMethod())
	  /*closureEnv =*/ declareThis(type);
	else if (parent.heapFrame == null && ! parent.getNeedsStaticLink()
		 && ! (parent instanceof ModuleExp))
	  closureEnv = null;
	else if (! isClassGenerated() && ! getInlineOnly())
	  {
	    Method primMethod = getMainMethod();
	    if (! primMethod.getStaticFlag())
	      /*closureEnv =*/ declareThis(primMethod.getDeclaringClass());
	    else
	      {
		Type envType = primMethod.getParameterTypes()[0];
		closureEnv = new Variable("closureEnv", envType);
		scope.addVariableAfter(null, closureEnv);
		closureEnv.setArtificial(true);
		closureEnv.setParameter(true);
	      }
	  }
	else
	  {
	    LambdaExp caller = getInlineOnly() ? getCaller() : null;
	    if (parent == caller)
	      closureEnv = parentFrame;
	    else if (caller != null && parent == caller.outerLambdaNotInline())
	      closureEnv = caller.closureEnv;
	    else
	      {
		closureEnv = new Variable("closureEnv",
                                          parentFrame.getType());
		scope.addVariable(closureEnv);
		closureEnv.setArtificial(true);
	      }
	  }
      }
    return closureEnv;
  }

  public LambdaExp ()
  {
  }

  public LambdaExp(int args)
  {
    min_args = args;
    max_args = args;
  }


  public LambdaExp (Expression body)
  {
    this.body = body;
  }

  /** Generate code to load heapFrame on the JVM stack. */
  public void loadHeapFrame (Compilation comp)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    LambdaExp curLambda = comp.curLambda;
    while (curLambda != this && curLambda.getInlineOnly())
      curLambda = curLambda.returnContinuation.context;
    if (this == curLambda)
      {
	if (this.heapFrame == null)
	  code.emitPushThis();
	else
	  code.emitLoad(this.heapFrame);
      }
    else
      {
	if (curLambda.closureEnv == null)
	  code.emitPushThis();
	else
	  code.emitLoad(curLambda.closureEnv);
	LambdaExp parent = curLambda.outerLambda();
	while (parent != this)
	  {
	    if (parent.staticLinkField != null)
	      code.emitGetField(parent.staticLinkField);
	    //curLambda = parent;
	    parent = parent.outerLambda();
	  }
      }
  }

  /** Get the i'the formal parameter. */
  Declaration getArg (int i)
  {
    for (Declaration var = firstDecl();  ; var = var.nextDecl ())
      {
	if (var == null)
	  throw new Error ("internal error - getArg");
        if (i == 0)
          return var;
        --i;
      }
  }

  public void compileEnd (Compilation comp)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    if (! getInlineOnly())
      {
	if (comp.method.reachableHere()
	    /* Work-around since reachableHere is not computed properly:
	       Only return if the method is void or if there is a value
	       on the stack.
	    */
	    && (code.SP > 0 || comp.method.getReturnType().isVoid())
	    && (! Compilation.usingTailCalls
		|| isModuleBody() || isClassMethod() || isHandlingTailCalls()))
	  {
            // The line of a method is by convention it's last one,
            // specifically to make this feature possible.
            if (getLine() > 0)
              code.putLineNumber(getFile(), getLine());

            code.emitReturn();
          }
	code.popScope();        // Undoes enterScope in allocParameters
      }
    if (! Compilation.fewerClasses) // FIXME
      code.popScope(); // Undoes pushScope in method.initCode.

    if (applyMethods != null && applyMethods.size() > 0)
      {
	Method save_method = comp.method;
	ClassType save_class = comp.curClass;
	comp.curClass = getHeapFrameType();
	comp.generateApplyMethods(this);
	comp.method = save_method;
	comp.curClass = save_class;
      }
    if (heapFrame != null)
      comp.generateConstructor((ClassType) heapFrame.getType(), this);
  }

  Field allocFieldFor (Compilation comp)
  {
    if (nameDecl != null && nameDecl.field != null)
      return nameDecl.field;
    String name = getName();
    String fname = name == null ? "lambda" : Compilation.mangleName(name);
    int fflags = Access.FINAL;
    if (nameDecl != null && nameDecl.context instanceof ModuleExp)
      {
	if (nameDecl.getFlag(Declaration.STATIC_SPECIFIED))
          {
            fflags |= Access.STATIC;
            // If there is no instanceField, then the field gets initialized in
            // <init>, not <clinit>, which is bad for a "static final" field.
            if (! ((ModuleExp) nameDecl.context).isStatic())
              fflags &= ~Access.FINAL;
          }
	if (! nameDecl.isPrivate())
	  fflags |= Access.PUBLIC;
      }
    else
      {
	fname = fname + "$Fn" + ++comp.localFieldIndex;
	if (! getNeedsClosureEnv())
	  fflags = (fflags | Access.STATIC) & ~Access.FINAL;
      }
    ClassType frameType = getHeapLambda().getHeapFrameType();
    Type rtype = Compilation.getMethodProcType(frameType);
    Field field = frameType.addField (fname, rtype, fflags);
    if (nameDecl != null)
      nameDecl.field = field;
    return field;
  }

  final void addApplyMethod(LambdaExp lexp)
  {
    if (applyMethods == null)
      {
	applyMethods = new Vector();
	if (heapFrame != null)
	  ((ClassType) heapFrame.getType()).setSuper(Compilation.typeModuleBody);
	else 
	  type.setSuper(Compilation.typeModuleBody);
      }
    applyMethods.addElement(lexp);
  }

  public Field compileSetField (Compilation comp)
  {
    if (comp.usingCPStyle())
      compile(comp, Type.pointer_type);
    else
      {
	compileAsMethod(comp);
	getHeapLambda().addApplyMethod(this);
      }

    return (new ProcInitializer(this, comp)).field;
  }

  public void compile (Compilation comp, Target target)
  {
    if (target instanceof IgnoreTarget)
      {
	if (getInlineOnly())
	  return;
	// else Causes failures.  Better to remove unneeded LambdaExp earlier. FIXME
      }
    Type rtype;
    CodeAttr code = comp.getCode();

    if (comp.usingCPStyle())
      {
	//	Label func_start = new Label(code);
	Label func_end = new Label(code);
	LambdaExp saveLambda = comp.curLambda;
	comp.curLambda = this;
	type = saveLambda.type;
	closureEnv = saveLambda.closureEnv;
        /*
	if (comp.usingCPStyle())
	  {
	    heapFrame = comp.thisDecl;
	    for (Declaration var = firstDecl();
		 var != null; var = var.nextDecl())
	      var.assignField(comp);
	  }
        */
	gnu.bytecode.SwitchState fswitch = comp.fswitch;
	int pc = comp.fswitch.getMaxValue() + 1;
	code.emitGoto(func_end);
	Type[] stackTypes = code.saveStackTypeState(true);

	fswitch.addCase(pc, code);
        /*
	code.emitPushThis();
	code.emitGetField(comp.argsCallContextField);
	code.emitStore(comp.argsArray);
        */
	allocParameters(comp);
	enterFunction(comp);

	compileBody(comp);
	compileEnd(comp);
	comp.curLambda = saveLambda;
	func_end.define(code);
	code.restoreStackTypeState(stackTypes);
	ClassType ctype = comp.curClass;
	rtype = ctype;
	/*
	code.emitNew(ctype);
	code.emitDup(ctype);
	code.emitInvokeSpecial(ctype.constructor);
	code.emitDup(ctype);
	code.emitPushInt(pc);
	code.emitPutField(comp.saved_pcCallFrameField);
	if (isHandlingTailCalls())
	  {
	    // Set name field.
	    if (name != null)
	      {
		code.emitDup(ctype);
		code.emitPushString(name);
		code.emitInvokeVirtual(comp.setNameMethod);
	      }
	    // Set numArgs field.
	    code.emitDup(ctype);
	    code.emitPushInt(min_args | (max_args << 12));
	    code.emitPutField(comp.numArgsCallFrameField);
	    // Set static link field to this CallFrame.
	    code.emitDup(ctype);
	    code.emitPushThis();
	    code.emitPutField(comp.callerCallFrameField);
	  }
	*/
      }
    else
      { LambdaExp outer = outerLambda();
	rtype = comp.typeModuleMethod;
	if ((flags & NO_FIELD) != 0)
	  {
	    compileAsMethod(comp);
	    ProcInitializer.emitLoadModuleMethod(this, comp);
	  }
	else
	  {
	    Field field = compileSetField(comp);
	    if (field.getStaticFlag())
	      code.emitGetStatic(field);
	    else
	      {
		LambdaExp parent = comp.curLambda;
		Variable frame
		  = parent.heapFrame != null ? parent.heapFrame
		  : parent.closureEnv;
		code.emitLoad(frame);
		code.emitGetField(field);
	      }
	  }
      }
    target.compileFromStack(comp, rtype);
  }

  public ClassType getHeapFrameType()
  {
    if (this instanceof ModuleExp || this instanceof ClassExp)
      return (ClassType) getType();
    else
      return (ClassType) heapFrame.getType();
  }

  public LambdaExp getHeapLambda()
  {
    ScopeExp exp = outer;
    
    if (getNeedsClosureEnv())
      for (;; exp = exp.outer)
        {
          if (exp == null)
            return null;
          if (exp instanceof ModuleExp
              || (exp instanceof ClassExp 
                  && ((ClassExp) exp).needsConstructor)
              || (exp instanceof LambdaExp 
                  && ((LambdaExp) exp).heapFrame != null))
            return (LambdaExp) exp;
        }

    // No need for heap, return the englobing class
    ClassExp res = currentClass();

    /*
      For user classes, needsConstructor is false, but their outer
      class (lambda) is the package class.
    */
    if (res.needsConstructor)
      return res;
    else
      return res.outerLambda();

  }

  void addMethodFor (Compilation comp, ObjectType closureEnvType)
  {
    LambdaExp heapLambda = getHeapLambda();
    ClassType ctype = heapLambda.getHeapFrameType();
    addMethodFor(ctype, comp, closureEnvType);
  }

  void addMethodFor (ClassType ctype, Compilation comp, ObjectType closureEnvType)
  {
    // generate_unique_name (new_class, child.getName());
    String name = getName();
    LambdaExp outer = outerLambda();

    int key_args = keywords == null ? 0 : keywords.length;
    int opt_args = defaultArgs == null ? 0 : defaultArgs.length - key_args;
    int numStubs =
      ((flags & DEFAULT_CAPTURES_ARG) != 0) || Compilation.usingTailCalls ? 0
      : opt_args;
    boolean varArgs = max_args < 0 || min_args + numStubs < max_args;
    primMethods = new Method[numStubs + 1];

    boolean isStatic;
    boolean isInitMethod = false;
    if (isClassMethod())
      {
	if (outer instanceof ClassExp)
	  {
	    ClassExp cl = (ClassExp) outer;
	    isStatic = cl.isMakingClassPair() && closureEnvType != null;
	    if (this == cl.initMethod)
	      isInitMethod = true;
	  }
	else
	  isStatic = false;
      }
    else if (thisVariable != null || closureEnvType == ctype)
      isStatic = false;
    else if (nameDecl == null)
      isStatic = true;
    else if (nameDecl.getFlag(Declaration.NONSTATIC_SPECIFIED))
      isStatic = false;
    else if (nameDecl.getFlag(Declaration.STATIC_SPECIFIED))
      isStatic = true;
    else if (nameDecl.context instanceof ModuleExp)
      {
	ModuleExp mexp = (ModuleExp) nameDecl.context;
	isStatic = mexp.getSuperType() == null && mexp.getInterfaces() == null;
      }
    else
      isStatic = true;

    StringBuffer nameBuf = new StringBuffer(60);
    if (! (outer.isModuleBody() || outer instanceof ClassExp)
	|| name == null)
      {
	nameBuf.append("lambda");
	nameBuf.append(+(++comp.method_counter));
      }
    if (name != null)
      nameBuf.append(Compilation.mangleName(name));
    if (getFlag(SEQUENCE_RESULT))
      nameBuf.append("$C");
    if (Compilation.usingTailCalls && ! isInitMethod && ! isClassMethod())
      nameBuf.append("$T");

    int mflags = (isStatic ? Access.STATIC : 0);
    if (nameDecl != null)
      if (nameDecl.isSpecifiedPrivate())
        mflags |= Access.PRIVATE;
      else if (! nameDecl.isPrivate())
        mflags |= Access.PUBLIC;

    if (isInitMethod)
      {
	// Make it provide to prevent inherited $finit$ from overriding
	// the current one - and thus preventing its execution.
	mflags = (mflags & ~(Access.PUBLIC|Access.PROTECTED))+Access.PRIVATE;
      }
    if (ctype.isInterface())
      mflags |= Access.ABSTRACT;
    if (! isStatic)
      if (isClassMethod())
	declareThis(ctype);
      else
	closureEnv = declareThis(ctype);

    Type rtype
      = (getFlag(SEQUENCE_RESULT)
	 || (Compilation.usingTailCalls && ! isClassMethod()))
      ? Type.void_type
      : getReturnType().getImplementationType();
    int extraArg = (closureEnvType != null && closureEnvType != ctype) ? 1 : 0;
    if (Compilation.usingTailCalls && ! isInitMethod && ! isClassMethod())
      {
	Type[] atypes = new Type[1+extraArg];
	if (extraArg > 0)
	  atypes[0] = closureEnvType;
	atypes[extraArg] = Compilation.typeCallContext;
	primMethods[0] = ctype.addMethod(nameBuf.toString(), atypes, rtype, mflags);
	argsArray = new Variable("argsArray", Compilation.objArrayType);
	return;
      }
    for (int i = 0;  i <= numStubs;  i++)
      {
	int plainArgs = min_args + i;
	int numArgs = plainArgs;
	if (i == numStubs && varArgs)
	  numArgs++;
	Type[] atypes = new Type[extraArg + numArgs];
	if (extraArg > 0)
	  atypes[0] = closureEnvType;
	Declaration var = firstDecl();
	for (int itype = 0; itype < plainArgs; var = var.nextDecl())
	  atypes[extraArg + itype++] = var.getType().getImplementationType();
	if (plainArgs < numArgs)
	  {	
	    nameBuf.append("$V");
	    name = nameBuf.toString();
	    Type lastType = var.getType();
	    String lastTypeName = lastType.getName();
	    if (key_args > 0 || numStubs < opt_args
		|| ! ("gnu.lists.LList".equals(lastTypeName)
		      || "java.lang.Object[]".equals(lastTypeName)))
	      {
		lastType = Compilation.objArrayType;
		argsArray = new Variable("argsArray",
					 Compilation.objArrayType);
	      }
	    firstArgsArrayArg = var;
	    atypes[atypes.length-1] = lastType;
	  }

	boolean classSpecified
	  = (outer instanceof ClassExp
	     || (outer instanceof ModuleExp
		 && (((ModuleExp) outer)
		     .getFlag(ModuleExp.SUPERTYPE_SPECIFIED))));
	name = nameBuf.toString();
	  {
	    // If the base class or interfaces were not explicitly
	    // specified, then any existing matching method (such as "run"
	    // or "apply") is a conflict.  So rename the method.
	    int renameCount = 0;
	    int len = nameBuf.length();
	  retry:
	    for (;;)
	      {
		for (ClassType t = ctype;  t != null; t = t.getSuperclass ())
		  {
		    if (t.getDeclaredMethod(name, atypes) != null)
		      {
			nameBuf.setLength(len);
			nameBuf.append('$');
			nameBuf.append(++renameCount);
			name = nameBuf.toString();
			continue retry;
		      }
		    if (classSpecified)
		      break;
		  }
		break;
	      }
	  }
	primMethods[i] = ctype.addMethod(name, atypes, rtype, mflags);
      }

    addAttributes(getMainMethod());
  }

  // Can we merge this with allocParameters?
  public void allocChildClasses (Compilation comp)
  {
    if (this instanceof ModuleExp)
      ((ModuleExp) this).allocFields(comp);
    else
      {
	Method main = getMainMethod();
	
	Declaration decl = firstDecl();
	if (isHandlingTailCalls())
	  {
	    firstArgsArrayArg = decl;
	    if (! Compilation.usingTailCalls) // FIXME
	      scope.addVariable(null, comp.typeCallContext, "$ctx");
	  }
	for (;;)
	  {
	    if (decl == firstArgsArrayArg && argsArray != null)
	      {
		scope.addVariable(argsArray);
		argsArray.setParameter(true);
		argsArray.setArtificial(true);
	      } 
	    if (decl == null)
	      break;
	    Variable var = decl.var;
	    // i is the register to use for the current parameter
	    if (decl.isSimple () && ! decl.isIndirectBinding())
	      {
		// For a simple parameter not captured by an inferior lambda,
		// just allocate it in the incoming register.
                var = decl.allocateVariable(null);
		//var.allocateLocal(code);
	      }
	    else
	      {
		// This variable was captured by an inner lambda.
		// Its home location is in the heapFrame.
		// Later, we copy it from its incoming register
		// to its home location heapFrame.  Here we just create and
		// assign a Variable for the incoming (register) value.
		String vname
                  = Compilation.mangleName(decl.getName()).intern();
                var = decl.var
                  = scope.addVariable(null, decl.getType(), vname);
		//scope.addVariableAfter(var, decl);
		var.setArtificial (true);
		var.setParameter (true);
		//var.allocateLocal(code);
	      }
	    decl = decl.nextDecl();
	  }
      }

    declareClosureEnv();

    if (comp.usingCPStyle() && comp.curClass == comp.mainClass)
      return;

    allocFrame(comp);

    for (LambdaExp child = firstChild;  child != null;
	 child = child.nextSibling)
      {
	if (child.isClassGenerated())
	  {
	    if (child.min_args != child.max_args || child.min_args > 4
		|| child.isHandlingTailCalls())
	      {
		child.argsArray = new Variable("argsArray", comp.objArrayType);
		child.firstArgsArrayArg = child.firstDecl();
	      }
	  }
	else if (! child.getInlineOnly())
	  {
	    boolean method_static;
	    ObjectType closureEnvType;
	    if (! child.getNeedsClosureEnv())
	      closureEnvType = null;
	    else if (this instanceof ClassExp)
	      closureEnvType = getCompiledClassType(comp);
	    else if ("$finit$".equals(getName()))
	      closureEnvType = outerLambda().getCompiledClassType(comp);
            else
              {
                LambdaExp owner = this;
                while (owner.heapFrame == null)
		  owner = owner.outerLambda();
                closureEnvType = (ClassType) owner.heapFrame.getType();
              }
	    child.addMethodFor(comp, closureEnvType);
	  }
      }
  }

  public void allocFrame (Compilation comp)
  {
    if (heapFrame != null)
      {
	ClassType frameType;
	if (this instanceof ModuleExp || this instanceof ClassExp)
	  frameType = getCompiledClassType(comp);
	else
	  {
	    ClassExp outerClass = outerClass();
	    if (outerClass != null)
              {
                String name = getName();
                if (name == null)
                  name = "lambda";
                // At least for constructors ("<init>") we need to make sure
                // that the name is valid.
                name = Compilation.mangleName(name);
                // Use the outer class as a prefix name.
                name = outerClass.getName() + "$" + name;
                // Make sure the name is unique.
                name = comp.generateUniqueName(name);
                frameType = new ClassType(name);
                if (outerClass.getFile() != null)
                  frameType.setSourceFile(outerClass.getFile());
              }
	    else
	      frameType = new ClassType(comp.generateClassName("frame"));
	    if (Compilation.usingTailCalls) // FIXME
	    frameType.setSuper(Type.pointer_type);
	    else
	    frameType.setSuper(Compilation.typeModuleBody);
	    comp.addClass(frameType);
	  }
	heapFrame.setType(frameType);
      }
  }

  void allocParameters (Compilation comp)
  {
    CodeAttr code = comp.getCode();

    int i = 0;
    int j = 0;

    if (isHandlingTailCalls() && ! isModuleBody() && ! comp.usingCPStyle())
      {
	comp.callStackContext = new Variable ("$ctx", comp.typeCallContext);
	// Variable thisVar = isStaic? = null : declareThis(comp.curClass);
	scope.addVariableAfter(thisVariable, comp.callStackContext);
	comp.callStackContext.setParameter(true);
	comp.callStackContext.setArtificial(true);
      }

    code.locals.enterScope (scope);

    if (argsArray != null && isHandlingTailCalls())
      {
        code.emitLoad(comp.callStackContext);
	code.emitInvoke(comp.typeCallContext.getDeclaredMethod("getArgs", 0));
        code.emitStore(argsArray);
      }

    for (Declaration decl = firstDecl();  decl != null;  )
      {
        Variable var = decl.var;
	// If the only reason we are using an argsArray is because there
	// are more than 4 arguments, copy the arguments in local register.
	// Then forget about the args array.  We do this first, before
	// the label that tail-recursion branches back to.
	// This way, a self-tail-call knows where to leave the argumnents.
	if (argsArray != null && min_args == max_args
	    && primMethods == null && ! isHandlingTailCalls())
	  {
	    code.emitLoad(argsArray);
	    code.emitPushInt(j);
	    code.emitArrayLoad(Type.pointer_type);
	    decl.getType().emitCoerceFromObject(code);
	    code.emitStore(decl.getVariable());
	  }
	j++;
	i++;
	decl = decl.nextDecl();
      }
    if (heapFrame != null)
      heapFrame.allocateLocal(code);
  }

  static Method searchForKeywordMethod3;
  static Method searchForKeywordMethod4;

  /** Rembembers stuff to do in <init> of this class. */
  Initializer initChain;
  /** Rembembers stuff to do in <clinit> of this class. */
  Initializer clinitChain;

  void addInitializer(Initializer elem)
  {
    elem.next = this.initChain;
    this.initChain = elem;
  }

  void addClassInitializer(Initializer elem)
  {
    elem.next = this.clinitChain;
    this.clinitChain = elem;
  }

  private Field instanceField;
  Field getInstanceField()
  {
    if (instanceField == null)
      {
	instanceField = type.addField
	  ("$instance", type, Access.STATIC|Access.FINAL|Access.PUBLIC);
      }
    return instanceField;
  }

  /** Rembembers literals to initialize (in <clinit>). */
  Literal literalsChain;
  java.util.Hashtable literalTable;

  void generateClassInit(Compilation comp)
  {
    if (instanceField == null && 
	clinitChain == null &&
	literalsChain == null)
      return;

    Method method = type.addMethod ("<clinit>", Type.typeArray0, 
				    Type.void_type,
				    Access.PUBLIC|Access.STATIC);
    method.init_param_slots ();

    CodeAttr code = method.getCode();
    
    // These gotos are losing.  Should instead do a pre-pass (using
    // an ExpWalker) before compilation that collects all needed
    // constants.  Then we generate code to init the literals *first*,
    // before compiling anything else.  FIXME.
    Label lab0 = new Label(code);
    Label lab1 = new Label(code);
    Label lab2 = new Label(code);

    code.emitGoto(lab2);

    lab0.define(code);

    if (clinitChain != null)
      {
	Label lastClinit = new Label(code);
	code.emitGoto(lastClinit);
	Label previous = null;

	/* Dumping initializers can add new initializers,
	   so we loop until none is added.
	   The order of execution is reversed, the last ones being required
	   by the first ones.
	*/
	while (clinitChain != null)
	  {
	    Label current = new Label(code);
	    current.define(code);

	    Initializer chain = clinitChain;
	    clinitChain = null;
	    comp.dumpInitializers(chain, method);

	    if (previous != null)
	      code.emitGoto(previous);
	    else
	      method.getCode().emitReturn();

	    if (clinitChain == null)
	      {
		lastClinit.define(code);
		code.emitGoto(current);
	      }
	    else
	      previous = current;
	  }
      }
    else
      method.getCode().emitReturn();

    lab1.define(code);
    comp.literalsChain = literalsChain;
    // reset the litTable to force emission of the literals
    // litTable should not be a field in compilation
    comp.litTable = null;
    comp.method = method;
    Literal.emit(comp);
    code.emitGoto(lab0);
    
    lab2.define(code);
    if (instanceField != null)
      {
	comp.generateConstructor(this);
	//ClassType type = getHeapFrameType(); // Is this more correct?
        if (type.constructor != null)
          {
            code.emitNew(type);
            code.emitDup(type);
            code.emitInvokeSpecial(type.constructor);
            code.emitPutStatic(instanceField);
          }
      }
    else if (this instanceof ClassExp)
      // generateConstructor might have been called earlier, but we call it 
      // again because the super-class (gnu.expr.ModuleBody)
      // seems to be found late.
      comp.generateConstructor(this);
    code.emitGoto(lab1);
  }

  void enterFunction (Compilation comp)
  {
    CodeAttr code = comp.getCode();

    // Tail-calls loop back to here!
    scope.setStartPC(code.getPC());

    if (closureEnv != null && ! closureEnv.isParameter()
	&& ! comp.usingCPStyle())
      {
	if (getInlineOnly())
	  outerLambda().loadHeapFrame(comp);
	else
	  {
	    code.emitPushThis();
	    Field field = closureEnvField;
	    if (field == null)
	      field = outerLambda().closureEnvField;
	    code.emitGetField(field);
	  }
	code.emitStore(closureEnv);
      }
    if (heapFrame != null && ! comp.usingCPStyle())
      {
	ClassType frameType = (ClassType) heapFrame.getType();
	for (Declaration decl = capturedVars; decl != null;
	     decl = decl.nextCapturedVar)
	  {
	    if (decl.field != null)
	      continue;
     	    String dname = comp.mangleName(decl.getName());
	    String mname = dname;
	    // Check for existing field with same name.
	    for (int i = 0; ; )
	      {
		Field fld = frameType.getField(mname);
		if (fld == null)
		  break;
		mname = dname + '_' + ++i;
	      }
	    Type dtype = decl.getType();
	    decl.field = frameType.addField (mname, decl.getType());
	  }
	if (closureEnv != null && heapFrame != null)
	  staticLinkField = frameType.addField("staticLink",
					       closureEnv.getType());
        if (! (this instanceof ModuleExp) && ! (this instanceof ClassExp))
          {
	    code.emitNew(frameType);
	    code.emitDup(frameType);
	    Method constructor = comp.getConstructor(frameType, this);
	    code.emitInvokeSpecial(constructor);

            // In a constructor, the closure (this) is not valid until the
            // end, so it shouldn't be used (presumably it is not used
            // anyway).
            // There might be a deeper change that would make this test
            // unnecessary.
            if (staticLinkField != null && ! (this instanceof ConstructorExp))
              {
                code.emitDup(heapFrame.getType());
                code.emitLoad(closureEnv);
                code.emitPutField(staticLinkField);
              }
            code.emitStore(heapFrame);
          }
      }

    Variable argsArray = this.argsArray;
    if (min_args == max_args && ! comp.fewerClasses
	&& primMethods == null && ! isHandlingTailCalls())
      argsArray = null;

    // For each non-artificial parameter, copy it from its incoming
    // location (a local variable register, or the argsArray) into
    // its home location, if they are different.
    int i = 0;
    int opt_i = 0;
    int key_i = 0;
    int key_args = keywords == null ? 0 : keywords.length;
    int opt_args = defaultArgs == null ? 0
      : defaultArgs.length - key_args;
    if (this instanceof ModuleExp)
      return;
    // If plainArgs>=0, it is the number of arguments *not* in argsArray.
    int plainArgs = -1;
    int defaultStart = 0;
    Method mainMethod = getMainMethod();

    for (Declaration param = firstDecl();  param != null; param = param.nextDecl())
      {
	if (param == firstArgsArrayArg && argsArray != null)
	  {
	    if (primMethods != null && ! Compilation.usingTailCalls)
	      {
		plainArgs = i;
		defaultStart = plainArgs - min_args;
	      }
	    else
	      {
		plainArgs = 0;
		defaultStart = 0;
	      }
	  }
	if (plainArgs >= 0 || ! param.isSimple()
	    || param.isIndirectBinding())
	  {
	    Type paramType = param.getType();
	    Type stackType
	      = (mainMethod == null || plainArgs >= 0 ? Type.pointer_type
		 : paramType);
	    // If the parameter is captured by an inferior lambda,
	    // then the incoming parameter needs to be copied into its
	    // slot in the heapFrame.  Thus we emit an aaload instruction.
	    // Unfortunately, it expects the new value *last*,
	    // so first push the heapFrame array and the array index.
	    if (!param.isSimple ())
	      param.loadOwningObject(comp);
	    // This part of the code pushes the incoming argument.
	    if (plainArgs < 0)
	      {
		// Simple case:  Use Incoming register.
		code.emitLoad(param.getVariable());
	      }
            else if (i < min_args)
	      { // This is a required parameter, in argsArray[i].
		code.emitLoad(argsArray);
		code.emitPushInt(i);
		code.emitArrayLoad(Type.pointer_type);
	      }
            else if (i < min_args + opt_args)
	      { // An optional parameter
		code.emitPushInt(i - plainArgs);
                code.emitLoad(argsArray);
		code.emitArrayLength();
		code.emitIfIntLt();
                code.emitLoad(argsArray);
		code.emitPushInt(i - plainArgs);
		code.emitArrayLoad(Type.pointer_type);
		code.emitElse();
		defaultArgs[defaultStart + opt_i++].compile(comp, paramType);
		code.emitFi();
	      }
	    else if (max_args < 0 && i == min_args + opt_args)
	      {
		// This is the "rest" parameter (i.e. following a "."):
		// Convert argsArray[i .. ] to a list.
		code.emitLoad(argsArray);
		code.emitPushInt(i - plainArgs);
		code.emitInvokeStatic(Compilation.makeListMethod);
		stackType = comp.scmListType;
              }
	    else
	      { // Keyword argument.
		code.emitLoad(argsArray);
		code.emitPushInt(min_args + opt_args - plainArgs);
		comp.compileConstant(keywords[key_i++]);
		Expression defaultArg = defaultArgs[defaultStart + opt_i++];
		// We can generate better code if the defaultArg expression
		// has no side effects.  For simplicity and safety, we just
		// special case literals, which handles most cases.
		if (defaultArg instanceof QuoteExp)
		  {
		    if (searchForKeywordMethod4 == null)
		      {
			Type[] argts = new Type[4];
			argts[0] = Compilation.objArrayType;
			argts[1] = Type.int_type;
			argts[2] = Type.pointer_type;
			argts[3] = Type.pointer_type;
			searchForKeywordMethod4
			  = Compilation.scmKeywordType.addMethod
			  ("searchForKeyword",  argts,
			   Type.pointer_type, Access.PUBLIC|Access.STATIC);
		      }
		    defaultArg.compile(comp, paramType);
		    code.emitInvokeStatic(searchForKeywordMethod4);
		  }
		else
		  {
		    if (searchForKeywordMethod3 == null)
		      {
			Type[] argts = new Type[3];
			argts[0] = Compilation.objArrayType;
			argts[1] = Type.int_type;
			argts[2] = Type.pointer_type;
			searchForKeywordMethod3
			  = Compilation.scmKeywordType.addMethod
			  ("searchForKeyword",  argts,
			   Type.pointer_type, Access.PUBLIC|Access.STATIC);
		      }
		    code.emitInvokeStatic(searchForKeywordMethod3);
		    code.emitDup(1);
		    comp.compileConstant(Special.dfault);
		    code.emitIfEq();
		    code.emitPop(1);
		    defaultArg.compile(comp, paramType);
		    code.emitFi();
		  }
	      }
	    // Now finish copying the incoming argument into its
	    // home location.
            if (paramType != stackType)
	      CheckedTarget.emitCheckedCoerce(comp, this, i, paramType);
	    if (param.isIndirectBinding())
              param.pushIndirectBinding(comp);
	    if (param.isSimple())
	      code.emitStore(param.getVariable());
            else
	      code.emitPutField(param.field);
	  }
	i++;
      }
  }

  void compileChildMethods (Compilation comp)
  {
    for (LambdaExp child = firstChild;  child != null; )
      {
	if (! child.getCanRead() && ! child.getInlineOnly()
	    && ! child.isHandlingTailCalls())
	  {
	    child.compileAsMethod(comp);
	  }
	child = child.nextSibling;
      }
  }

  void compileAsMethod (Compilation comp)
  {
    if ((flags & METHODS_COMPILED) != 0)
      return;
    flags |= METHODS_COMPILED;
    Method save_method = comp.method;
    LambdaExp save_lambda = comp.curLambda;
    Variable saveStackContext = comp.callStackContext;
    comp.curLambda = this;

    if (primMethods == null)
      {
	// This happens for lambdas that are not toplevel nor contained
	// in a toplevel lambda (eg: global variables values).
	outer = (ClassExp) comp.topLambda;
	addMethodFor(comp.topClass, comp, null);
	compileAsMethod(comp);
      }

    Method method = primMethods[0];
    boolean isStatic = method.getStaticFlag();
    int numStubs = primMethods.length - 1;
    Type restArgType = restArgType();

    int[] saveDeclFlags = null;
    if (numStubs > 0)
      {
	saveDeclFlags = new int[min_args + numStubs];
	int k = 0;
	for (Declaration decl = firstDecl();
	     k < min_args + numStubs; decl = decl.nextDecl())
	  saveDeclFlags[k++] = decl.flags;
      }

    for (int i = 0;  i <= numStubs;  i++)
      {
	comp.method = primMethods[i];
	if (i < numStubs)
	  {
	    comp.method.init_param_slots();
	    CodeAttr code = comp.getCode();
	    int toCall = i + 1;
	    while (toCall < numStubs
		   && defaultArgs[toCall] instanceof QuoteExp)
	      toCall++;
	    int thisArg = isStatic ? 0 : 1;
	    boolean varArgs = toCall == numStubs && restArgType != null;
	    Declaration decl;
	    Variable var = code.getArg(0);
	    if (! isStatic)
	      {
		code.emitPushThis();
		if (getNeedsClosureEnv())
		  closureEnv = var;
		var = code.getArg(1);
	      }
	    decl = firstDecl();
	    for (int j = 0;  j < min_args + i;  j++, decl = decl.nextDecl())
	      {
		decl.flags |= Declaration.IS_SIMPLE;
		decl.var = var;
		code.emitLoad(var);
		var = var.nextVar();
	      }
	    for (int j = i; j < toCall;  j++, decl = decl.nextDecl())
	      {
		Target paramTarget = StackTarget.getInstance(decl.getType());
		defaultArgs[j].compile(comp, paramTarget);
	      }
	    if (varArgs)
	      {
		Expression arg;
		String lastTypeName = restArgType.getName();
		if ("gnu.lists.LList".equals(lastTypeName))
		  arg = new QuoteExp(gnu.lists.LList.Empty);
		else if ("java.lang.Object[]".equals(lastTypeName))
		  arg = new QuoteExp(Values.noArgs);
		else // FIXME
		  throw new Error("unimplemented #!rest type");
		arg.compile(comp, restArgType);
	      }
	    if (isStatic)
	      code.emitInvokeStatic(primMethods[toCall]);
	    else
	      code.emitInvokeVirtual(primMethods[toCall]);
	    code.emitReturn();
	    closureEnv = null;
	  }
	else
	  {
	    if (saveDeclFlags != null)
	      {
		int k = 0;
		for (Declaration decl = firstDecl();
		     k < min_args + numStubs; decl = decl.nextDecl())
		  {
		    decl.flags = saveDeclFlags[k++];
		    decl.var = null;
		  }
	      }
            if (comp.curClass.isInterface() && comp.method.isAbstract()){
              // Interface method. Skip code generation.
            }else{
              comp.method.initCode();
              allocChildClasses(comp);
              allocParameters(comp);
              enterFunction(comp);

              compileBody(comp);
              compileEnd(comp);
            }
	  }
      }

    compileChildMethods(comp);
    comp.method = save_method;
    comp.curLambda = save_lambda;
    comp.callStackContext = saveStackContext;
  }

  public void compileBody (Compilation comp)
  {
    Target target;
    if (isHandlingTailCalls())
      {
	CodeAttr code = comp.getCode();
	Variable ctxVar = comp.callStackContext;
	code.emitLoad(ctxVar);
	code.emitGetField(comp.typeCallContext.getDeclaredField("consumer"));
	Scope scope = code.getCurrentScope();
	Variable result
	  = scope.addVariable(code, Compilation.typeConsumer, "$result");
	code.emitStore(result);
	target = new ConsumerTarget(result);
      }
    else
      target = Target.pushValue(comp.method.getReturnType());
    body.compileWithPosition(comp, target);
  }

  /** A cache if this has already been evaluated. */
  Procedure thisValue;

  protected Expression walk (ExpWalker walker)
  {
    return walker.walkLambdaExp(this);
  }

  protected void walkChildren(ExpWalker walker)
  {
    walkChildrenOnly(walker);
    walkProperties(walker);
  }

  protected final void walkChildrenOnly(ExpWalker walker)
  {
    LambdaExp save = walker.currentLambda;
    walker.currentLambda = this;
    try
      {
	walker.walkDefaultArgs(this);
	if (walker.exitValue == null && body != null)
	  body = body.walk(walker);
      }
    finally
      {
	walker.currentLambda = save;
      }
  }

  protected final void walkProperties(ExpWalker walker)
  {
    if (properties != null)
      {
	int len = properties.length;
	for (int i = 1;  i < len;  i += 2)
	  {
	    Object val = properties[i];
	    if (val instanceof Expression)
	      {
		properties[i] = ((Expression) properties[i]).walk(walker);
	      }
	  }
      }
  }

  public void print (OutPort out)
  {
    out.startLogicalBlock("(Lambda/", ")", 2);
    if (name != null)
      {
	out.print(name);
	out.print('/');
      }
    out.print(id);
    out.print('/');
    out.writeSpaceFill();
    printLineColumn(out);
    out.print('(');
    Special prevMode = null;
    int i = 0;
    int opt_i = 0;
    int key_args = keywords == null ? 0 : keywords.length;
    int opt_args = defaultArgs == null ? 0 : defaultArgs.length - key_args;
    for (Declaration decl = firstDecl();
         decl != null;  decl = decl.nextDecl())
      {
	Special mode;
	if (i < min_args)
	  mode = null;
	else if (i < min_args + opt_args)
	  mode = Special.optional;
	else if (max_args < 0 && i == min_args + opt_args)
	  mode = Special.rest;
	else
	  mode = Special.key;
	if (i > 0)
	   out.writeSpaceFill();
	if (mode != prevMode)
	  {
	    out.print(mode);
	    out.writeSpaceFill();
	  }
	Expression defaultArg = null;
	if ((mode == Special.optional || mode == Special.key)
	    && defaultArgs != null)
	  defaultArg = defaultArgs[opt_i++];
	if (defaultArg != null)
	  out.print('(');
	out.print(decl.getName());
	if (defaultArg != null && defaultArg != QuoteExp.falseExp)
	  {
	    out.print(' ');
	    defaultArg.print(out);
	    out.print(')');
	  }
	i++;
	prevMode = mode;
      }
    out.print(')');
    out.writeSpaceLinear();
    if (body == null)
      out.print("<null body>");
    else
      body.print(out);
    out.endLogicalBlock(")");
  }

  protected final String getExpClassName()
  {
    String cname = getClass().getName();
    int index = cname.lastIndexOf('.');
    if (index >= 0)
      cname = cname.substring(index+1);
    return cname;
  }

  public String toString()
  {
    String str = getExpClassName()+':'+name+'/'+id+'/';

	int l = getLine();
	if (l <= 0 && body != null)
	  l = body.getLine();
	if (l > 0)
	  str = str + "l:" + l;

    return str;
  }

  /** If non-null, a sequence of (key, value)-pairs.
   * These will be used to call setProperty at run-time. */
  Object[] properties;

  public Object getProperty(Object key, Object defaultValue)
  {
    if (properties != null)
      {
	for (int i = properties.length;  (i -= 2) >= 0; )
	  {
	    if (properties[i] == key)
	      return properties[i + 1];
	  }
      }
    return defaultValue;
  }

  public synchronized void setProperty(Object key, Object value)
  {
    properties = Procedure.setProperty(properties, key, value);
  }

  /** If non-null, the type of values returned by this function.
   * If null, the return type has not been set or calculated yet. */
  protected Type returnType;

  /** The return type of this function, i.e the type of its returned values. */
  public final Type getReturnType()
  {
    if (returnType == null)
      {
	returnType = Type.pointer_type;  // To guards against cycles.
	returnType = body.getType();
      }
    return returnType;
  }

  /* Set teh return type of this function. */
  public final void setReturnType (Type returnType)
  {
    this.returnType = returnType;
  }

  /**
     List attributes that should be added to the main primitive method
     generated for this lambdaExp.
  */
  public void addBytecodeAttribute(Attribute a)
  {
    a.setNext(attributes);
    attributes = a;
  }

  private gnu.bytecode.Attribute attributes;

  void addAttributes(AttrContainer bytecode)
  {
    for (Attribute a = attributes; a != null; )
      {
	Attribute next = a.getNext();
	a.addToFrontOf(bytecode);
	a = next;
      }
  }
}
