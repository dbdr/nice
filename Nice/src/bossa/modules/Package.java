/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.modules;

import bossa.util.*;
import bossa.syntax.*;
import gnu.bytecode.*;
import gnu.expr.*;
import nice.tools.code.Import;

import java.util.*;
import java.util.jar.*;
import java.io.*;

import bossa.util.Location;
import gnu.expr.ClassExp;

/**
   A Nice package.
   
   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */
public class Package implements mlsub.compilation.Module, Located, bossa.syntax.Module
{
  /****************************************************************
   * Loading
   ****************************************************************/

  public static Package make(String name, 
			     Compilation compilation,
			     boolean isRoot)
  {
    return make(new LocatedString(name, Location.option), 
		compilation, isRoot);
  }
  
  public static Package make(LocatedString lname, 
			     Compilation compilation,
			     boolean isRoot)
  {
    String name = lname.toString();

    Package res = (Package) compilation.packages.get(name);
    if (res != null)
      return res;
    
    return new Package(lname, compilation, isRoot);
  }

  /****************************************************************
   * Single Constructor
   ****************************************************************/
  
  private Package(LocatedString name, 
		  Compilation compilation,
		  boolean isRoot)
  {
    this.name = name;
    this.compilation = compilation;
    this.isRoot = isRoot;

    compilation.packages.put(name.toString(), this);

    source = compilation.locator.find(this);
    if (source == null)
      User.error(name, "Could not find package " + name);

    loadImports();

    prepareCodeGeneration();

    read(compilation.recompileAll || 
	 isRoot && compilation.recompileCommandLine);
  }

  private void loadImports()
  {
    Set opens = new TreeSet();
    opens.add("java.lang");
    opens.add("java.util");

    imports = source.getImports(opens);

    if (!name.toString().equals("nice.lang") && !Debug.ignorePrelude)
      imports.add(new LocatedString("nice.lang", 
				    bossa.util.Location.nowhere()));
    
    setOpens(opens);

    List p = this.getImports();
    for (Iterator i = p.iterator(); i.hasNext();)
      source.someImportModified(((Package) i.next()).lastModification());
  }

  /**
     @param shouldReload reload if the source if available.
   **/
  private void read(boolean shouldReload)
  {
    Module oldModule = Definition.currentModule;
    Definition.currentModule = this;
    Node.setModule(this);
    
    compilation.progress(this, "parsing");

    definitions = new ArrayList();
    source.getDefinitions(definitions, shouldReload);
    this.ast = new AST(this, definitions);
    definitions = null;
    compilation.addNumberOfDeclarations(ast.numberOfDeclarations());

    if (compiling())
      // Inform compilation that at least one package is going to generate code
      compilation.recompilationNeeded = true;

    Definition.currentModule = oldModule;
  }
  
  void setOpens(Set opens)
  {
    // when we import a Nice package, we also open it.
    for (Iterator i = imports.iterator(); i.hasNext();)
      opens.add(((LocatedString) i.next()).toString());

    int len = opens.size();
    this.opens = (String[]) opens.toArray(new String[len + 1]);
    // We must guarantee that this package is the first element of 'open'.
    this.opens[len] = this.opens[0];
    this.opens[0] = this.name.toString();
  }

  public long lastModification()
  {
    return source.lastModification;
  }
  
  private void readAlternatives()
  {
    for(Method method = source.getBytecode().getMethods();
	method != null;
	method = method.getNext())
      bossa.link.ImportedAlternative.read(source.getBytecode(), method,
					  location());
  }
  
  /****************************************************************
   * Package dependencies
   ****************************************************************/
  
  public List /* of Package */ getRequirements()
  {
    return getImports();
  }
  
  public String[] listImplicitPackages()
  {
    return opens;
  }
  
  /** List of the packages implicitely opened, a la 'import pkg.*'.
      The first element must be this package itself.
   */
  private String[] opens;
  private List /* of LocatedString */ imports;
  private List /* of Package */ importedPackages;

  private List getImports()
  {
    if (importedPackages == null)
      computeImportedPackages();
    
    return importedPackages; 
  }
  
  private void computeImportedPackages()
  {
    importedPackages = new ArrayList(imports.size());

    for(Iterator i = imports.iterator(); i.hasNext();)
      {
	LocatedString s = (LocatedString) i.next();
	importedPackages.add(make(s, compilation, false));
      }
  }

  /****************************************************************
   * Progress
   ****************************************************************/

