// Copyright (c) 1999, 2000  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.expr;
import gnu.bytecode.*;
import java.util.Hashtable;
import gnu.mapping.*;

/** A primitive Procedure implemented by a plain Java method. */

public class PrimProcedure extends MethodProc implements gnu.expr.Inlineable
{
  Type retType;
  Type[] argTypes;
  Method method;
  int op_code;

  /** If non-null, the LambdaExp that this PrimProcedure implements. */
  LambdaExp source;

  java.lang.reflect.Member member;

  public final int opcode() { return op_code; }

  public Type getReturnType () { return retType; }
  public void setReturnType (Type retType) { this.retType = retType; }

  public Type getReturnType (Expression[] args) { return retType; }

  /** Return true iff the last parameter is a "rest" argument. */
  public boolean takesVarArgs()
  {
    return method != null && method.getName().endsWith("$V");
  }

  public int numArgs()
  {
    int num = argTypes.length;
    if (! getStaticFlag())
      num++;
    return takesVarArgs() ? (num - 1) + (-1 << 12) : num + (num << 12);
  }

  public int match (CallContext ctx, Object[] args)
  {
    ctx.setArgsN(args);

    int nargs = ctx.count;
    boolean takesVarArgs = takesVarArgs();
    int mlength = minArgs() + (takesVarArgs ? 1 : 0);
    int fixArgs = takesVarArgs ? mlength - 1 : mlength;

    if (takesVarArgs)
      {
        if (nargs < fixArgs)
          return NO_MATCH_TOO_FEW_ARGS|fixArgs;
      }
    else
      {
        if (nargs != mlength)
          if (nargs < mlength)
            return NO_MATCH_TOO_FEW_ARGS|fixArgs;
          else
            return NO_MATCH_TOO_MANY_ARGS|fixArgs;
      }
    int arg_count = argTypes.length;
    Type elementType = null;
    Object[] restArray = null;
    int this_count = getStaticFlag() ? 0 : 1;
    Object[] rargs = new Object[mlength - this_count];
    Object thisValue;
    if (takesVarArgs)
      {
	Type restType = argTypes[arg_count-1];
	if (restType == Compilation.scmListType)
	  { // FIXME
	    rargs[rargs.length-1] = gnu.lists.LList.makeList(args, fixArgs);
	    nargs = fixArgs;
	  }
	else
	  {
	    ArrayType restArrayType = (ArrayType) restType;
	    elementType = restArrayType.getComponentType();
	    Class elementClass = elementType.getReflectClass();
	    restArray = (Object[])
	      java.lang.reflect.Array.newInstance(elementClass, nargs-fixArgs);
	    rargs[rargs.length-1] = restArray;
	  }
      }
    if (this_count != 0)
      thisValue = method.getDeclaringClass().coerceFromObject(ctx.getArgAsObject(0));
    else
      thisValue = null;
    for (int i = this_count;  i < nargs; i++)
      {
        try
          {
            Object arg = ctx.getArgAsObject(i);
            Type type = i < fixArgs ? argTypes[i-this_count] : elementType;
            if (type != Type.pointer_type)
              arg = type.coerceFromObject(arg);
            if (i < fixArgs)
              rargs[i-this_count] = arg;
            else
              restArray[i - fixArgs] = arg;
          }
        catch (ClassCastException ex)
          {
            return NO_MATCH_BAD_TYPE|i;
          }
      }
    ctx.value1 = thisValue;
    ctx.value2 = rargs;
    return 0;
  }

  public Object applyV (CallContext ctx) throws Throwable
  {
    int arg_count = argTypes.length;
    boolean is_constructor = op_code == 183;
    boolean is_static = getStaticFlag();

    try
      {
	if (member == null)
	  {
	    Class clas = method.getDeclaringClass().getReflectClass();
	    Class[] paramTypes = new Class[arg_count];
	    for (int i = arg_count; --i >= 0; )
	      paramTypes[i] = argTypes[i].getReflectClass();
	    if (is_constructor)
	      member = clas.getConstructor(paramTypes);
	    else
	      member = clas.getMethod(method.getName(), paramTypes);
	  }
        Object[] rargs = (Object[]) ctx.value2;
	if (is_constructor)
	  return ((java.lang.reflect.Constructor) member).newInstance(rargs);
	else
	  {
	    java.lang.reflect.Method meth = (java.lang.reflect.Method) member;
	    Object result = meth.invoke(ctx.value1, rargs);
            return retType.coerceToObject(result);
	  }
      }
    catch (java.lang.reflect.InvocationTargetException ex)
      {
	throw ex.getTargetException();
      }
  }

