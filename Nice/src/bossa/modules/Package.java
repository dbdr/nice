/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : Package.java
// Created : Wed Oct 13 16:09:47 1999 by bonniot
//$Modified: Mon May 15 18:08:56 2000 by Daniel Bonniot $

package bossa.modules;

import mlsub.compilation.*;

import bossa.util.*;
import bossa.syntax.*;
import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;
import java.util.jar.*;
import java.io.*;

import bossa.modules.Compilation;

/**
 * A Bossa Package.
 * 
 * @author bonniot
 */

public class Package implements mlsub.compilation.Module
{
  public long lastModification()
  {
    return lastModified;
  }
  
  public void lastModifiedRequirement(long date)
  {
    // If we've already loaded the source
    // or we knew the state of all our imports last time we compiled,
    // there is nothing to do
    if(sourcesRead || date<=lastModified)
      return;
    
    if(itfFromJar!=null)
      User.error(name+" should be recompiled, but it was loaded from an archive file");
    
    if(Debug.modules)
      Debug.println("Recompiling "+this+
		    " because a required package changed "+
		    new java.util.Date(date));
    
    List definitions = new LinkedList();
    read(getSources(), definitions);
    sourcesRead();
    ast = new AST(this, definitions);
  }
  
  public void compile()
  {    
    typecheck();
    generateCode();
    saveInterface();
  }
  
  /****************************************************************
   * Loading
   ****************************************************************/
  
  public static Package make(String name, Compilation compilation,
			     boolean forceReload)
  {
    return make(new LocatedString(name, bossa.util.Location.nowhere()), 
		compilation,forceReload);
  }
  
  private static final Map map = new HashMap();
  
  public static Package make(LocatedString lname, Compilation compilation,
			     boolean forceReload)
  {
    String name = lname.toString();

    Package res = (Package) map.get(name);
    if(res!=null)
      return res;
    
    return new Package(lname, compilation, forceReload);
  }

  /****************************************************************
   * Single Constructor
   ****************************************************************/
  
  private Package(LocatedString name, Compilation compilation,
		  boolean forceReload)
  {
    this.name = name;
    this.compilation = compilation;
    
    map.put(name.toString(),this);

    imports = new LinkedList();
    
    if(!name.toString().equals("nice.lang") && !Debug.ignorePrelude)
      imports.add(new LocatedString("nice.lang", 
				    bossa.util.Location.nowhere()));
    
    opens  = new LinkedList();
    opens.add(this.name);
    opens.add(new LocatedString("java.lang", bossa.util.Location.nowhere()));

    findPackageDirectory();
    if(directory==null && itfFromJar==null)
      User.error(name,"Could not find package "+name);
    //Debug.println("Dir of "+this+" is "+directory.getPath());
    
    read(forceReload || compilation.recompileAll);
    
    // when we import a bossa package, we also open it.
    opens.addAll(imports);
  }

  private void read(boolean forceReload)
  {
    Package oldModule = Definition.currentModule;
    Definition.currentModule = this;
    
    List definitions = new LinkedList();
    if(itfFromJar!=null)
      bossa.parser.Loader.open(itfFromJar, name.toString(), 
			       definitions, imports, opens);
    else
      {
	File[] sources = getSources();
	File itf = getInterface();

	lastModified = maxLastModified(sources);
    
	if(!forceReload &&
	   itf!=null && lastModified <= itf.lastModified())
	  read(itf, definitions);
	else
	  {
	    if(sources.length==0)
	      User.error(name, "Package "+name+" has no source file in "+directory);
	
	    read(sources, definitions);
	    sourcesRead();
	  }
      }
    
    this.ast = new AST(this, expand(definitions));

    Definition.currentModule = oldModule;
  }
  
  private void sourcesRead()
  {
    sourcesRead=true;
    
    bytecode = ClassType.make(name+".package");
    bytecode.setSuper("java.lang.Object");
    bytecode.setModifiers(Access.FINAL|Access.PUBLIC);
    bytecode.requireExistingClass(false);
  }
  
  private static List expand(List definitions)
  {
    Collection ads = new LinkedList();
    for(Iterator i = definitions.iterator(); i.hasNext();)
      {
	Definition d = (Definition) i.next();
	Collection c = d.associatedDefinitions();
	if(c!=null)
	  ads.addAll(c);
      }
    definitions.addAll(ads);
    return definitions;
  }
  
  private long maxLastModified(File[] files)
  {
    long res = 0;
    for(int i=0; i<files.length; i++)
      {
	long time = files[i].lastModified();
	if(time>res) res = time;
      }
    return res;
  }
  
