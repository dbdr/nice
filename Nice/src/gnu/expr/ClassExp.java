package gnu.expr;
import gnu.bytecode.*;
import gnu.mapping.*;
import java.util.Vector;

public class ClassExp extends LambdaExp
{
  boolean simple;
  public boolean isSimple() { return simple; }
  public void setSimple(boolean value) { simple = value; }

  int accessFlags;
  public void setAccessFlags(int value) 
  { 
    accessFlags = value;
  }
  public boolean isInterface() 
  { 
    return (accessFlags & Access.INTERFACE) != 0; 
  }

  /** The class of instances of this class.
   * Same as super.type unless isMakingClassPair(), in which case super.type
   * is an interface, and instanceType is a class implementing the interface.
   * Using an interface plus a class gives us true multiple inheritance. */
  ClassType instanceType;

  boolean makeClassPair;

  public void setMakingClassPair(boolean val)
  {
    makeClassPair = val;
  }

  /** True if we should make a pair of an interface and a class. */
  public boolean isMakingClassPair()
  {
    return makeClassPair;
  }

  /** List of base classes and implemented interfaces. */
  public Expression[] supers;
  ClassExp superClass;

  public LambdaExp initMethod;

  public ClassExp ()
  {
    type = null;
    // Make sure we actually generate a class.
    setCanRead(true);
  }

  /**
     Create a ClassExp object to represent an existing class.
     This ClassExp will not be compiled to a new class.
  */
  public ClassExp (ClassType type)
  {
    this();
    this.type = this.instanceType = type;
  }

  private int fieldRank = 0;
  int numFieldsSuper;
  private Type[] constructorArgs;
  Type[] getConstructorArgs()
  {
    if (superClass != null && fieldRank < constructorArgs.length
	&& constructorArgs[fieldRank] == null)
      {
	Type[] sup = superClass.getConstructorArgs();
	numFieldsSuper = sup.length;
	System.arraycopy(constructorArgs, 0, constructorArgs, sup.length, constructorArgs.length - sup.length); 
	System.arraycopy(sup, 0, constructorArgs, 0, sup.length);
      }
    return constructorArgs;
  }

  public void setFieldCount(int count)
  {
    constructorArgs = new Type[count];
  }

  public Declaration addField(String name, Type type)
  {
    Declaration res = addDeclaration(name, type);
    res.setSimple(false);
    res.setFlag(Declaration.NONSTATIC_SPECIFIED);
    res.setCanRead(true);
    res.setCanWrite(true);
    res.noteValue(null);

    // If the class already exists, find the corresponding field there.
    if (instanceType != null)
      res.field = instanceType.getDeclaredField(name);

    addInitializer(new FieldInitializer(this, res, fieldRank));
    constructorArgs[fieldRank] = type;
    fieldRank++;

    return res;
  }

  /**
     Add a method to this class.
     @return the expression that should be used to call that method.
  */
  public ReferenceExp addMethod(LambdaExp method)
  {
    Declaration decl = new Declaration(method.getName());
    decl.noteValue(method);
    decl.setFlag(Declaration.IS_CONSTANT|Declaration.STATIC_SPECIFIED);
    decl.setProcedureDecl(true);
    method.nameDecl.context = this;

    method.nextSibling = this.firstChild;
    this.firstChild = method;

    return new ReferenceExp(decl);
  }

  /*
  public Object eval (Environment env)
  {
    System.err.println("eval ClassExp");
    Class clas = evalToClass();
    return Type.make(clas);
    }
  */

  public void compile (Compilation comp, Target target)
  {
    if (target instanceof IgnoreTarget)
      return;
    ClassType new_class = compile (comp);
    String className = new_class.getName();
    // Type.make(Class.forname)

    ClassType typeClass = ClassType.make("java.lang.Class");
    Method forNameClassMethod
      = typeClass.addMethod("forName", comp.string1Arg,
                            typeClass, Access.STATIC|Access.PUBLIC);
    gnu.bytecode.CodeAttr code = comp.getCode();
    code.emitPushString(className);
    code.emitInvokeStatic(forNameClassMethod);
    ClassType typeType;
    int nargs;
    boolean needsLink = getNeedsClosureEnv();
    if (isMakingClassPair() || needsLink)
      {
	code.emitPushString(instanceType.getName());
	code.emitInvokeStatic(forNameClassMethod);
	typeType = ClassType.make("gnu.expr.PairClassType");
	nargs = needsLink ? 3 : 2;
      }
    else
      {
	typeType = ClassType.make("gnu.bytecode.Type");
	nargs = 1;
      }
    Type[] argsClass = new Type[nargs];
    if (needsLink)
      {
	comp.curLambda.loadHeapFrame(comp);
	argsClass[--nargs] = Type.pointer_type;
      }
    while (--nargs >= 0) argsClass[nargs] = typeClass;
    Method makeMethod
      = typeType.addMethod("make", argsClass,
			   typeType, Access.STATIC|Access.PUBLIC);
    code.emitInvokeStatic(makeMethod);

    target.compileFromStack(comp, typeType);
  }