  public PrimProcedure(java.lang.reflect.Method method,
		       Class thisClass, Class[] parameterClasses,
		       Interpreter interpreter)
  {
    Type[] parameterTypes = new Type[parameterClasses.length];
    Type[] implParameterTypes = new Type[parameterClasses.length];
    for (int i = parameterClasses.length;  --i >= 0; )
      {
	Type ptype = interpreter.getTypeFor(parameterClasses[i]);
	parameterTypes[i] = ptype;
	implParameterTypes[i] = ptype.getImplementationType();
      }
    Type returnType = interpreter.getTypeFor(method.getReturnType());
    Type implReturnType = returnType.getImplementationType();
    ClassType thisType = (ClassType) interpreter.getTypeFor(thisClass);
    Method meth = thisType.addMethod(method.getName(), method.getModifiers(),
				     implParameterTypes, implReturnType);
    init(meth);
    argTypes = parameterTypes;
    retType = op_code == 183 ? meth.getDeclaringClass() : returnType;
  }

  public PrimProcedure(java.lang.reflect.Method method,
		       Interpreter interpreter)
  {
    this(method, method.getDeclaringClass(),
	 method.getParameterTypes(), interpreter);
  }

  public PrimProcedure(Method method)
  {
    init(method);
  }

  public PrimProcedure(Method method, java.util.Stack[] parameterCopies)
  {
    init(method);
    this.parameterCopies = parameterCopies;
  }

  private java.util.Stack[] parameterCopies;

  public PrimProcedure(Method method, Interpreter interpreter)
  {
    init(method);

    // This stuff deals with that a language may have its own mapping
    // from Java types to language types, for coercions and other reasons.
    Type[] pTypes = method.getParameterTypes();
    int nTypes = pTypes.length;
    argTypes = null;
    for (int i = nTypes;  --i >= 0; )
      {
	Type javaType = pTypes[i];
	Type langType = interpreter.getTypeFor(javaType.getReflectClass());
	if (javaType != langType)
	  {
	    if (argTypes == null)
	      {
		argTypes = new Type[nTypes];
		System.arraycopy(pTypes, 0, argTypes, 0, nTypes); 
	      }
	    argTypes[i] = langType;
	  }
      }
    if (argTypes == null)
      argTypes = pTypes;
    retType = op_code == 183 ? method.getDeclaringClass() :
      interpreter.getTypeFor(method.getReturnType().getReflectClass());
  }

  private boolean isConstructor()
  {
    return "<init>".equals(method.getName());
  }

  private void init(Method method)
  {
    this.method = method;
    this.argTypes = method.getParameterTypes();
    this.retType = method.getReturnType();
    int flags = method.getModifiers();
    if ((flags & Access.STATIC) != 0)
      this.op_code = 184;  // invokestatic
    else
      {
	ClassType mclass = method.getDeclaringClass();
	if ((mclass.getModifiers() & Access.INTERFACE) != 0)
	  this.op_code = 185;  // invokeinterface
	else if ("<init>".equals(method.getName()))
	  {
	    this.op_code = 183;  // invokespecial
	    this.retType = mclass;
	  }
	else
	  this.op_code = 182;  // invokevirtual
      }
  }

  public static PrimProcedure specialCall(Method method)
  {
    PrimProcedure res = new PrimProcedure(method);
    res.op_code = 183;
    return res;
  }

  public PrimProcedure(Method method, LambdaExp source)
  {
    this(method);
    this.source = source;
  }

  public PrimProcedure(int opcode, Type retType, Type[] argTypes)
  {
    this.op_code = opcode;
    this.retType = retType;
    this.argTypes= argTypes;
  }

  public static PrimProcedure makeBuiltinBinary(int opcode, Type type)
  {
    // FIXME - should cache!
    Type[] args = new Type[2];
    args[0] = type;
    args[1] = type;
    return new PrimProcedure(opcode, type, args);
  }

  public PrimProcedure(int op_code, ClassType classtype, String name,
		       Type retType, Type[] argTypes)
  {
    this.op_code = op_code;
    if (op_code == 185) // invokeinterface
      classtype.access_flags |= Access.INTERFACE;

    method = classtype.getDeclaredMethod(name, argTypes, retType);

    if (method == null)
      method = classtype.addMethod(name, 
				   op_code == 184 ? Access.STATIC : 0,
				   argTypes, retType);
    else
      if ((op_code == 184) != method.getStaticFlag())
	  throw new Error("Method "+method+
			  " should "+
			  (op_code == 184 ? "" : "not ")+
			  "be static");
    
    this.retType = retType;
    this.argTypes= argTypes;
  }

