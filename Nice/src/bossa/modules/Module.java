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

// File    : Module.java
// Created : Wed Oct 13 16:09:47 1999 by bonniot
//$Modified: Fri Dec 03 19:04:22 1999 by bonniot $

package bossa.modules;

import bossa.util.*;
import bossa.parser.*;
import bossa.syntax.*;
import gnu.bytecode.*;

import java.util.*;
import java.io.*;

/**
 * A Bossa Module
 * 
 * @author bonniot
 */

public class Module
{
  final static String sourceExt = ".bossa";
  final static String interfaceExt = ".bossi";
  
  public Module(String name, List imports, List definitions)
  {
    if(!name.endsWith(interfaceExt) 
       && !name.endsWith(sourceExt))
      User.error("Invalid file name: "+name);
    this.name=name.substring(0,name.lastIndexOf("."));
    this.imports=imports;
    this.definitions=new AST(definitions);
  }

  public static void compile(LocatedString file) throws IOException
  {
    Module module=Loader.open(file);
    module.compile();
  }
  
  private void compile() throws IOException
  {
    if(dbg) Debug.println("Compiling "+this);
    load();

    try{
      bossa.engine.Engine.createInitialContext();
    }
    catch(bossa.engine.Unsatisfiable e){
      User.error("Unsatisfiable initial context: "+e.getMessage());
    }
    
    definitions.typechecking();
    
    if(Debug.passes) Debug.println("Generating code");
    generateCode();

    if(Debug.passes) Debug.println("Saving module interface");
    saveInterface();

    // We link only if this package pretends to be runnable
    if(bossa.syntax.MethodBodyDefinition.hasMain())
      {
	if(Debug.passes)
	  Debug.println("Linking");
	link();
	addClass(dispatchClass);
      }
  }
  
  private void load()
  {
    if(dbg) Debug.println("Loading "+this);
    loadImports();
    definitions.load();
    definitions.createContext(this);
  }
  
  private void loadImports()
  {
    for(Iterator i=imports.iterator();i.hasNext();) 
      {
	LocatedString name = (LocatedString)i.next();
	LocatedString itfName = name.cloneLS();
	itfName.append(interfaceExt);
	String bytecodeName = name.toString()+".class";	

	// Get the alternatives from the bytecode file
	try{
	  InputStream file = new FileInputStream(bytecodeName);

	  Module m=Loader.open(itfName);
	  m.bytecode = gnu.bytecode.ClassFileInput.readClassType(file);	  

	  for(Method method=m.bytecode.getMethods();
	      method!=null;
	      method=method.getNext())
	    {
	      String methodName = method.getName();

	      if(
		 // "main" is not a real alternative
		 methodName.equals("main")
		 || methodName.startsWith("main$")
		 // $get and $set methods
		 || methodName.startsWith("$")
		 // Class initialization
		 || methodName.equals("<clinit>")
		 )
		continue;

	      new bossa.link.Alternative(methodName,m.bytecode,method);
	    }

	  m.load();
	}
	catch(IOException e){
	  User.error("Compilation of module "+name+" failed");
	}
      }
  }

  /**
   * Saves the interface exported by this module.
   */
  private void saveInterface() throws IOException
  {
    Writer f=new BufferedWriter(new FileWriter(name+interfaceExt));
    definitions.printInterface(new PrintWriter(f));
    f.close();
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
  
  public gnu.bytecode.ClassType bytecode;
  public gnu.expr.Compilation compilation;

  public static final gnu.bytecode.ClassType dispatchClass;
  public static final gnu.expr.Compilation dispatchComp;
  static
  {
    dispatchClass = new gnu.bytecode.ClassType("dispatchClass");
    dispatchClass.setSuper(Type.pointer_type);
    dispatchClass.setModifiers(Access.PUBLIC|Access.FINAL);
    dispatchComp = new gnu.expr.Compilation(dispatchClass,"dispatchClass","dispatchClass","prefix",false);
  }
  
  /**
   * Creates bytecode for the alternatives defined in the module.
   */
  private void generateCode()
  {
    bytecode = ClassType.make(name);
    bytecode.setSuper("java.lang.Object");
    bytecode.setModifiers(Access.FINAL);

    compilation = new gnu.expr.Compilation(bytecode,name,name,"prefix",false);
    
    definitions.compile(this);
    compilation.compileClassInit(initStatements);
    
    addClass(bytecode);
  }

  //public gnu.expr.ModuleExp moduleExp;
  
  /**
   * Creates bytecode for the alternatives defined in the module.
   */
//    private void generateCode2()
//    {
//      moduleExp = new gnu.expr.ModuleExp();
//      moduleExp.setFile(name+sourceExt);
//      moduleExp.mustCompile = true;
//      definitions.compile(this);
//      try{
//        moduleExp.compileToArchive(name+".zip");
//      }
//      catch(IOException e){
//        User.error("Could not write code for "+this);
//      }    
//    }

  public void addClass(ClassType c)
  {
    try{
      c.setSourceFile(name+sourceExt);
      c.writeToFile();
    }
    catch(IOException e){
      User.error("Could not write code for "+this);
    }    
  }

  public gnu.bytecode.Method addDispatchMethod(MethodDefinition def)
  {
    return dispatchClass.addMethod
      (def.getFullBytecodeName(),
       Access.PUBLIC|Access.STATIC|Access.FINAL,
       def.javaArgTypes(),def.javaReturnType());
  }

  public String bytecodeName()
  {
    return name.toString();
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
  
  private List initStatements = new LinkedList();
  
  public void addClassInitStatement(gnu.expr.Expression exp)
  {
    initStatements.add(exp);
  }
  
  /****************************************************************
   * Link
   ****************************************************************/

  private void link()
  {
    bossa.link.Dispatch.test(this);
    
  }
  
  public String toString()
  {
    return "module "+name;
  }
  
  public String name;
  private List /* of LocatedString */ imports;
  private AST definitions;

  private boolean dbg = Debug.modules;
}