  public String getJavaName ()
  {
    return name == null ? "object" : Compilation.mangleNameIfNeeded (name);
  }

  public ClassType getCompiledClassType(Compilation comp)
  {
    if (! partsDeclared)
      {
	getType();
	declareParts(comp);
      }
    if (type.getName() == null)
      {
	String name = getName();
	if (name == null)
	  name = "object";
	else
	  {
	    int nlen = name.length();
	    if (nlen > 2
		&& name.charAt(0) == '<' && name.charAt(nlen-1) == '>')
	      name = name.substring(1, nlen-1);
	  }
	if (! isSimple() || this instanceof ObjectExp)
	  name = comp.generateClassName(name);
	else
	  // Keep the name if it is fully qualified, mangle it instead
	  if (name.indexOf('.') == -1)
	    name = comp.mangleNameIfNeeded(name);
	type.setName(name);
      }
    if (filename != null)
      type.setSourceFile(filename);
    if (! isInterface())
      comp.generateConstructor(getClassType(), this);
    return type;
  }

  private void setTypes()
  {
    int len = supers == null ? 0 : supers.length;
    ClassType[] superTypes = new ClassType[len];
    ClassType superType = null;
    int j = 0;
    for (int i = 0;  i < len;  i++)
      {
	Type st = Interpreter.getInterpreter().getTypeFor(supers[i]);
	if (st == null || ! (st instanceof ClassType))
	  throw new Error("invalid super type");
	ClassType t = (ClassType) st;
	if ((t.getModifiers() & Access.INTERFACE) == 0)
	  {
	    if (j < i)
	      throw new Error("duplicate superclass");
	    superType = t;
	    if (supers[i] instanceof ClassExp)
	      superClass = (ClassExp) supers[i];
	  }
	else
	  superTypes[j++] = t;
      }
    if (superType == null)
      {
	if (! isSimple())
	  {
	    PairClassType ptype = new PairClassType();
	    type = ptype;
	    setMakingClassPair(true);
	    instanceType = new ClassType();
	    type.setInterface(true);
	    ClassType[] interfaces = { type };
	    // Can we better.  FIXME.
	    instanceType.setSuper(Type.pointer_type);
	    instanceType.setInterfaces(interfaces);
	    ptype.instanceType = instanceType;
	  }
	else
	  instanceType = type = new ClassType(getName());
	type.setSuper(Type.pointer_type);
      }
    else
      {
	instanceType = type = new ClassType(getName());
	type.setSuper(superType);
      }
    // Access.SUPER mut be set on all non-interface classes
    if (! isInterface())
      accessFlags |= Access.SUPER;
    instanceType.setModifiers(accessFlags);

    if (j > 0)
      {
	ClassType[] interfaces;
	if (j == len)
	  interfaces = superTypes;
	else
	  {
	    interfaces = new ClassType[j];
	    System.arraycopy(superTypes, 0, interfaces, 0, j);
	  }
	type.setInterfaces(interfaces);
      }
  }

  public Type getType()
  {
    if (type == null)
      setTypes();
    return type;
  }

  public ClassType getClassType()
  {
    return (ClassType) getType();
  }

  boolean partsDeclared;

  public void declareParts(Compilation comp)
  {
    if (partsDeclared)
      return;
    partsDeclared = true;
    comp.topLambda = this;
    comp.topClass = this.type;
    for (Declaration decl = firstDecl();
	 decl != null;  decl = decl.nextDecl())
      {
	// If the declaration derives from a method, don't create field.
	if (decl.getCanRead())
	  {
	    int flags = Access.PUBLIC;
	    if (decl.getFlag(Declaration.STATIC_SPECIFIED))
	      flags |= Access.STATIC;
	    if (isMakingClassPair())
	      {
		flags |= Access.ABSTRACT;
		Type ftype = decl.getType().getImplementationType();
		type.addMethod(slotToMethodName("get", decl.getName()),
			       flags, Type.typeArray0, ftype);
		Type[] stypes = { ftype };
		type.addMethod(slotToMethodName("set",decl.getName()),
			       flags, stypes, Type.void_type);
	      }
	    else if (decl.value instanceof LambdaExp)
	      // This handles global variables with LambdaExp initial values.
	      {
		LambdaExp l = (LambdaExp) decl.value;
		l.outer = this;
		l.addMethodFor(type, comp, null);
		l.compileAsMethod(comp);
	      }
	    else
	      // This handles class fields.
	      {
		String fname
		  = Compilation.mangleNameIfNeeded(decl.getName());
		decl.field
		  = instanceType.addField(fname, decl.getType(), flags);
		decl.setSimple(false);
	      }
	  }
      }

    for (LambdaExp child = firstChild;  child != null;
	 child = child.nextSibling)
      {
	if (child != initMethod || ! isMakingClassPair())
	  child.addMethodFor(type, null, null);
	if (isMakingClassPair())
	  child.addMethodFor(instanceType, null, type);
      }
  }