  private void read(File sourceFile, List definitions)
  {
    if(Debug.passes)
      Debug.println("Parsing "+sourceFile);
    
    try{
      Reader reader = new BufferedReader(new FileReader(sourceFile));
      LocatedString pkgName = 
	bossa.parser.Loader.open(reader, sourceFile.getName(),
				 definitions, imports, opens);
      if(pkgName!=null && !pkgName.equals(this.name))
	User.error(pkgName,
		   sourceFile+" declares belonging to package "+pkgName+
		   ", but it was found in package "+name);
    }
    catch(FileNotFoundException e){
      User.error(sourceFile.getName()+" of package "+name+" could not be found");
    }
  }
  
  private void read(File[] sources, List definitions)
  {
    for(int i=0; i<sources.length; i++)
      read(sources[i], definitions);
  }
  
  private void readAlternatives()
  {
    for(Method method=bytecode.getMethods();
	method!=null;
	method=method.getNext())
      {
	String methodName = bossa.Bytecode.unescapeString(method.getName());

	if(
	   // "main" is not a real alternative
	   methodName.equals("main")
	   || methodName.startsWith("main$")

	   // $get and $set methods
	   || methodName.startsWith("$")

	   // Class initialization
	   || methodName.equals("<clinit>")

	   // Instance initialization
	   || methodName.equals("<init>")
	   )
	  continue;

	new bossa.link.Alternative(methodName,bytecode,method);
      }
  }
  

  public List /* of Package */ getRequirements()
  {
    return getImports();
  }
  
  public ListIterator /* of String */ listImplicitPackages()
  {
    return opens.listIterator();
  }
  
  public void scope()
  {
    ast.buildScope();
  }
  
  public void load()
  {
    ast.resolveScoping();
    ast.createContext();
  }

  public void freezeGlobalContext()
  {
    contextFrozen=true;
    try{
      bossa.syntax.JavaTypeConstructor.createContext();
      bossa.engine.Engine.createInitialContext();
    }
    catch(bossa.engine.Unsatisfiable e){
      bossa.util.User.error("Unsatisfiable initial context: "+e.getMessage());
    }
  }

  public void unfreezeGlobalContext()
  {
    contextFrozen=false;
    bossa.engine.Engine.releaseInitialContext();
  }

  private static boolean contextFrozen;
  public static boolean contextFrozen()
  {
    return contextFrozen;
  }
  
  private void typecheck()
  {
    //if(!sourcesRead)
    //return;
    
    if(Debug.passes)
      Debug.println("Typechecking "+this);
    
    ast.typechecking();
  }

  public void link()
  {
    if(!sourcesRead)
      readAlternatives();
    
    if(Debug.passes)
      Debug.println("Linking "+this);
    if(isRunnable())
      bossa.link.Dispatch.test(this);
  }
  
  public void endOfLink()
  {
    if(jar!=null)
      closeJar();
  }
  
  private static JarOutputStream jar;
  
