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

// File    : Front.java
// Created : Thu Jul 01 15:11:18 1999 by bonniot
//$Modified: Mon Mar 13 17:42:49 2000 by Daniel Bonniot $

package bossa.test;

import java.util.*;

import bossa.parser.Loader;
import bossa.syntax.AST;
import bossa.modules.Package;
import bossa.util.Debug;

/** 
 * Test of the frontend.
 */
public class Front
{
  /** The main function
   * 
   * @exception Exception 
   * @param args Command line arguments
   */
  public static void main(String[] args) throws Exception
  {
    String file;
    if(args.length==0)
      {
	System.out.println("Usage: bossa file.bossa");
	if((file=bossa.util.Debug.defaultFile)==null)
	  System.exit(-1);
      }
    else file=args[0];
    
    try{
      compile(file);
    }
    catch(Exception e){
      System.out.println("Uncaught exception :");
      e.printStackTrace();
    }
    catch(Error e){
      System.out.println("Uncaught error :");
      e.printStackTrace();
    }
  }

  private static void compile(String file)
  {
    bossa.modules.Compilation compilation = new bossa.modules.Compilation();
    
    bossa.modules.Package p;
    if(!file.equals("bossa.lang"))
      Package.make("bossa.lang",compilation,false);
    p = Package.make(file,compilation,true);
    
    List req = p.getRequirements();
    for(Iterator i = req.iterator(); i.hasNext();)
      {
	Package r = (Package) i.next();
	r.scope();
	r.load();
      }
    
    p.scope();
    p.load();

    try{
      bossa.syntax.JavaTypeConstructor.createContext();
      bossa.engine.Engine.createInitialContext();
    }
    catch(bossa.engine.Unsatisfiable e){
      bossa.util.User.error("Unsatisfiable initial context: "+e.getMessage());
    }
    
    for(Iterator i = req.iterator(); i.hasNext();)
      {
	Package r = (Package) i.next();
	r.compile();
      }
    
    p.compile();

    if(p.isRunnable())
      {
	for(Iterator i = req.iterator(); i.hasNext();)
	  {
	    Package r = (Package) i.next();
	    r.link();
	  }    
	p.link();
	p.endOfLink();
      }    
  }
  
}

