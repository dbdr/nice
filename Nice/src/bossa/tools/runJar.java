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

// File    : jarMainClass.java
// Created : Thu Apr 20 14:48:25 2000 by Daniel Bonniot
//$Modified: Tue Jun 20 15:54:32 2000 by Daniel Bonniot $

package bossa.tools;

import java.util.jar.*;
import java.lang.reflect.*;

/**
 * Runs a jar file.
 *
 * Does the same as java -jar, except the user classpath is used.
 * 
 * @author Daniel Bonniot
 */

public class runJar
{
  public static void main(String[] args)
  {
    if(args.length==0)
      {
	System.err.println("usage: java bossa.tools.runJar file.jar");
	System.exit(1);
      }
    
    String jar = args[0];
    String mainClass = null;
    try{
      mainClass = new JarFile(new java.io.File(jar)).getManifest().
	getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
    }
    catch(java.util.zip.ZipException e){
      System.err.println(jar+" is not a valid Jar file");
      System.exit(1);
    }
    catch(java.io.IOException e){
      System.err.println(jar+" does not exist");
      System.exit(1);
    }
    if(mainClass==null)
      {
	System.err.println(jar+" has no main class");
	System.exit(2);
      }
    
    try{
      Class main = Class.forName(mainClass);
      try{
	Class[] sv = new Class[1];
	sv[0] = args.getClass(); // ie String[]
	Method mainMethod = main.getMethod("main", sv);
	String[] newArgs = new String[args.length-1];
	System.arraycopy(args, 1, newArgs, 0, newArgs.length);
	try{
	  mainMethod.invoke(null, new Object[]{newArgs});
	}
	catch(IllegalAccessException e){
	  System.err.println(e.toString());
	}
	catch(java.lang.reflect.InvocationTargetException e){
	  try{
	    throw e.getTargetException();
	  }
	  catch(NoClassDefFoundError ex){
	    System.err.println("nicer: Class "+
			       ex.getMessage().replace('/','.')+
			       " not found"+
			       "\nInstall the adequate package in your CLASSPATH");
	  }
	  catch(Throwable ex){
	    System.err.println("Uncaught exception: ");
	    ex.printStackTrace();
	  }
	}
      }
      catch(NoSuchMethodException e){
	System.err.println(mainClass+" has no main method");
      }
      
    }
    catch(ClassNotFoundException e){
      System.err.println(jar+" indicates "+mainClass+
			 " as its main class, but no such class was found");
    }
  }
}