  /** The average contribution to compile time of the different phases.
      The sum should be 1.0 
  */
  private static float
    PROGRESS_SCOPE           = 0.04f,
    PROGRESS_LOAD            = 0.04f,
    PROGRESS_TYPED_RESOLVE   = 0.04f,
    PROGRESS_LOCAL_RESOLVE   = 0.04f,
    PROGRESS_TYPECHECK       = 0.50f,
    PROGRESS_GENERATE_CODE   = 0.10f,
    PROGRESS_SAVE_INTERFACE  = 0.04f,
    PROGRESS_LINK            = 0.20f;

  void addProgress(float weight)
  {
    compilation.addProgress(weight * ast.numberOfDeclarations());
  }

  void addGlobalProgress(float weight)
  {
    compilation.addProgress(weight * compilation.getNumberOfDeclarations());
  }

  /****************************************************************
   * Passes
   ****************************************************************/

  private boolean scoped = false;

  public void scope()
  {
    if (scoped) return;
    scoped = true;

    ast.buildScope();

    addProgress(PROGRESS_SCOPE);
  }
  
  public void load()
  {
    ast.resolveScoping();

    // this must be done before freezing
    if (!compiling())
      readAlternatives();

    addProgress(PROGRESS_LOAD);
  }

  public void typedResolve()
  {
    ast.typedResolve();

    addProgress(PROGRESS_TYPED_RESOLVE);
  }

  public void localResolve()
  {
    ast.localResolve();

    addProgress(PROGRESS_LOCAL_RESOLVE);
  }

  public void compile()
  {
    compilation.exitIfErrors();
    generateCode();

    addProgress(PROGRESS_GENERATE_CODE);

    saveInterface();

    addProgress(PROGRESS_SAVE_INTERFACE);
  }

  public static void startNewCompilation()
  {
    // Perform resets to discard static information gathered during the 
    // previous compilation. This is a workaround, full non-staticness
    // would be better.

    mlsub.typing.Typing.startNewCompilation();
    nice.tools.code.Types.reset();
    nice.tools.code.SpecialTypes.init();
    bossa.link.Alternative.reset();
    bossa.link.Dispatch.reset();
    ClassDefinition.reset();
    TypeConstructors.reset();
    JavaClasses.reset();
    gnu.bytecode.Type.reset();
  }

  public void freezeGlobalContext()
  {
    contextFrozen = true;

    mlsub.typing.Typing.createInitialContext();
  }

  public void unfreezeGlobalContext()
  {
    contextFrozen = false;
    mlsub.typing.Typing.releaseInitialContext();
  }

  private static boolean contextFrozen;
  public static boolean contextFrozen()
  {
    return contextFrozen;
  }
  
  public void typecheck()
  {
    // An interface file does not have to be typecheked.
    // It is known to be type correct from previous compilation!
    // We still call ast.typechecking, but with a value telling
    // that only bookeeping tasks are needed, and we don't advertise
    // this pass.
    if (compiling())
      compilation.progress(this, "typechecking");
    
    ast.typechecking(compiling());

    addProgress(PROGRESS_TYPECHECK);
  }

  public void link()
  {
    if (!isRoot)
      return;

    // If at least one package is recompiled, the root will also be recompiled
    if (compiling())
      {
        compilation.progress(this, "linking");
	bossa.link.Dispatch.test(this);

	finishCompilation();

	compilation.exitIfErrors();
      }

    addGlobalProgress(PROGRESS_LINK);

    // Write the archive even if nothing was compiled.
    // This is useful to bundle the application after it was compiled.
    writeArchive();
  }

  /**
     Save the dispatch class generated during link,
     and ask imported packages to do so too.
  */
  private void finishCompilation()
  {
    gnu.expr.Package pkg = compilePackages(null);

    try {
      pkg.compileToFiles();
      pkg = null;
    }
    catch(IOException e) {
      User.error(this.name, "Error during creation of bytecode files:\n" + e);
    }
  }

  /**
     Recursive traversal of the import graph.

     Root package are compiled first.

     Setting module to false allows to reclaim memory, 
     and avoids infinite recursion.
  */
  private gnu.expr.Package compilePackages(gnu.expr.Package res)
  {
    if (dispatchClass == null)
      return res;

    if (compiling())
      // Force generation of the package class.
      // We might not need to do that forever, but at the moment, a compiled 
      // package is ignored when "fun.class" is missing.
      getImplementationClass();

    // The implementation class is null if this package was up-to-date.
    if (implementationClass != null)
      thisPkg.addClass(implementationClass);

    thisPkg.addClass(dispatchClass);
    thisPkg.directory = source.getOutputDirectory();

    this.implementationClass = null;
    this.dispatchClass = null;

    thisPkg.next = res;
    res = thisPkg;

    // Allow memory to be reclaimed early.
    thisPkg = null;
    ast = null;

    for (Iterator i = getImports().iterator(); i.hasNext();)
      res = ((Package) i.next()).compilePackages(res);

    return res;
  }