  /** Return implementation method matching name and param types.
   * Used when compiling a pair class and generating a concrete method
   * implementing an interface method, to find static implementation method
   * in this or super implementation class we need to call.
   * @param interfaceType search the implementation classes corresponding
   *   to this interface type and its super-interfaces.
   * @param mname method name to look for.
   * @param paramTypes method types to look for.
   * @param vec where to place found methods
   * If a method is found, don't search super-interfaces, as the found method
   * is more specific and overrides any that might in super-interfaces.
   */
  static void getImplMethods(ClassType interfaceType,
			     String mname, Type[] paramTypes, Vector vec)
  {
    ClassType implType;
    if (interfaceType instanceof PairClassType)
      implType = ((PairClassType) interfaceType).instanceType;
    else if (! interfaceType.isInterface())
      return;
    else
      {
	String implTypeName = interfaceType.getName() + "$class";
	implType = ClassType.make(implTypeName);
      }
    Type[] itypes = new Type[paramTypes.length + 1];
    itypes[0] = interfaceType;
    System.arraycopy (paramTypes, 0, itypes, 1, paramTypes.length);
    Method implMethod = implType.getDeclaredMethod(mname, itypes);
    if (implMethod != null)
      {
	int count = vec.size();
	if (count == 0 || ! vec.elementAt(count-1).equals(implMethod))
	  vec.addElement(implMethod);
      }
    else
      {
	ClassType[] superInterfaces = interfaceType.getInterfaces();
	for (int i = 0;  i < superInterfaces.length;  i++)
	  getImplMethods(superInterfaces[i], mname, paramTypes, vec);
      }
  }

  public ClassType compile (Compilation comp)
  {
    ClassType saveClass = comp.curClass;
    Method saveMethod = comp.method;
    try
      {
	ClassType new_class = getCompiledClassType(comp);
	comp.curClass = new_class;

	String filename = getFile();
	if (filename != null)
	  new_class.setSourceFile (filename);

	LambdaExp saveLambda = comp.curLambda;
	comp.curLambda = this;

	allocFrame(comp);
	if (getNeedsStaticLink())
	  {
            Variable parentFrame = saveLambda.heapFrame != null
              ? saveLambda.heapFrame
              : saveLambda.closureEnv;
            if (parentFrame != null)
              closureEnvField = staticLinkField
                = instanceType.addField("this$0", parentFrame.getType());
	  }
	comp.generateConstructor(instanceType, this);
	CodeAttr code;

	for (LambdaExp child = firstChild;  child != null; )
	  {
	    Method save_method = comp.method;
	    LambdaExp save_lambda = comp.curLambda;
	    comp.method = child.getMainMethod();
	    //comp.curClass = comp.method.getDeclaringClass();
	    child.declareThis(comp.curClass);
	    comp.curClass = instanceType;
	    comp.curLambda = child;
	    comp.method.initCode();
            child.allocChildClasses(comp);
	    child.allocParameters(comp);
	    child.enterFunction(comp);
	    child.compileBody(comp);
	    child.compileEnd(comp);
	    child.compileChildMethods(comp);
	    comp.method = save_method;
	    comp.curClass = new_class;
	    comp.curLambda = save_lambda;
	    child = child.nextSibling;
	  }

	Method[] methods = type.getMethods(AbstractMethodFilter.instance, 2);
	for (int i = 0;  i < methods.length;  i++)
	  {
	    Method meth = methods[i];
	    String mname = meth.getName();
	    Type[] ptypes = meth.getParameterTypes();
	    Type rtype = meth.getReturnType();

	    Method mimpl = instanceType.getMethod(mname, ptypes);
	    if (mimpl != null && ! mimpl.isAbstract())
	      continue;

	    char ch;
	    if (mname.length() > 3
		&& mname.charAt(2) == 't'
		&& mname.charAt(1) == 'e'
		&& ((ch = mname.charAt(0)) == 'g' || ch == 's'))
	      {
		Type ftype;
		if (ch == 's' && rtype.isVoid() && ptypes.length == 1)
		  ftype = ptypes[0];
		else if (ch == 'g' && ptypes.length == 0)
		  ftype = rtype;
		else
		  continue;
		String fname = Character.toLowerCase(mname.charAt(3))
		  + mname.substring(4);
		Field fld = instanceType.getField(fname);
		if (fld == null)
		  fld = instanceType.addField(fname, ftype, Access.PUBLIC);
		Method impl = instanceType.addMethod(mname, Access.PUBLIC,
						     ptypes, rtype);
		impl.init_param_slots ();
		code = impl.getCode();
		code.emitPushThis();
		if (ch == 'g')
		  {
		    code.emitGetField(fld);
		  }
		else
		  {
		    code.emitLoad(code.getArg(1));
		    code.emitPutField(fld);
		  }
		code.emitReturn();
	      }
	    else
	      {
		Vector vec = new Vector();
		getImplMethods(type, mname, ptypes, vec);
		if (vec.size() != 1)
		  {
		    // FIXME - need better error message!
		    String msg = vec.size() == 0
		      ? "missing implementation for "
		      : "ambiguous implementation for ";
		    comp.error('e', msg+meth);
		  }
		else
		  {
		    Method impl = instanceType.addMethod(mname, Access.PUBLIC,
							 ptypes, rtype);
		    impl.init_param_slots ();
		    code = impl.getCode();
		    for (Variable var = code.getCurrentScope().firstVar();
			 var != null;  var = var.nextVar())
		      code.emitLoad(var);
		    Method imethod = (Method) vec.elementAt(0);
		    code.emitInvokeStatic(imethod);
		    code.emitReturn();
		  }
	      }
	  }

	comp.curLambda = saveLambda;

	return new_class;
      }
    finally
      {
	comp.curClass = saveClass;
	comp.method = saveMethod;
      }
  }

