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
//$Modified: Wed Mar 01 21:53:28 2000 by Daniel Bonniot $

package bossa.modules;

import mlsub.compilation.*;

import bossa.util.*;
import bossa.syntax.*;
import gnu.bytecode.*;

import java.util.*;
import java.io.*;

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

  public static Package make(String name, boolean forceReload)
  {
    return make(new LocatedString(name, bossa.util.Location.nowhere()), 
		forceReload);
  }
  
  public static Package make(String name)
  {
    return make(name, false);
  }
  
  private static final Map map = new HashMap();
  
  public static Package make(LocatedString lname, boolean forceReload)
  {
    String name = lname.toString();

    Package res = (Package) map.get(name);
    if(res!=null)
      return res;
    
    return new Package(lname, forceReload);
  }
  
  private Package(LocatedString name, boolean forceReload)
  {
    this.name=name;
    
    map.put(name.toString(),this);

    imports = new LinkedList();
    opens  = new LinkedList();
    opens.add(this.name);
    opens.add(new LocatedString("java.lang", bossa.util.Location.nowhere()));

    findPackageDirectory();
    if(directory==null)
      User.error(name,"Could not find package "+name);

    read(forceReload);
    
    // when we import a bossa package, we also open it.
    opens.addAll(imports);
  }

  private void read(boolean forceReload)
  {
    File[] sources = getSources();
    File itf = getInterface();

    lastModified = maxLastModified(sources);
    
    Definition.currentModule = this;
    
    List definitions = new LinkedList();

    if(!forceReload &&
       itf!=null && lastModified <= itf.lastModified())
      read(itf, definitions);
    else
      {
	if(sources.length==0)
	  User.error(name, "Package "+name+" has no source file");
	
	read(sources, definitions);
	sourcesRead();
      }
    
    this.ast = new AST(this, expand(definitions));
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
    
    bossa.parser.Loader.open(sourceFile, definitions, imports, opens);
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
    bossa.engine.Engine.releaseInitialContext();
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
    if(isRunnable())
      addClass(dispatchClass);
  }
  
  private void saveInterface()
  {
    if(!sourcesRead)
      return;

    try{
      PrintWriter f=new PrintWriter
	(new BufferedWriter(new FileWriter(new File(directory,"package.bossi"))));

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
  
  private gnu.bytecode.ClassType bytecode;
  public  gnu.bytecode.ClassType getBytecode() { return bytecode; }

  private gnu.expr.Compilation compilation;
  public  gnu.expr.Compilation getCompilation() { return compilation; }
  
  public static final gnu.bytecode.ClassType dispatchClass;
  public static final gnu.expr.Compilation dispatchComp;
  static
  {
    gnu.bytecode.ClassType ct=null;
    gnu.expr.Compilation comp=null;
    try{
      kawa.standard.Scheme.registerEnvironment();
      
      ct = new gnu.bytecode.ClassType("dispatchClass");
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
    //if(!sourcesRead)
    //return;

    if(Debug.passes)
      Debug.println("Generating code for "+this);    
    
    compilation = new gnu.expr.Compilation(bytecode,name.toString(),name.toString(),"prefix",false);
    
    ast.compile();
    if(isRunnable())     
      MethodBodyDefinition.compileMain(this, mainAlternative);

    compilation.compileClassInit(initStatements);
    
    addClass(bytecode);
    for (int iClass = 0;  iClass < compilation.numClasses;  iClass++)
      addClass(compilation.classes[iClass]);
  }

  public ClassType createClass(String name)
  {
    ClassType res = gnu.bytecode.ClassType.make(this.name+"."+name);
    res.requireExistingClass(false);

    return res;
  }
  
  public void addClass(ClassType c)
  {
    try{
      c.setSourceFile(name+sourceExt);
      c.writeToFile(new File(rootDirectory,c.getName().replace('.',File.separatorChar)+".class"));
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
    //FIXME
    gnu.expr.ChainLambdas.chainLambdas(meth);
    gnu.expr.PushApply.pushApply(meth);
    gnu.expr.FindTailCalls.findTailCalls(meth);
    meth.setCanRead(true);
    gnu.expr.FindCapturedVars.findCapturedVars(meth);
    
    meth.compileAsMethod(compilation);
  }
  
  public void compileDispatchMethod(gnu.expr.LambdaExp meth)
  {
    meth.compileAsMethod(dispatchComp);
  }
  
  /****************************************************************
   * Mangling
   ****************************************************************/

  private TreeSet takenNames = new TreeSet();
  
  public String mangleName(String str)
  {
    int i=0;
    String res = str+"$0";
    while(takenNames.contains(res))
      res = str + "$" + (++i);
  
    takenNames.add(res);
    return res;
  }
  
  /****************************************************************
   * File searching
   ****************************************************************/

  private static final String sourceExt = ".bossa";
  
  private static final String[] packageRoots;
  static 
  {
    String path = Debug.getProperty("bossa.package.path",".");
    LinkedList res = new LinkedList();
    
    int start=0;
    while(start<path.length())
      {
	int end=path.indexOf(File.pathSeparatorChar,start);
	if(end==-1)
	  end=path.length();
	    
	String pathComponent=path.substring(start,end);
	if(pathComponent.length()>0)
	  res.add(pathComponent);
	
	start=end+1;
      }

    packageRoots = (String[]) res.toArray(new String[res.size()]);
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
    
    // put bossa.lang.prelude first if it exists
    if(name.content.equals("bossa.lang"))
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
    File itf = new File(directory,"package.bossi");

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
      {
	directory = new File(packageRoots[i],rname);
	if(directory.exists())
	  {
	    rootDirectory=new File(packageRoots[i]);
	    return;
	  }
      }
    directory=null;
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
	importedPackages.add(make(s, false));
      }
  }
  
  List /* of LocatedString */ opens; /* package open */
  private AST ast;

  /** The directory where this package resides. */
  private File directory;

  /** The component of the package path this package was found in. */ 
  private File rootDirectory;
  
  public boolean isRunnable()
  {
    return mainAlternative!=null;
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
  
  private gnu.bytecode.Method mainAlternative=null;
  public void setMainAlternative(gnu.bytecode.Method main)
  {
    mainAlternative=main;
  }
  public gnu.bytecode.Method getMainAlternative()
  {
    return mainAlternative;
  }
  
  private boolean dbg = Debug.modules;
}
