package gnu.expr;
import gnu.bytecode.*;
import gnu.mapping.*;

/** This class is used to represent "combination" or "application".
 * A function and arguments are evaluated, and then the function applied.
 * @author	Per Bothner
 */

public class ApplyExp extends Expression
{
  Expression func;
  Expression[] args;
  boolean tailCall;

  /** true if this is a special (super) call. */
  boolean special;

  /** Containing LambdaExp. */
  LambdaExp context;

  /** The next ApplyExp in ((ReferenceExp)func).binding.firstCall list. */
  public ApplyExp nextCall;

  public final Expression getFunction() { return func; }
  public final Expression[] getArgs() { return args; }
  public final int getArgCount() { return args.length; }
  public void setArgs(Expression[] args) { this.args = args; }
  public final boolean isTailCall() { return tailCall; }
  public final void setTailCall(boolean tailCall) { this.tailCall = tailCall; }

  public ApplyExp (Expression f, Expression[] a) { this(f, a, false); }

  public ApplyExp (Expression f, Expression[] a, boolean special)
  { func = f; args = a; this.special = special; }

  public ApplyExp (Procedure p, Expression[] a) { func = new QuoteExp(p); args = a; }

  public ApplyExp (Method m, Expression[] a)
  {
    func = new QuoteExp(new PrimProcedure(m));
    args = a;
  }

  public Object eval (Environment env) throws Throwable
  {
    Procedure proc = (Procedure) func.eval(env);
    int n = args.length;
    Object[] vals = new Object[n];
    for (int i = 0; i < n; i++)
      vals[i] = args[i].eval (env);
    return proc.applyN (vals);
  }

  public void eval (Environment env, CallContext ctx) throws Throwable
  {
    Procedure proc = (Procedure) func.eval(env);
    int n = args.length;
    Object[] vals = new Object[n];
    for (int i = 0; i < n; i++)
      vals[i] = args[i].eval (env);
    ctx.setArgsN(vals);
    ctx.proc = proc;
  }

  public static void compileToArray(Expression[] args, Compilation comp)
  {
    CodeAttr code = comp.getCode();
    if (args.length == 0)
      {
	code.emitGetStatic(Compilation.noArgsField);
	return;
      }
    LambdaExp caller = comp.curLambda;
    code.emitPushInt(args.length);
    code.emitNewArray(Type.pointer_type);
    for (int i = 0; i < args.length; ++i)
      {
	Expression arg = args[i];
	if (comp.usingCPStyle
	    && ! (arg instanceof QuoteExp) && ! (arg instanceof ReferenceExp))
	  {
	    // If the argument involves a CPStyle function call, we will
	    // have to save and restore anything on the JVM stack into
	    // fields in the CallFrame.  This is expensive, so defer
	    // pushing the duplicated argument array and the index
	    // until *after* we've calculated the argument.  The downside
	    // is that we have to do some extra stack operations.
	    // However, these are cheap (and get compiled away when
	    // compiling to native code).
	    arg.compile (comp, Target.pushObject);
	    code.emitSwap();
	    code.emitDup(1, 1);
	    code.emitSwap();
	    code.emitPushInt(i);
	    code.emitSwap();
	  }
	else
	  {
	    code.emitDup(comp.objArrayType);
	    code.emitPushInt(i);
	    arg.compile (comp, Target.pushObject);
	  }
	code.emitArrayStore(Type.pointer_type);
      }
  }

  public void compile (Compilation comp, Target target)
  {
    try {
      compile(this, comp, target, true);
    }
    catch (VerificationError e) {
      throw bossa.util.User.error(bossa.util.Location.make(this),
                                  e.getMessage());
    }
  }

  public static void compile (ApplyExp exp, Compilation comp, Target target)
  {
    compile(exp, comp, target, false);
  }