  private void createJar()
  {
    String name = this.name.toString();
    int lastDot = name.lastIndexOf('.');
    if(lastDot!=-1)
      name = name.substring(lastDot+1, name.length());
    
    try{
      File dest = new File(directory.getParent(), name+".jar");
      
      OutputStream out = new FileOutputStream(dest);
      Manifest manifest = new Manifest();

      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");
      manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, name+".package");
     
      jar = new JarOutputStream(out, manifest);
    }
    catch(IOException e){
      User.error(this.name, "Error during creation of executable file: "+e);
    }
  }
  
  private void closeJar()
  {
    try{
      JarEntry dispatchEntry = new JarEntry("dispatchClass.class");
      jar.putNextEntry(dispatchEntry);
      dispatchClass.writeToStream(jar);

      jar.close();
    }
    catch(IOException e){
      User.error(this.name, "Error during creation of executable file: "+e);
    }
  }
  
  private void saveInterface()
  {
    // do not save the interface if a Jar is produced for this package
    // because it's not supposed to be a library,
    // and it produces a warning! (interface, but no bytecode)
    if(!sourcesRead || jar!=null)
      return;

    try{
      PrintWriter f=new PrintWriter
	(new BufferedWriter(new FileWriter(new File(directory,"package.nicei"))));
      f.print("package "+name+";\n\n");
      
      for(Iterator i = getImports().iterator(); i.hasNext();)
	{
	  Package m = (Package) i.next();
	  f.print("import "+m.getName()+";\n");
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
   * Code generation
   ****************************************************************/

  /**
   * Transform the name of a class to its
   * fully qualified name.
   */
  public static String className(String name)
  {
    return name;
  }
  
  private ClassType bytecode;
  public  ClassType getBytecode() { return bytecode; }

  private static ModuleExp pkg;
  public ScopeExp getPackageScopeExp()
  {
    return pkg;
  }
  static
  {
    // pkg is shared ny all packages for now
    // if we change this, there should be a "super-ModuleExp"
    // (or change FindCapturedVars, and maybe others...)
    pkg = new ModuleExp();
    pkg.setName("packageExp");
    pkg.body = QuoteExp.voidExp;
  }

  private gnu.expr.Compilation comp;
  public  gnu.expr.Compilation getCompilation() { return comp; }
  
  public static final ClassType dispatchClass;
  public static final gnu.expr.Compilation dispatchComp;
  static
  {
    ClassType ct=null;
    gnu.expr.Compilation comp=null;
    try{
      kawa.standard.Scheme.registerEnvironment();
      
      ct = new ClassType("dispatchClass");
      ct.setSuper(Type.pointer_type);
      ct.setModifiers(Access.PUBLIC|Access.FINAL);
      comp = new gnu.expr.Compilation(ct,"dispatchClass",
				      "dispatchClass","prefix",false);
    }
    catch(ExceptionInInitializerError e){
      Internal.error("Error initializing Package class:\n"+
		     e.getException());
    }
    dispatchClass=ct;
    dispatchComp=comp;
  }
  
  /**
   * Creates bytecode for the alternatives defined in the module.
   */
  private void generateCode()
  {
    if(!sourcesRead)
      {
	ast.compile();
	return;
      }

    if(Debug.passes)
      Debug.println("Generating code for "+this);    
    
    //comp = new gnu.expr.Compilation(pkg, name.toString()+"DUMMY",
    //name.toString()+".", false);

    comp = new gnu.expr.Compilation(bytecode,name.toString(),
				    name.toString(),"prefix",false);
    
    ast.compile();
    
    if(isRunnable()) 
      MethodBodyDefinition.compileMain(this, mainAlternative);
    
    comp.compileClassInit(initStatements);
    
    addClass(bytecode);
    for (int iClass = 0;  iClass < comp.numClasses;  iClass++)
      addClass(comp.classes[iClass]);
  }

  public ClassType createClass(String name)
  {
    ClassType res = ClassType.make(this.name+"."+name);
    res.requireExistingClass(false);

    return res;
  }
  
  public void addClass(ClassType c)
  {
    // if we did not have to recompile, no class has to be regenerated
    if(!sourcesRead)
      return;
    
    try{
      c.setSourceFile(name+sourceExt);
      String filename = c.getName().replace('.',File.separatorChar)+".class";
      if(jar==null)
	c.writeToFile(new File(rootDirectory, filename));
      else
	{
	  jar.putNextEntry(new JarEntry(filename));
	  c.writeToStream(jar);
	}
    }
    catch(IOException e){
      User.error(this.name,"Could not write code for "+this,": "+e);
    }    
  }

  public gnu.bytecode.Method addDispatchMethod(MethodDefinition def)
  {
    return dispatchClass.addMethod
      (bossa.Bytecode.escapeString(def.getFullName()),
       Access.PUBLIC|Access.STATIC|Access.FINAL,
       def.javaArgTypes(),def.javaReturnType());
  }

  public String bytecodeName()
  {
    return name.toString();
  }
  
  private List initStatements = new LinkedList();
  
  public void addClassInitStatement(gnu.expr.Expression exp)
  {
    initStatements.add(exp);
  }

  public void compileMethod(gnu.expr.LambdaExp meth)
  {
    pkg.addMethod(meth);
    
    //FIXME
    gnu.expr.ChainLambdas.chainLambdas(meth);
    gnu.expr.PushApply.pushApply(meth);
    gnu.expr.FindTailCalls.findTailCalls(meth);
    meth.setCanRead(true);
    gnu.expr.FindCapturedVars.findCapturedVars(meth);
    
    meth.compileAsMethod(comp);
  }
  
  public void compileDispatchMethod(gnu.expr.LambdaExp meth)
  {
    meth.compileAsMethod(dispatchComp);
  }
  
  /****************************************************************
   * Mangling
   ****************************************************************/

  private HashMap takenNames = new HashMap();
  
  public String mangleName(String str)
  {
    int i=0;
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
   * File searching
   ****************************************************************/

  private static final String sourceExt = ".nice";
  
  private static final Object[] packageRoots;
  static 
  {
    String path = Debug.getProperty("nice.package.path",".");
    LinkedList res = new LinkedList();
    
    int start=0;
    while(start<path.length())
      {
	int end=path.indexOf(File.pathSeparatorChar,start);
	if(end==-1)
	  end=path.length();
	    
	String pathComponent=path.substring(start,end);
	if(pathComponent.length()>0)
	  {
	    File f = new File(pathComponent);
	    if(f.exists())
	      {
		if(pathComponent.endsWith(".jar"))
		  try{
		    res.add(new JarFile(f));
		  }
		  catch(IOException e){}
		else
		  res.add(f);
	      }
	  }
	start=end+1;
      }

    packageRoots = res.toArray(new Object[res.size()]);
  }
  
  private InputStream openClass()
  {
    File f = new File(directory, "package.class");

    try{
      if(f!=null)
	return new FileInputStream(f);
    }
    catch(FileNotFoundException e){}

    return null;
  } 

  private File[] getSources()
  {
    File[] res=directory.listFiles
      (new FilenameFilter()
	{ 
	  public boolean accept(File dir, String name)
	  { return name.endsWith(sourceExt); }
	}
       );
    if(res==null)
      User.error(name,"Could not list source files in "+name);
    
    // put nice.lang.prelude first if it exists
    if(name.toString().equals("nice.lang"))
      for(int i=0; i<res.length; i++)
	if(res[i].getName().equals("prelude"+sourceExt))
	  {
	    File tmp = res[i];
	    res[i] = res[0];
	    res[0] = tmp;
	    break;
	  }
    
    return res;
  }

  private File getInterface()
  {
    File itf = new File(directory,"package.nicei");

    if(!itf.exists())
      return null;
    
    InputStream s = openClass();
    if(s!=null)
      try{ bytecode = ClassFileInput.readClassType(s); }
      catch(IOException e){ s=null; }

    if(s!=null)
      return itf;
    
    User.warning("Bytecode for "+this+
		 " was not found, altough its interface exists.\n"+
		 "Ignoring and recompiling");
    return null;
  }
  
  private void findPackageDirectory()
  {
    String rname = name.toString().replace('.',File.separatorChar);
    
    for(int i=0; i<packageRoots.length; i++)
      if(packageRoots[i] instanceof File)
	{
	  directory = new File((File) packageRoots[i], rname);
	  if(directory.exists())
	    {
	      rootDirectory = (File) packageRoots[i];
	      return;
	    }
	  else
	    directory = null;
	}
      else
	{
	  JarFile jar = (JarFile) packageRoots[i];
	  JarEntry ent = jar.getJarEntry(rname+"/package.nicei");
	  if(ent!=null)
	    try{
	      itfFromJar = new BufferedReader
		(new InputStreamReader(jar.getInputStream(ent)));
	      ent = jar.getJarEntry(rname+"/package.class");
	      bytecode = ClassFileInput.readClassType(jar.getInputStream(ent));
	    }
	    catch(IOException e){
	      User.error(this.name, "Error reading archive "+jar);
	    }
	}
  }
  
  /****************************************************************
   * Misc
   ****************************************************************/

  public String getName()
  {
    return name.toString();
  }
  
  public String toString()
  {
    return "package "+name;
  }
  
  public LocatedString name;
  
  private List /* of LocatedString */ imports;
  private List /* of Package */ importedPackages;

  private List getImports()
  {
    if(importedPackages==null)
      computeImportedPackages();
    
    return importedPackages; 
  }
  
  private void computeImportedPackages()
  {
    importedPackages=new ArrayList(imports.size());

    for(Iterator i = imports.iterator(); i.hasNext();)
      {
	LocatedString s = (LocatedString) i.next();
	importedPackages.add(make(s, compilation, false));
      }
  }

  /** List of the LocatedStrings of packages implicitely opened. */
  List opens;

  private AST ast;

  /** The directory where this package resides. */
  private File directory;

  /** The component of the package path this package was found in. */
  private File rootDirectory;

  /** The interface file of the package if it was found in a jar file */
  private Reader itfFromJar;
  
  /** The compilation that is in process. */
  private Compilation compilation;
  
  /** Whether this package has a "main" method */
  private boolean isRunnable;
  public boolean isRunnable()
  {
    return isRunnable;
  }
  public void isRunnable(boolean isRunnable)
  {
    this.isRunnable = isRunnable;
    if(isRunnable && !compilation.skipLink)
      createJar();
  }

  private gnu.bytecode.Method mainAlternative=null;
  public void setMainAlternative(gnu.bytecode.Method main)
  {
    mainAlternative=main;
  }
  public gnu.bytecode.Method getMainAlternative()
  {
    return mainAlternative;
  }
  
  public boolean generatingBytecode()
  {
    return sourcesRead;
  }
  
  /** Whether we read this package from its interface
      (it was already compiled and up to date)
      or from source files.
  */
  private boolean sourcesRead;

  private long lastModified;
  
  private boolean dbg = Debug.modules;
}