  /** Use to compile new followed by constructor. */
  public PrimProcedure(ClassType classtype, Type[] argTypes)
  {
    this(183, classtype, "<init>", Type.void_type, argTypes);
    this.retType = classtype;
  }

  public final boolean getStaticFlag()
  {
    return method == null || method.getStaticFlag() || isConstructor();
  }

  public final Type[] getParameterTypes() { return argTypes; }

  public static void compileArgs(Expression[] args,
				 Type thisType, Type[] argTypes,
				 boolean variable,
				 String name, LambdaExp source,
				 Compilation comp)
 {
   compileArgs(args, thisType, argTypes, variable, name, source, comp, 
               source == null ? null : source.parameterCopies);
 }

  /** Compile arguments and push unto stack.
   * @param args arguments to evaluate and push.
   * @param thisType If we are calling a non-static function,
   *   then args[0] is the receiver and thisType is its expected class.
   *   If thisType==Type.void_type, ignore argTypes[0].  (It is used to to
   *   pass a link to a closure environment, which was pushed by our caller.)
   *   If this_type==null, no special handling of args[0] or argTypes[0].
   */
  public static void compileArgs(Expression[] args,
				 Type thisType, Type[] argTypes,
				 boolean variable,
				 String name, LambdaExp source,
				 Compilation comp,
                                 java.util.Stack[] parameterCopies)
 {
    Type arg_type = null;
    gnu.bytecode.CodeAttr code = comp.getCode();
    int skipArg = thisType == Type.void_type ? 1 : 0;
    int arg_count = argTypes.length - skipArg;
    boolean is_static = thisType == null || skipArg != 0;
    int fix_arg_count = variable ? arg_count - (is_static ? 1 : 2)
      : args.length;
    Declaration argDecl = source == null ? null : source.firstDecl();

    Scope scope;
    if (parameterCopies == null)
      scope = null;
    else
      scope = code.pushScope();

    for (int i = 0; ; ++i)
      {
        if (variable && i == fix_arg_count)
          {
            arg_type = argTypes[arg_count-1+skipArg];
	    /*
	    if (arg_type == Compilation.scmListType)
	      {
		gnu.kawa.functions.MakeList.compile(args, i, comp);
		break;
	      }
	    */
            code.emitPushInt(args.length - fix_arg_count);
            arg_type = ((ArrayType) arg_type).getComponentType();
            code.emitNewArray(arg_type);
          }
        if (i >= args.length)
          break;
        if (i >= fix_arg_count)
          {
            code.emitDup(1); // dup array.
            code.emitPushInt(i - fix_arg_count);
          }
        else
          arg_type = argDecl != null && (is_static || i > 0) ? argDecl.getType()
	    : is_static ? argTypes[i + skipArg]
            : i==0 ? thisType
            : argTypes[i-1];
	Target target =
          StackTarget.getInstance(arg_type);
	args[i].compileNotePosition(comp, target);
	if (parameterCopies != null && parameterCopies[i] != null)
	  {
	    Variable value = scope.addVariable(code, target.getType(), 
                                               argDecl != null 
                                               ? argDecl.getName()
                                               : "l_" + i);
            parameterCopies[i].push(value);
	    code.emitDup();
	    code.emitStore(value);
	  }
        if (i >= fix_arg_count)
          code.emitArrayStore(arg_type);
	if (argDecl != null && (is_static || i > 0))
	  argDecl = argDecl.nextDecl();
      }

    // Restore the state of the captured parameters,
    // so that a subsequent after a nested call use is possible.
    if (parameterCopies != null)
      for (int i = 0; i < arg_count; i++)
        if (parameterCopies[i] != null)
          parameterCopies[i].pop();

    if (scope != null)
      code.popScope();
  }

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();

    if (isConstructor())
      {
	ClassType type = method.getDeclaringClass();
	code.emitNew(type);
	code.emitDup(type);
      }

    Expression[] args = exp.getArgs();
    String arg_error = WrongArguments.checkArgCount(this, args.length);
    if (arg_error != null)
      comp.error('e', arg_error);