  static void compile (ApplyExp exp, Compilation comp, Target target,
                              boolean checkInlineable)
  {
    int args_length = exp.args.length;
    Expression exp_func = exp.func;
    LambdaExp func_lambda = null;
    String func_name = null;
    if (exp_func instanceof LambdaExp)
      {
	func_lambda = (LambdaExp) exp_func;
	func_name = func_lambda.getName();
	if (func_name == null)
	  func_name = "<lambda>";
      }
    else if (exp_func instanceof ReferenceExp) 
      { 
        Declaration func_decl = ((ReferenceExp) exp_func).binding;
        if (! func_decl.getFlag(Declaration.IS_UNKNOWN))
	  {
	    Expression value = func_decl.getValue();
	    func_name = func_decl.getName();
	    if (value != null && value instanceof LambdaExp) 
	      func_lambda = (LambdaExp) value;
	    if (value != null && value instanceof QuoteExp) 
	      {
		Object quotedValue = ((QuoteExp) value).getValue();
		Procedure proc;
		String msg = null;
		if (! (quotedValue instanceof Procedure))
		  {
		    proc = null;
		    msg = "calling " + func_name + " which is not a procedure";
		  }
                else if (checkInlineable && quotedValue instanceof Inlineable)
                  {
                    ((Inlineable) quotedValue).compile(exp, comp, target);
                    return;
                  }
		else
		  {
		    proc = (Procedure) quotedValue;
		    msg = WrongArguments.checkArgCount(proc, args_length);
		  }
		if (msg != null)
		  comp.error('w', msg);
		else
		  {
		    PrimProcedure pproc
		      = PrimProcedure.getMethodFor(proc, func_decl, exp.args,
						   comp.getInterpreter());
		    if (pproc != null)
		      {
			if (! pproc.getStaticFlag())
			  func_decl.base.load(comp);
			pproc.compile(null, exp.args, comp, target);
			return;
		      }
		  }
	      }
	  }
      }
    else if (exp_func instanceof QuoteExp)
      {
        Object proc = ((QuoteExp) exp_func).getValue();
	if (proc instanceof Inlineable)
	  {
            if (checkInlineable)
              {
                ((Inlineable) proc).compile(exp, comp, target);
                return;
              }

            // If it wasn't inlineable, we already checked for this in Translator.
            PrimProcedure pproc
              = PrimProcedure.getMethodFor((Procedure) proc, exp.args);
            if (pproc != null)
              {
                exp = new ApplyExp(pproc, exp.args);
                ((Inlineable) pproc).compile(exp, comp, target);
                return;
              }
          }
      }

    gnu.bytecode.CodeAttr code = comp.getCode();
    Method method;

    if (func_lambda != null)
      {
	// These error message should really be done earlier,
	// but we do not have the right information until the rewrite pass
	// is finished.  Perhaps InlineCalls would work?  FIXME
        String msg = null;
	if (func_lambda.isClassMethod())
	  args_length--;
	if (args_length < func_lambda.min_args)
          msg = "too few args for ";
	else if (func_lambda.max_args >= 0
		 && args_length > func_lambda.max_args)
          msg = "too many args "+args_length+" for ";
	else if (! func_lambda.isHandlingTailCalls()
		 && (method = func_lambda.getMethod(args_length)) != null)
	  {
	    boolean is_static = method.getStaticFlag();
	    Expression[] args = exp.getArgs();
	    int extraArg = 0;
	    Type[] argTypes = method.getParameterTypes();
	    // ?? Procedure.checkArgCount(this, args.length); // FIXME
	    /* For Per, it seems that when a function is static the
	       this parameter is implicit, which is why he wants to
	       load the heap frame. I remove it for Nice.
	    */
	    if (/*! is_static || */func_lambda.declareClosureEnv() != null)
	      {
		if (is_static)
		  extraArg = 1;
		if (comp.curLambda == func_lambda)
		  code.emitLoad(func_lambda.closureEnv);  // Recursive call.
		else
		  func_lambda.getHeapLambda().loadHeapFrame(comp);
	      }

	    boolean varArgs = func_lambda.restArgType() != null;
	    PrimProcedure.compileArgs(args,
				      func_lambda.isClassMethod() ? (Type) method.getDeclaringClass() : extraArg > 0 ? Type.void_type : null,
				      argTypes, varArgs,
				      func_name, func_lambda, comp);
            if (exp.special)
              code.emitInvokeSpecial(method);
            else
              code.emitInvoke(method);
	    target.compileFromStack(comp, func_lambda.getReturnType());
	    return;
	  }
        if (msg != null)
          {
            comp.error('w', msg + func_name);
	    func_lambda = null;
          }
      }

    if (comp.usingCPStyle())
      {
	  {
	    Label l = new Label(code);
	    gnu.bytecode.SwitchState fswitch = comp.fswitch;
	    int pc = fswitch.getMaxValue() + 1;
	    fswitch.addCase(pc, l, code);
            exp_func.compile (comp, new StackTarget(comp.typeProcedure));
	    code.emitLoad(comp.callStackContext);

	    // Emit: context->pc = pc.
	    code.emitLoad(comp.callStackContext);
	    code.emitPushInt(pc);
	    code.emitPutField(Compilation.pcCallContextField);
	    code.emitInvokeVirtual(comp.applyCpsMethod);

	    // emit[save java stack, if needed]
	    Type[] stackTypes = code.saveStackTypeState(false);
	    java.util.Stack stackFields = new java.util.Stack(); 
	    if (stackTypes != null)
	      {
		for (int i = stackTypes.length;  --i >= 0; )
		  {
		    Field fld = comp.allocLocalField (stackTypes[i], null);
		    code.emitPushThis();
		    code.emitSwap();
		    code.emitPutField(fld);
		    stackFields.push(fld);
		  }
	      }

	    code.emitReturn();
	    l.define(code);

	    // emit[restore java stack, if needed]
	    if (stackTypes != null)
	      {
		for (int i = stackTypes.length;  --i >= 0; )
		  {
		    Field fld = (Field) stackFields.pop();
		    code.emitPushThis();
		    code.emitGetField(fld);
		    comp.freeLocalField(fld);
		  }
	      }

	    /* FIXME
	    // Load result from stack.value to target.
	    code.emitLoad(comp.callStackContext);
	    code.emitGetField(comp.valueCallContextField);
	    target.compileFromStack(comp, Type.pointer_type);
	    */
	  }
	return;
      }

    // Check for tail-recursion.
    boolean tail_recurse
      = exp.tailCall
      && func_lambda != null && func_lambda == comp.curLambda;

    if (func_lambda != null && func_lambda.getInlineOnly() && !tail_recurse
	&& func_lambda.min_args == args_length)
      {
	Declaration param = func_lambda.firstDecl();
	for (int i = 0; i < args_length; ++i)
	  {
	    exp.args[i].compile(comp, param.getType());
	    param = param.nextDecl();
	  }
	LambdaExp saveLambda = comp.curLambda;
	comp.curLambda = func_lambda;
	func_lambda.allocChildClasses(comp);
	func_lambda.allocParameters(comp);
	popParams (code, func_lambda, false);
	func_lambda.enterFunction(comp);
	func_lambda.body.compileWithPosition(comp, target);
	func_lambda.compileEnd(comp);
	// comp.method.popScope();
	func_lambda.compileChildMethods(comp);
	comp.curLambda = saveLambda;
	return;
      }

    if (comp.curLambda != null &&
	comp.curLambda.isHandlingTailCalls()
	&& ! comp.curLambda.getInlineOnly())
      {
	ClassType typeContext = comp.typeCallContext;
	exp_func.compile(comp, new StackTarget(comp.typeProcedure));
	code.emitLoad(comp.callStackContext);
	code.emitDupX();
	// Stack:  context, proc, context
	if (! exp.isTailCall())
	  code.emitDupX();
	//  evaluate args to frame-locals vars;  // may recurse! 
	if (args_length <= 4)
	  {
	    for (int i = 0; i < args_length; ++i)
	      exp.args[i].compile(comp, Target.pushObject);
	    code.emitInvoke(typeContext.getDeclaredMethod("setArgs",
							  args_length));
	  }
	else
	  {
	    compileToArray (exp.args, comp);
	    code.emitInvoke(typeContext.getDeclaredMethod("setArgsN", 1));
	  }
	if (exp.isTailCall())
	  {
	    // Stack: context, proc
	    code.emitPutField(comp.procCallContextField);
	    code.emitReturn();
	  }
	else if (target instanceof ConsumerTarget)
	  {
	    code.emitPutField(comp.procCallContextField);
	    code.emitLoad(((ConsumerTarget) target).getConsumerVariable());
	    code.emitInvoke(typeContext.getDeclaredMethod("runUntilValue", 1));
	  }
	else
	  {
	    code.emitPutField(comp.procCallContextField);
	    code.emitInvoke(typeContext.getDeclaredMethod("runUntilValue", 0));
	    target.compileFromStack(comp, Type.pointer_type);
	  }
	return;
      }

    if (!tail_recurse)
      exp_func.compile (comp, new StackTarget(comp.typeProcedure));

    boolean toArray
      = (tail_recurse ? func_lambda.min_args != func_lambda.max_args
         : args_length > 4);
    if (toArray)
      {
	compileToArray(exp.args, comp);
	method = comp.applyNmethod;
      }
    else if (tail_recurse)
      {
	Declaration param = func_lambda.firstDecl();
	for (int i = 0; i < args_length; ++i)
	  {
	    exp.args[i].compile(comp, param.getType());
	    param = param.nextDecl();
	  }
        method = null;
      }
    else
      {
	for (int i = 0; i < args_length; ++i)
	  exp.args[i].compile (comp, Target.pushObject);
        method = comp.applymethods[args_length];
      }
    if (tail_recurse)
      {
	popParams(code, func_lambda, toArray);
	code.emitTailCall(false, func_lambda.scope);
	return;
      }
    code.emitInvokeVirtual(method);
    target.compileFromStack(comp, Type.pointer_type);
  }