  private void saveInterface()
  {
    // do not save the interface 
    // if this package already comes from an interface file
    if (!compiling())
      return;

    File dir = source.getOutputDirectory();

    try{
      PrintWriter f = new PrintWriter
	(new BufferedWriter(new FileWriter(new File(dir, "package.nicei"))));
      f.print("package "+name+";\n\n");
      
      for(Iterator i = getImports().iterator(); i.hasNext();)
	{
	  Package m = (Package) i.next();
	  f.print("import "+m.getName()+";\n");
	}
      f.println();

      for(int i = 0; i < opens.length; i++)
	{
	  f.print("import " + opens[i] + ".*" + 
		   (Import.isStrictPackage(opens[i]) ? "(!)" : "") + ";\n");
	}
      f.println();

      ast.printInterface(f);
      f.close();
    }
    catch(IOException e){
      User.warning(name,
		   "Could not save the interface of package "+name);
    }
  }

  /****************************************************************
   * Archive
   ****************************************************************/

  private void writeArchive()
  {
    if (compilation.output == null)
      return;

    File jarFile = createJarFile();

    try
      {
	JarOutputStream jarStream = createJarStream(jarFile);
	if (! compilation.excludeRuntime)
	  writeRuntime(compilation.runtimeFile, jarStream);
	// The link was performed iff this package (root) was recompiled.
	this.addToArchive(jarStream, compiling());
	jarStream.close();
      }
    catch(Exception e)
      {
	// The jar file was not completed
	// it must be corrupt, so it's cleaner to delete it
	jarFile.delete();

	if (e instanceof RuntimeException)
	  throw (RuntimeException) e;
	else
	  User.error(this, "Error while writing archive (" +
		     e.getMessage() + ")", e);
      }
  }
  
  private File createJarFile()
  {
    if (!compilation.output.endsWith(".jar"))
      compilation.output = compilation.output + ".jar";
    
    File jarFile = new File(compilation.output);

    // Create the directory if necessary
    File parent = jarFile.getParentFile();
    // The parent is null (i.e. current dir) if it is not specified. 
    if (parent != null)
      parent.mkdirs();

    return jarFile;
  }