  /**
     Return code to create an instance of this class.
     The instance is initialized by calling the default constructor.
  */
  public final Expression instantiate ()
  {
    return new QuoteExp(new PrimProcedure(getDefaultConstructor()));
  }

  /**
     Return code to create an instance of this class.
     The instance is initialized by calling the default constructor.
  */
  public final Expression instantiateDefault ()
  {
    return new QuoteExp(new PrimProcedure(getDefaultConstructor()));
  }

  private Method getDefaultConstructor()
  {
    ClassType clas = getClassType();
    return Compilation.getConstructor(clas, this);
  }

  void compileChildMethods (Compilation comp)
  {
    // We set comp.curClass, so that literals are emited in the right class.
    ClassType save_class = comp.curClass;
    comp.curClass = this.type;
    
    super.compileChildMethods(comp);
    setFieldValues(comp);

    comp.curClass = save_class;
  }

  private void setFieldValues(Compilation comp)
  {
    for (Declaration decl = firstDecl();
	 decl != null;  decl = decl.nextDecl())
      {
	if (decl.field != null)
	  {
	    decl.setFieldValue(comp);
	  }
      }
  }

  protected Expression walk (ExpWalker walker)
  {
    return walker.walkClassExp(this);
  }

  protected void walkChildren(ExpWalker walker)
  {
    LambdaExp save = walker.currentLambda;
    walker.currentLambda = this;
    try
      {
	for (LambdaExp child = firstChild;
	     child != null && walker.exitValue == null;
	     child = child.nextSibling)
	  walker.walkLambdaExp(child);
      }
    finally
      {
	walker.currentLambda = save;
      }
  }

  public void print (OutPort out)
  {
    out.startLogicalBlock("("+getExpClassName()+"/", ")", 2);
    if (name != null)
      {
	out.print(name);
	out.print('/');
      }
    out.print(id);
    out.print("/ (");
    Special prevMode = null;
    int i = 0;
    int opt_i = 0;
    int key_args = keywords == null ? 0 : keywords.length;
    int opt_args = defaultArgs == null ? 0 : defaultArgs.length - key_args;
    for (Declaration decl = firstDecl();  decl != null; decl = decl.nextDecl())
      {
	if (i > 0)
	  out.print(' ');
	out.print(decl);
	i++;
      }
    out.print(") ");
    for (LambdaExp child = firstChild;  child != null;
	 child = child.nextSibling)
      {
	out.writeSpaceLinear();
        out.print(" method: ");
        child.print(out);
      }
    out.writeSpaceLinear();
    if (body == null)
      out.print("<null body>");
    else
      body.print (out);
    out.endLogicalBlock(")");
  }

  public Field compileSetField (Compilation comp)
  {
    return (new ClassInitializer(this, comp)).field;
  }

  /** Mangle a "slot" name to a get- or set- method name. */
  public static String slotToMethodName(String prefix, String sname)
  {
    StringBuffer sbuf = new StringBuffer(sname.length()+3);
    sbuf.append(prefix);
    sbuf.append(Character.toTitleCase(sname.charAt(0)));
    sbuf.append(sname.substring(1));
    return sbuf.toString();
  }
}

class AbstractMethodFilter implements gnu.bytecode.Filter
{
  public static AbstractMethodFilter instance = new AbstractMethodFilter();

  public boolean select(Object value)
  {
    gnu.bytecode.Method method = (gnu.bytecode.Method) value;
    return method.isAbstract();
  }
}