  protected Expression walk (ExpWalker walker)
  {
    return walker.walkApplyExp(this);
  }

  protected void walkChildren(ExpWalker walker)
  {
    func = func.walk(walker);
    if (walker.exitValue == null)
      args = walker.walkExps(args);
  }

  public void print (OutPort out)
  {
    out.startLogicalBlock("(Apply", ")", 2);
    if (tailCall)
      out.print (" [tailcall]");
    out.writeSpaceFill();
    printLineColumn(out);
    func.print(out);
    for (int i = 0; i < args.length; ++i)
      {
	out.writeSpaceLinear();
	args[i].print(out);
      }
    out.endLogicalBlock(")");
  }

  private static void popParams (CodeAttr code, LambdaExp lexp,
                                 boolean toArray)
  {
    Variable params = lexp.scope.firstVar();
    if (params != null && params.getName() == "this")
      params = params.nextVar();
    if (params != null && params.getName() == "$ctx")
      params = params.nextVar();
    if (params != null && params.getName() == "argsArray")
      {
	if (toArray)
	  {
	    popParams (code, params, 1);
	    return;
	  }
        params = params.nextVar();
      }
    popParams (code, params, lexp.min_args);
  }

  // Recursive helper function.
  private static void popParams (CodeAttr code, Variable vars, int count)
  {
    if (count > 0)
      {
	popParams (code, vars.nextVar(), count - 1);
	code.emitStore(vars);
      }
  }