    try {
      compile(getStaticFlag() ? null : method.getDeclaringClass(), args, comp, target);
    }
    catch (VerificationError e) {
      throw bossa.util.User.error(bossa.util.Location.make(exp), 
                                  e.getMessage());
    }
  }

  public void compile (Type thisType, Expression[] args, Compilation comp, Target target)
  {
    gnu.bytecode.CodeAttr code = comp.getCode();
    compileArgs(args, thisType, argTypes,
		takesVarArgs(), getName(), source, comp, parameterCopies);
    
    if (method == null)
      code.emitPrimop (opcode(), args.length, retType);
    else
      code.emitInvokeMethod(method, opcode());
    target.compileFromStack(comp, retType);
  }

  public Type getParameterType(int index)
  {
    if (! getStaticFlag())
      {
        if (index == 0)
          return method.getDeclaringClass();
        index--;
      }
    int lenTypes = argTypes.length;
    if (index < lenTypes - 1)
      return argTypes[index];
    boolean varArgs = takesVarArgs();
    if (index < lenTypes && ! varArgs)
      return argTypes[index];
    // if (! varArgs) ERROR;
    return ((ArrayType) argTypes[lenTypes - 1]).getComponentType();
  }

  // This is null in JDK 1.1 and something else in JDK 1.2.
  private static ClassLoader systemClassLoader
  = PrimProcedure.class.getClassLoader();

  public static PrimProcedure getMethodFor (Procedure pproc, Expression[] args)
  {
    return getMethodFor(pproc, null, args, Interpreter.getInterpreter());
  }

  /** Search for a matching static method in a procedure's class.
   * @return a PrimProcedure that is suitable, or null. */
  public static PrimProcedure getMethodFor (Procedure pproc, Declaration decl,
					    Expression[] args,
					    Interpreter interpreter)
  {
    Class pclass = getProcedureClass(pproc);
    if (pclass == null)
      return null;
    return getMethodFor(pclass, pproc.getName(), decl, args, interpreter);
  }

  public static Class getProcedureClass (Object pproc)
  {
    Class procClass;
    if (pproc instanceof gnu.expr.ModuleMethod)
      procClass = ((ModuleMethod) pproc).module.getClass();
    else
      procClass = pproc.getClass();
    try
      {
	if (procClass.getClassLoader() == systemClassLoader)
	  return procClass;
      }
    catch (SecurityException ex)
      {
      }
    return null;
  }

  /** Get PrimProcedure for matching method in given class. */
  public static PrimProcedure
  getMethodFor (Class procClass, String name, Declaration decl,
                Expression[] args, Interpreter interpreter)
  {
    try
      {
        java.lang.reflect.Method[] meths = procClass.getDeclaredMethods();
        java.lang.reflect.Method best = null;
        Class[] bestTypes = null;
        if (name == null)
          return null;
        String mangledName
	  = decl == null || decl.field == null ? Compilation.mangleName(name)
	  : decl.field.getName();
        String mangledNameV = mangledName + "$V";
        for (int i = meths.length;  --i >= 0; )
          {
            java.lang.reflect.Method meth = meths[i];
            int mods = meth.getModifiers();
            if ((mods & (Access.STATIC|Access.PUBLIC))
                != (Access.STATIC|Access.PUBLIC))
	      {
		if (decl == null || decl.base == null)
		  continue;
	      }
            String mname = meth.getName();
            boolean variable;
            if (mname.equals("apply") || mname.equals(mangledName))
              variable = false;
            else if (mname.equals("apply$V") || mname.equals(mangledNameV))
              variable = true;
            else
              continue;
            Class[] ptypes = meth.getParameterTypes();
            if (variable ? ptypes.length - 1 > args.length
                : ptypes.length != args.length)
              continue;
            // In the future, we may try to find the "best" match.
            if (best != null)
              return null;
            best = meth;
            bestTypes = ptypes;
          }
        if (best != null)
          {
            PrimProcedure prproc = new PrimProcedure(best, procClass,
						     bestTypes, interpreter); 
	    prproc.setName(name);
            return prproc;
          }
      }
    catch (SecurityException ex)
      {
      }
    return null;
  }

  public String getName()
  {
    String name = super.getName();
    if (name != null)
      return name;
    name = getVerboseName();
    setName(name);
    return name;
  }

  public String getVerboseName()
  {
    StringBuffer buf = new StringBuffer(100);
    if (method == null)
      {
	buf.append("<op ");
	buf.append(op_code);
	buf.append('>');
      }
    else
      {
	buf.append(method.getDeclaringClass().getName());
	buf.append('.');
	buf.append(method.getName());
      }
    buf.append('(');
    for (int i = 0; i < argTypes.length; i++)
      {
	if (i > 0)
	  buf.append(',');
	buf.append(argTypes[i].getName());
      }
    buf.append(')');
    return buf.toString();
  }


  public String toString()
  {
    StringBuffer buf = new StringBuffer(100);
    buf.append(retType.getName());
    buf.append(' ');
    buf.append(getVerboseName());
    return buf.toString();
  }

  public void print(java.io.PrintWriter ps)
  {
    ps.print("#<primitive procedure ");
    ps.print(toString());
    ps.print ('>');
  }
}
