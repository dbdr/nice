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
//$Modified: Tue Aug 01 18:55:46 2000 by Daniel Bonniot $

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
    String pkgName = pkg.name.toString().replace('.', '/');

    JarEntry itfEntry = jar.getJarEntry(pkgName + "/package.nicei");

    if (itfEntry != null)
      {
	JarEntry bytecodeEntry = jar.getJarEntry(pkgName + "/package.class");
	if (bytecodeEntry != null)
	  return new JarSource(pkg, jar, itfEntry, bytecodeEntry);	
      }

    return null;
  }
  
  JarSource(Package pkg, JarFile jar, 
	    JarEntry itfEntry, JarEntry bytecodeEntry)
  {
    this.pkg = pkg;
    this.jar = jar;

    this.itfEntry = itfEntry;
    this.bytecodeEntry = bytecodeEntry;
  }

  BufferedReader[] getDefinitions(boolean forceReload)
  {
    BufferedReader res = null;

    try{
      res = new BufferedReader
	(new InputStreamReader(jar.getInputStream(itfEntry)));

      bytecode = gnu.bytecode.ClassFileInput.readClassType
	(jar.getInputStream(bytecodeEntry));
    }
    catch(IOException e){
      User.error(pkg.name,
		 "Error reading archive " + getName());
    }
    
    return new BufferedReader[]{ res };
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

  private JarEntry itfEntry, bytecodeEntry;
}
