/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : JarSource.java
// Created : Tue Aug 01 16:57:33 2000 by Daniel Bonniot
//$Modified: Tue Oct 03 10:54:34 2000 by Daniel Bonniot $

package bossa.modules;

import bossa.util.*;
import java.io.*;
import java.util.jar.*;

/**
 * A package located in a jar file.
 * 
 * @author Daniel Bonniot
 */

class JarSource extends PackageSource
{
  static JarSource create(Package pkg, JarFile jar)
  {
    // Jar and Zip files use forward slashes
    String pkgName = pkg.getName().replace('.', '/');

    JarEntry itfEntry = jar.getJarEntry(pkgName + "/package.nicei");
    if (itfEntry == null)
      return null;
    
    JarEntry bytecodeEntry = jar.getJarEntry(pkgName + "/package.class");
    if (bytecodeEntry == null)
      return null;
    
    JarEntry dispatchEntry = jar.getJarEntry(pkgName + "/dispatch.class");
    if (dispatchEntry == null)
      return null;

    return new JarSource(pkg, jar, itfEntry, bytecodeEntry, dispatchEntry);
  }
  
  JarSource(Package pkg, JarFile jar, 
	    JarEntry itfEntry, JarEntry bytecodeEntry, JarEntry dispatchEntry)
  {
    this.pkg = pkg;
    this.jar = jar;

    this.itfEntry = itfEntry;
    this.bytecodeEntry = bytecodeEntry;
    this.dispatchEntry = dispatchEntry;

    this.lastCompilation = this.lastModification =
      Math.min(itfEntry.getTime(),
	       Math.min(bytecodeEntry.getTime(), dispatchEntry.getTime()));
  }

  Unit[] getDefinitions(boolean forceReload)
  {
    BufferedReader res = null;

    try{
      res = new BufferedReader
	(new InputStreamReader(jar.getInputStream(itfEntry)));

      bytecode = gnu.bytecode.ClassFileInput.readClassType
	(jar.getInputStream(bytecodeEntry));
      dispatch = gnu.bytecode.ClassFileInput.readClassType
	(jar.getInputStream(dispatchEntry));
    }
    catch(IOException e){
      User.error(pkg.name,
		 "Error reading archive " + getName());
    }
    
    return new Unit[]{ new Unit(res, pkg.name.toString()) };
  }

  Stream[] getClasses()
  {
    java.util.List res = new java.util.LinkedList();
    String pkgPrefix = pkg.getName().replace('.', '/') + "/";

    java.util.Enumeration enum = jar.entries();
    while(enum.hasMoreElements())
      {
	JarEntry e = (JarEntry) enum.nextElement();
	String fullname = e.getName();
	if (fullname.startsWith(pkgPrefix)
	    && fullname.endsWith(".class"))
	  {
	    String name = fullname.substring(pkgPrefix.length());
	    if (!name.equals("dispatch.class"))
	      try{
		res.add(new Stream(jar.getInputStream(e), name));
	      }
	      catch(IOException ex){
		User.error(pkg.name,
			   "Error reading archive " + getName());
	      }
	  }
      }

    return (Stream[]) res.toArray(new Stream[res.size()]);
  }
  
  InputStream getBytecodeStream()
  {
    try{
      return jar.getInputStream(bytecodeEntry);
    }
    catch(IOException e){
      User.error(pkg, "Error reading archive " + getName());
      return null;
    }
  }
  
  File getOutputDirectory()
  {
    return new File(jar.getName()).getParentFile();
  }
  
  public String getName()
  {
    return nice.tools.util.System.prettyPrint(jar);
  }
  
  public String toString()
  {
    return "Jar package source: " + getName();
  }

  private Package pkg;
  private JarFile jar;

  private JarEntry itfEntry, bytecodeEntry, dispatchEntry;
}
