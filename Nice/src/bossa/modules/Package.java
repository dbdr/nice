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

import java.util.*;
import java.util.jar.*;
import java.io.*;

import bossa.util.Location;
import gnu.expr.ClassExp;

/**
   A Nice package.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
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
    return make(new LocatedString(name, bossa.util.Location.nowhereAtAll()), 
		compilation, isRoot);
  }
  
  private static final Map map = new HashMap();
  
  public static Package make(LocatedString lname, 
			     Compilation compilation,
			     boolean isRoot)
  {
    String name = lname.toString();

    Package res = (Package) map.get(name);
    if (res != null)
      return res;
    
    try{
      return new Package(lname, compilation, isRoot);
    }
    catch(ExceptionInInitializerError e){
      e.getException().printStackTrace();
      Internal.error("Exception in initializer: "+e.getException());
      return null;
    }
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
    
    map.put(name.toString(),this);

    imports = new LinkedList();
    
    if (!name.toString().equals("nice.lang") && !Debug.ignorePrelude)
      imports.add(new LocatedString("nice.lang", 
				    bossa.util.Location.nowhere()));
    
    source = compilation.locator.find(this);
    if (source == null)
      User.error(name, "Could not find package " + name);

    read(compilation.recompileAll || 
	 isRoot && compilation.recompileCommandLine, false);
    
    computeImportedPackages();

    thisPkg = new gnu.expr.Package(getName());
  }

  /**
     @param shouldReload reload if the source if available.
     @param mustReload   fail if source is not available.
   **/
  private void read(boolean shouldReload, boolean mustReload)
  {
    Module oldModule = Definition.currentModule;
    Definition.currentModule = this;
    
    List definitions = new LinkedList();
    TreeSet opens = new TreeSet();
    opens.add(this.name.toString());
    opens.add("java.lang");    

    if (Debug.passes)
      Debug.println(this + ": parsing " + source);

    Content.Unit[] readers = source.getDefinitions(shouldReload, mustReload);
    if (compiling())
      // Inform compilation that at least one package is going to generate code
      compilation.recompilationNeeded = true;
    
    for(int i = 0; i<readers.length; i++)
      read(readers[i], definitions, opens);
    
    this.ast = new AST(this, expand(definitions));

    // when we import a bossa package, we also open it.
    for (Iterator i = imports.iterator(); i.hasNext();)
      opens.add(((LocatedString) i.next()).toString());
    this.opens = (String[]) opens.toArray(new String[opens.size()]);

    // if we are root, and read the AST from an interface file, 
    // we don't know yet if we are runnable
    if (isRoot && !compiling())
      isRunnable(alternativesHaveMain());
    
    Definition.currentModule = oldModule;
  }
  
  public long lastModification()
  {
    return source.lastModification;
  }
  
  public void lastModifiedRequirement(long date)
  {
    // If we've already loaded the source
    // or none of our imports changed since last time we compiled,
    // there is nothing to do
    if (compiling() || date <= source.lastCompilation)
      return;
    
    if (Debug.modules && date > source.lastCompilation)
      Debug.println
      (this + " was compiled " + new java.util.Date(source.lastCompilation) + 
       "\nA required package changed " + new java.util.Date(date) );

    read(true, true);
  }
  
  private static List expand(List definitions)
  {
    Collection ads = new LinkedList();
    for(Iterator i = definitions.iterator(); i.hasNext();)
      {
	Definition d = (Definition) i.next();
	Collection c = d.associatedDefinitions();
	if (c!=null)
	  ads.addAll(c);
      }
    definitions.addAll(ads);
    return definitions;
  }
  
  private void read(Content.Unit unit, List definitions, Set opens)
  {
    bossa.util.Location.setCurrentFile(unit.name);

    LocatedString pkgName = 
      bossa.parser.Loader.open(unit.reader,
			       definitions, imports, opens);
    if (pkgName!=null && !pkgName.equals(this.name))
      User.error(pkgName,
		 source + " declares it belongs to package " + pkgName +
		 ", but it was found in package " + name);
  }
  
  private void readAlternatives()
  {
    for(Method method = source.getBytecode().getMethods();
	method != null;
	method = method.getNext())
      bossa.link.Alternative.read(source.getBytecode(), method);
  }
  
  private boolean alternativesHaveMain()
  {
    for(Method method = source.getBytecode().getMethods();
	method != null;
	method = method.getNext())
      if (method.getName().equals("main"))
	return true;
    
    return false;
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
  
  /** List of the packages implicitely opened, a la 'import pkg.*' 
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
   * Passes
   ****************************************************************/

  public void scope()
  {
    ast.buildScope();
  }
  
  public void load()
  {
    try{
      ast.resolveScoping();
      ast.createContext();

      // this must be done before freezing
      if (!compiling())
	readAlternatives();
    }
    catch(Throwable e){
      Internal.error(e);
    }
  }

  public void compile()
  {    
    typecheck();
    nice.tools.compiler.OutputMessages.exitIfErrors();
    generateCode();
    saveInterface();
  }
  
  public void freezeGlobalContext()
  {
    contextFrozen = true;

    bossa.syntax.JavaClasses.createContext();
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
  
  private void typecheck()
  {
    // An interface file does not have to be typecheked.
    // It is known to be type correct from previous compilation!
    if (!compiling())
      return;

    if (Debug.passes)
      Debug.println(this + ": typechecking");
    
    ast.typechecking();
  }

  public void link()
  {
    if (!isRoot)
      return;
    
    // If at least one package is recompiled, the root will also be recompiled
    if (compiling())
      {
	if (Debug.passes)
	  Debug.println(this + ": linking");
	bossa.link.Dispatch.test(this);
    
	finishCompilation();
    
	nice.tools.compiler.OutputMessages.exitIfErrors();
      }

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

    // The implementation class is null if this package was up-to-date.
    if (implementationClass != null)
      thisPkg.addClass(implementationClass);

    thisPkg.addClass(dispatchClass);
    thisPkg.directory = source.getOutputDirectory();

    this.implementationClass = null;
    this.dispatchClass = null;

    thisPkg.next = res;
    res = thisPkg;

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
    
      for(int i = 0; i < opens.length; i++)
	{
	  f.print("import " + opens[i] + ".*;\n");
	}
    
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

  private static JarOutputStream jar;
  private static File jarDestFile;
  
  void writeArchive()
  {
    if (compilation.output == null)
      return;

    try{
      createJar();
      addToArchive();
      closeJar();
    }
    finally{
      if (jarDestFile != null)
	// The jar file was not completed
	// it must be corrupt, so it's cleaner to delete it
	{
	  jarDestFile.delete();
	  jarDestFile = null;
	}
    }
  }
  
  void createJar()
  {
    if (jar != null)
      Internal.error(this + " can't create a jar file again");
    
    if (!compilation.output.endsWith(".jar"))
      compilation.output = compilation.output + ".jar";
    
    try{
      jarDestFile = new File(compilation.output);
      // Create the directory if necessary
      jarDestFile.getParentFile().mkdirs();

      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");
      manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, 
                                       this.name + ".package");
     
      jar = new JarOutputStream(new FileOutputStream(jarDestFile), manifest);
    }
    catch(IOException e){
      User.error(this.name, "Error during creation of executable file: " + e);
    }
  }
  
  private void closeJar()
  {
    try{
      jar.close();
      jar = null;
      jarDestFile = null;
    }
    catch(IOException e){
      User.error(this.name, "Error during creation of executable file: "+e);
    }
  }

  private boolean addedToArchive;

  private void addToArchive()
  {
    if (addedToArchive)
      return;
    addedToArchive = true;

    if (Debug.passes)
      Debug.println(this + ": writing to archive");

    try{
      String packagePrefix = getName().replace('.', '/') + "/";
      Content.Stream[] classes = source.getClasses();
      
      for (int i = 0; i < classes.length; i++)
	{
	  Content.Stream s = classes[i];
	  jar.putNextEntry(new JarEntry(packagePrefix + s.name));
	  copyStreams(s.stream, jar);
	}
    }
    catch(IOException e){
      User.error(this, "Error writing bytecode in archive", e);
    }

    for (Iterator i = getImports().iterator(); i.hasNext();)
      ((Package) i.next()).addToArchive();
  }

  private static void copyStreams(InputStream in, OutputStream out)
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

  public void addImplementationClass(gnu.bytecode.ClassType classe)
  {
    thisPkg.addClass(classe);
  }

  public gnu.expr.Declaration addGlobalVar(String name, Type type)
  {
    gnu.expr.Declaration declaration = new gnu.expr.Declaration(name, type);

    if (!compiling())
      // The code is already there
      {
	declaration.field = source.getBytecode().getField(name);
	
	if (declaration.field == null)
	  Internal.error(this,
			 "The compiled file is not consistant with the interface file for global variable " + name);
      }
    else
      {
	implementationClass.addDeclaration(declaration);
	declaration.setFlag(Declaration.STATIC_SPECIFIED|Declaration.TYPE_SPECIFIED);
      }
    
    return declaration;
  }

  private gnu.expr.Package thisPkg;
  private ClassExp implementationClass, dispatchClass;

  private static ModuleExp createModule(String name)
  {
    ModuleExp res = new ModuleExp();
    res.setName(name);
    res.body = QuoteExp.voidExp;
    res.setFlag(ModuleExp.STATIC_SPECIFIED);
    res.setSuperType(gnu.bytecode.Type.pointer_type);
    return res;
  }

  private ClassExp createClassExp(String name)
  {
    ClassExp res = new ClassExp();
    res.setName(name);
    res.setSimple(true);
    res.body = QuoteExp.voidExp;
    
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
    try{
      nice.tools.code.SpecialTypes.init();
    }
    catch(ExceptionInInitializerError e){
      e.getException().printStackTrace();
      Internal.error("Exception in initializer: "+e.getException());
    }
  }

  static
  {
    nice.tools.code.NiceInterpreter.init();
  }

  /**
   * Creates bytecode for the alternatives defined in the module.
   */
  private void generateCode()
  {
    dispatchClass = createClassExp(name + ".dispatch");

    if (!compiling())
      return;

    if (Debug.passes)
      Debug.println(this + ": generating code");    
    
    implementationClass = createClassExp(name + ".package");

    ast.compile();
    
    if (isRunnable()) 
      MethodBodyDefinition.compileMain(this, mainAlternative);
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
     @return an expression to call this method 
     if the package has not been recompiled.
  */
  public gnu.expr.Expression lookupPackageMethod(String methodName)
  {
    if (source.getBytecode() == null) 
      return null;

    methodName = nice.tools.code.Strings.escape(methodName);
    
    for (Method m = source.getBytecode().getDeclaredMethods();
	 m != null; m = m.getNext())
      if (m.getName().equals(methodName))
	return new gnu.expr.QuoteExp(new gnu.expr.PrimProcedure(m));

    return null;
  }
  
  /**
     @return the bytecode method with this (unique) name
     if the package has not been recompiled.
  */
  private Method lookupDispatchMethod(String methodName)
  {
    if (source.getDispatch() == null) 
      return null;

    methodName = nice.tools.code.Strings.escape(methodName);
    
    for (Method m = source.getDispatch().getDeclaredMethods();
	 m != null; m = m.getNext())
      if (m.getName().equals(methodName))
	{
	  // The dispatch code will have to be regenerated anyway
	  m.eraseCode();
	  return m;
	}

    return null;
  }
  
  public gnu.expr.Expression getDispatchMethod(NiceMethod def)
  {
    String bytecodeName = def.getBytecodeName();
    LambdaExp res;

    /*
      If this package is not recompiled,
      we fetch the bytecode type information
      from the previous dispatch class.
      Benefits: we get the most precise bytecode type for methods,
        as computed during the initial compilation.
        This would not be the case if we recomputed it,
	as the precise types are found during typechecking.
    */
    Method meth = lookupDispatchMethod(bytecodeName);
    if (meth != null)
      res = nice.tools.code.Gen.createMethod
	(bytecodeName,
	 meth.arg_types,
	 meth.return_type,
	 def.formalParameters().getMonoSymbols());
    else
      res = nice.tools.code.Gen.createMethod
	(bytecodeName, 
	 def.javaArgTypes(),
	 def.javaReturnType(),
	 def.formalParameters().getMonoSymbols());

    ReferenceExp ref = nice.tools.code.Gen.referenceTo(res);
    addMethod(res, false);
    return ref;
  }

  public void addMethod(LambdaExp method, boolean packageMethod)
  {
    ClassExp classe = packageMethod ? implementationClass : dispatchClass;
    method.nextSibling = classe.firstChild;
    classe.firstChild = method;
    method.outer = classe;

    if (method.nameDecl != null && method.nameDecl.context == null)
      method.nameDecl.context = classe;
  }

  public String bytecodeName()
  {
    return name.toString();
  }
  
  /****************************************************************
   * Mangling
   ****************************************************************/

  private HashMap takenNames = new HashMap();
  
  public String mangleName(String str)
  {
    int i = 0;
    String res;
    do 
      {
	res = str + "$" + (i++);
      }
    while(takenNames.containsKey(res));

    takenNames.put(res,null);
    return res;
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
  
  public LocatedString name;
  
  /** True if this package was specified on the command line. */
  private boolean isRoot;
  
  private AST ast;

  /** The "source" where this package resides. */
  private Content source;
  
  /** The compilation that is in process. */
  Compilation compilation;
  
  /** Whether this package has a "main" method */
  private boolean isRunnable;
  public boolean isRunnable()
  {
    return isRunnable;
  }
  public void isRunnable(boolean isRunnable)
  {
    this.isRunnable = isRunnable;
  }

  private ReferenceExp mainAlternative = null;
  public void setMainAlternative(ReferenceExp main)
  {
    mainAlternative = main;
  }
  public ReferenceExp getMainAlternative()
  {
    return mainAlternative;
  }
  
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
}
