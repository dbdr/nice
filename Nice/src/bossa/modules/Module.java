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
//$Modified: Tue Nov 23 17:40:53 1999 by bonniot $

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
  public Module(String name, List imports, List definitions)
  {
    if(!name.endsWith(".bossa") 
       && !name.endsWith(".bossi"))
      User.error("Invalid file name: "+name);
    this.name=name.substring(0,name.lastIndexOf("."));
    this.imports=imports;
    this.definitions=new AST(definitions);
  }

  public static void compile(String file) throws IOException
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
      }
  }
  
  private void load()
  {
    if(dbg) Debug.println("Loading "+this);
    loadImports();
    definitions.load();
    definitions.createContext();
  }
  
  private void loadImports()
  {
    for(Iterator i=imports.iterator();i.hasNext();) 
      {
	String name = ((LocatedString)i.next()).toString();
	
	Module m=Loader.open(name+".bossi");
	m.load();
	
	// Get the alternatives from the bytecode file
	try{
	  InputStream file = new FileInputStream(name+".class");
	  gnu.bytecode.ClassType c = gnu.bytecode.ClassFileInput.readClassType(file);
	  
	  for(Method method=c.getMethods();method!=null;method=method.getNext())
	    new bossa.link.Alternative(method.getName());
	}
	catch(IOException e){
	  User.error("Module "+name+" is not compiled");
	}
      }
  }

  /**
   * Saves the interface exported by this module.
   */
  private void saveInterface() throws IOException
  {
    Writer f=new BufferedWriter(new FileWriter(name+".bossi"));
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

  public static final gnu.bytecode.ClassType dispatchClass
    = new gnu.bytecode.ClassType("dispatchClass");
  
  /**
   * Creates bytecode for the alternatives defined in the module.
   */
  private void generateCode()
  {
    bytecode=ClassType.make(name);
    bytecode.setSuper("java.lang.Object");
    bytecode.setModifiers(Access.FINAL);
    definitions.compile(this);
    addClass(bytecode);
  }

  /**
   * Creates bytecode for the alternatives defined in the module.
   * Not implemented yet.
   */
//    private void generateCode()
//    {
//      gnu.expr.ModuleExp moduleExp = new gnu.expr.ModuleExp();
//      moduleExp.setFile(name+".bossa");
//      moduleExp.mustCompile = true;
//      //definitions.compile(moduleExp);
//    }

  public void addClass(ClassType c)
  {
    try{
      c.setSourceFile(name+".bossa");
      c.writeToFile();
    }
    catch(IOException e){
      User.error("Could not write code for "+this);
    }    
  }
  
  /****************************************************************
   * Link
   ****************************************************************/

  private void link()
  {
    bossa.link.Dispatch.test();
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