  private JarOutputStream createJarStream(File jarFile)
  throws IOException
  {
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");
    manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, 
                                     this.name + ".dispatch");
    return new JarOutputStream(new FileOutputStream(jarFile), manifest);
  }

  /**
     Write all base classes that are needed to run generated code.
  */
  private static void writeRuntime(String runtimeJar, JarOutputStream jarStream)
  throws IOException
  {
    JarFile runtime = null;
    
    if (runtimeJar != null)
      try {
	runtime = new JarFile(runtimeJar);
      } catch(java.util.zip.ZipException e) {}

    if (runtime == null)
      {
	Internal.warning("Runtime was not found. The archive is not self-contained");
	return;
      }

    // add individual classes
    String[] classes = 
    {
      "gnu/mapping/Procedure.class", "gnu/mapping/Procedure0.class", "gnu/mapping/Procedure1.class", "gnu/mapping/Procedure2.class", "gnu/mapping/Procedure3.class", "gnu/mapping/ProcedureN.class", "gnu/mapping/Named.class", "gnu/mapping/Printable.class", "gnu/mapping/WrongArguments.class", "gnu/mapping/WrongType.class", "gnu/mapping/WrappedException.class", 
      "gnu/expr/ModuleBody.class", "gnu/expr/ModuleMethod.class"
    };

    for (int i = 0; i < classes.length; i++)
      {
	JarEntry entry = runtime.getJarEntry(classes[i]);
	if (entry == null)
	  System.out.println("Runtime: " + classes[i] + " not found");
	else
	  addEntry(classes[i], runtime.getInputStream(entry), jarStream);
      }
  }

  private boolean addedToArchive;

  private void addToArchive(JarOutputStream jarStream, boolean linkPerformed)
  throws IOException
  {
    if (addedToArchive)
      return;
    addedToArchive = true;

    compilation.progress(this, "writing in archive");

    String packagePrefix = getName().replace('.', '/') + "/";
    Content.Stream[] classes = source.getClasses(linkPerformed);

    for (int i = 0; i < classes.length; i++)
      addEntry(packagePrefix + classes[i].name, classes[i].stream, jarStream);

    for (Iterator i = getImports().iterator(); i.hasNext();)
      ((Package) i.next()).addToArchive(jarStream, linkPerformed);
  }

  private static void addEntry(String name, InputStream data, 
			       JarOutputStream out)
  throws IOException
  {
    out.putNextEntry(new JarEntry(name));
    copy(data, out);
  }

  private static void copy(InputStream in, OutputStream out)
  throws IOException
  {
    int size = in.available();
    if (size < 1024) size = 1024;
    byte[] buf = new byte[size];
    
    try{
      int read;
      do
	{
	  read = in.read(buf);
	  if (read > 0)
	    out.write(buf, 0, read);
	}
      while (read != -1);
    }
    finally{
      in.close();
    }
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  /** The name of the class package functions and method implementations
      are stored in. 
  **/
  static final String packageClassName = "fun";

  public ClassExp getClassExp(NiceClass def)
  {
    if (compiling())
      return def.createClassExp();
    
    String name = def.getName().toString();
    ClassType classe = source.readClass(name);
    if (classe == null)
      Internal.error("Compiled class " + def + " was not found");
    importMethods(def, classe);
    
    ClassExp res = new ClassExp(classe);
    addUserClass(res);
    return res;
  }

  /** Load methods compiled in a class (custom constructors for now). */
  private void importMethods(NiceClass def, ClassType classe)
  {
    for(Method method = classe.getMethods();
	method != null;
	method = method.getNext())
      {
        Definition d = CustomConstructor.load(def, method);
        if (d != null)
          definitions.add(d);
      }
  }

  public void addUserClass(gnu.expr.ClassExp classe)
  {
    thisPkg.addClass(classe);

    // A class only needs an outer frame if we are compiling the package.
    if (compiling())
      classe.outer = getImplementationClass();
  }

  public void addGlobalVar(gnu.expr.Declaration decl, 
                           boolean constant)
  {
    if (!compiling())
      // The code is already there
      {
	decl.setSimple(false);
	decl.field = source.getBytecode().getField(decl.getName());

	if (decl.field == null)
	  Internal.error(this,
			 "The compiled file is not consistant with the interface file for global variable " + decl.getName());
      }
    else
      {
	getImplementationClass().addDeclaration(decl);
        if (constant) decl.setFlag(Declaration.IS_CONSTANT);

	decl.setFlag(Declaration.STATIC_SPECIFIED|Declaration.TYPE_SPECIFIED);
      }
  }

  private gnu.expr.Package thisPkg;
  private ClassExp implementationClass, dispatchClass;

  private ClassExp createClassExp(String name)
  {
    ClassExp res = new ClassExp();
    res.setName(name);
    res.setSimple(true);
    res.body = QuoteExp.voidExp;
    // This is not true, but useful to make some Java-centric tools happy.
    // The real file info in the the SourceDebugExtension attribute.
    res.setFile(name.substring(name.lastIndexOf('.') + 1) + ".nice");
    res.needsConstructor = true;
    return res;
  }

  /**
   * Transform the name of a class to its
   * fully qualified name.
   */
  public static String className(String name)
  {
    return name;
  }

  static
  {
    nice.tools.code.NiceInterpreter.init();
  }

  private void prepareCodeGeneration()
  {
    thisPkg = new gnu.expr.Package(getName());

    dispatchClass = createClassExp(name + ".dispatch");
    dispatchClass.addBytecodeAttribute(MiscAttr.synthetic());
  }

  /** Creates the implementation class lazily. */
  private ClassExp getImplementationClass()
  {
    if (implementationClass == null)
      implementationClass = createClassExp(name + "." + packageClassName);

    return implementationClass;
  }

  /**
   * Creates bytecode for the alternatives defined in the module.
   */
  private void generateCode()
  {
    if (compiling())
      compilation.progress(this, "generating code");
    
    ast.compile(compiling());
  }

  public ClassType createClass(String name)
  {
    // If we use new ClassType(), we may end up with two different 
    // objects representing this class.
    // However if the class exists but is invalid, we create a new one.

    String className = this.name + "." + name;
    ClassType res;
    try{
      res = ClassType.make(className);
    }
    catch(java.lang.LinkageError e){
      res = new ClassType(className);
    }

    res.setExisting(false);
    return res;
  }

  /**
     @return the bytecode method with this (unique) name
     if the package has not been recompiled.
  */
  private Method lookupClassMethod(ClassType clas, String name,
				   String attribute, String value)
  {
    if (clas == null)
      return null;

    name = nice.tools.code.Strings.escape(name);
    
    for (Method m = clas.getDeclaredMethods(); m != null; m = m.getNext())
      if (m.getName().equals(name)
	  // in rare cases, the method name might have been made unique
	  // in the bytecode by appending "$..."
          // but names appended with "$$..." may not be matched because
          // that are escape characters
	  || m.getName().startsWith(name)
	  && m.getName().charAt(name.length()) == '$'
          && m.getName().charAt(name.length()+1) != '$')
	{
	  MiscAttr attr = (MiscAttr) Attribute.get(m, attribute);
	  if (attr != null && new String(attr.data).equals(value))
	    return m;
	}

    return null;
  }
  
  public gnu.expr.Expression getDispatchMethod(NiceMethod def)
  {
    String name = def.getName().toString();
    LambdaExp res;
    Type[] argTypes;
    Type retType;

    /*
      If this package is not recompiled,
      we fetch the bytecode type information
      from the previous dispatch class.
      Benefits: we get the most precise bytecode type for methods,
        as computed during the initial compilation.
        This would not be the case if we recomputed it,
	as the precise types are found during typechecking.
    */
    Method meth = lookupClassMethod(source.getDispatch(), name,
				    "id", def.getFullName());
    if (meth != null) // Reuse existing dispatch method header
      {
	// The dispatch code will have to be regenerated anyway
	meth.eraseCode();

	argTypes = meth.arg_types;
	retType  = meth.return_type;
        // Make sure we use the same bytecode name, since compiled code
        // can rely on it.
        name = meth.getName();
      }
    else // Get type information from the nice declaration
      {
	argTypes = def.javaArgTypes();
	retType  = def.javaReturnType();
      }

    // Try to compile the dispatch method as a member method if possible.
    NiceClass receiver;
    if (argTypes.length == 0)
      receiver = null;
    else
      {
        receiver = NiceClass.get(def.getArgTypes()[0]);

        // JVM interfaces cannot contain code.
        if (receiver != null && receiver.isInterface())
          receiver = null;

        // Special treatment for serialization at the moment.
        if (def.getArity() == 2 && 
            (name.equals("writeObject")||name.equals("readObject")))
          receiver = null;
      }

    res = nice.tools.code.Gen.createMethod
      (name, argTypes, retType, def.getSymbols(), true, receiver != null);
    res.parameterCopies = def.formalParameters().getParameterCopies();

    // add unique information to disambiguate which method this represents
    res.addBytecodeAttribute
      (new MiscAttr("id", def.getFullName().getBytes()));

    if (receiver != null)
      return receiver.addJavaMethod(res);
    else
      return addMethod(res, false);
  }

  /** Add a method to this package and return an expression to refer it. */
  public ReferenceExp addMethod(LambdaExp method, boolean packageMethod)
  {
    ClassExp classe = packageMethod ? getImplementationClass() : dispatchClass;
    return classe.addMethod(method);
  }

  public String bytecodeName()
  {
    return name.toString();
  }
  
  /****************************************************************
   * Misc
   ****************************************************************/

  public Location location()
  {
    return name.location();
  }
  
  public String getName()
  {
    return name.toString();
  }
  
  public String toString()
  {
    return "package " + name;
  }
  
  public AST getDefinitions()
  {
    return ast;
  }

  public LocatedString name;
  
  /** True if this package was specified on the command line. */
  private boolean isRoot;
  
  /** The list of definitions. Used to add global definitions on the fly. */
  private List definitions;

  private AST ast;

  /** The "source" where this package resides. */
  private Content source;
  
  /** The compilation that is in process. */
  Compilation compilation;
  
  public Compilation compilation() { return compilation; }

  /** 
      @return true if this package was loaded from an interface file,
      not a source file
  */
  public boolean interfaceFile()
  {
    return !compiling();
  }

  /** 
      @return true if this package is recompiled.
  */
  public boolean compiling()
  {
    return source.sourceRead;
  }

  /****************************************************************
   * Static link to the current Compilation object (not thread safe!)
   ****************************************************************/

  public static Compilation currentCompilation;
}