  public final gnu.bytecode.Type getType()
  {
    Expression afunc = func;
    if (afunc instanceof ReferenceExp)
      {
	Declaration func_decl = ((ReferenceExp) afunc).binding;
	if (func_decl != null && ! func_decl.getFlag(Declaration.IS_UNKNOWN))
	  afunc = func_decl.getValue();
      }
    if (afunc instanceof QuoteExp)
      {
	Object proc = ((QuoteExp) afunc).getValue();
	if (proc instanceof Inlineable)
          return ((Inlineable) proc).getReturnType(args);
      }
    if (afunc instanceof LambdaExp)
      {
	return ((LambdaExp) afunc).getReturnType();
      }
    return super.getType();
  }

  public static Expression inlineIfConstant(Procedure proc, ApplyExp exp)
  {
    int len = exp.args.length;
    for (int i = len;  --i >= 0; )
      {
	if (! (exp.args[i] instanceof QuoteExp))
	  return exp;
      }
    Object[] vals = new Object[len];
    for (int i = len;  --i >= 0; )
      {
	vals[i] = ((QuoteExp) (exp.args[i])).getValue();
      }
    try
      {
	return new QuoteExp(proc.applyN(vals));
      }
    catch (Throwable ex)
      {
	// Should emit error message or warning.  FIXME. 
	return null;
      }
  }

}
