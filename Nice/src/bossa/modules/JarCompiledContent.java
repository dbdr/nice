/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2001                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.modules;

import bossa.util.*;
import java.io.*;
import java.util.jar.*;
import gnu.bytecode.ClassFileInput;

/**
   A compiled package located in a jar file.
    
   @author Daniel Bonniot (Daniel.Bonniot@inria.fr)
 */

class JarCompiledContent extends CompiledContent
{
  static JarCompiledContent create(Package pkg, JarFile jar)
  {
    // Jar and Zip files use forward slashes
    String pkgName = pkg.getName().replace('.', '/');

    JarEntry itfEntry = jar.getJarEntry(pkgName + "/package.nicei");
    if (itfEntry == null)
      return null;
    
    JarEntry bytecodeEntry = jar.getJarEntry
      (pkgName + "/" + Package.packageClassName + ".class");
    if (bytecodeEntry == null)
      return null;
    
    JarEntry dispatchEntry = jar.getJarEntry(pkgName + "/dispatch.class");
    if (dispatchEntry == null)
      return null;

    return new JarCompiledContent
      (pkg, jar, itfEntry, bytecodeEntry, dispatchEntry);
  }

  JarCompiledContent(Package pkg, JarFile jar, JarEntry itfEntry,
		     JarEntry bytecodeEntry, JarEntry dispatchEntry)
  {
    this.pkg = pkg;
    this.jar = jar;

    this.itfEntry = itfEntry;
    this.bytecodeEntry = bytecodeEntry;
    this.dispatchEntry = dispatchEntry;

    this.lastCompilation =
      Math.min(itfEntry.getTime(),
      Math.min(bytecodeEntry.getTime(),
               dispatchEntry.getTime()));

    /* Use the date of creation of the jar file if it is later.
       The package might have been compiled earlier, but we probably did
       not get to see it before it was put into this jar.
       This is in particular the case if the jar is an upgraded library.
    */
    this.lastCompilation =
      Math.max(new File(jar.getName()).lastModified(), this.lastCompilation);
  }

  Content.Unit[] getDefinitions()
  {
    BufferedReader res = null;

    try{
      res = new BufferedReader
	(new InputStreamReader(jar.getInputStream(itfEntry)));

      bytecode = ClassFileInput.readClassType
	(jar.getInputStream(bytecodeEntry));
      dispatch = ClassFileInput.readClassType
	(jar.getInputStream(dispatchEntry));
    }
    catch(IOException e){
      User.error(pkg.name,
		 "Error reading archive " + getName());
    }
    
    return new Content.Unit[]{ new Content.Unit(res, pkg.name.toString()) };
  }

  gnu.bytecode.ClassType readClass(String name)
  {
    JarEntry entry = jar.getJarEntry(name.replace('.', '/') + ".class");
    if (entry == null)
      return null;

    try {
      return ClassFileInput.readClassType(name, jar.getInputStream(entry));
    }
    catch (IOException e) {
      return null;
    }
  }

  Content.Stream[] getClasses(boolean wantDispatch)
  {
    java.util.List res = new java.util.LinkedList();
    String pkgPrefix = pkg.getName().replace('.', '/') + "/";

    java.util.Enumeration enum = jar.entries();
    while(enum.hasMoreElements())
      {
	JarEntry e = (JarEntry) enum.nextElement();
	String fullname = e.getName();
	if (fullname.startsWith(pkgPrefix)
	    && fullname.indexOf('/', pkgPrefix.length()) == -1
	    && fullname.endsWith(".class"))
	  {
	    String name = fullname.substring(pkgPrefix.length());
	    if (wantDispatch || !name.equals("dispatch.class"))
	      try{
		res.add(new Content.Stream(jar.getInputStream(e), name));
	      }
	      catch(IOException ex){
		User.error(pkg.name,
			   "Error reading archive " + getName());
	      }
	  }
      }

    int len = res.size();
    if (! wantDispatch)
      len++;
    return (Content.Stream[]) res.toArray(new Content.Stream[len]);
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
  
  public String getName()
  {
    return nice.tools.util.System.prettyPrint(jar);
  }
  
  public String toString()
  {
    return "Compiled package: " + getName();
  }

  private Package pkg;
  private JarFile jar;

  private JarEntry itfEntry, bytecodeEntry, dispatchEntry;
}
