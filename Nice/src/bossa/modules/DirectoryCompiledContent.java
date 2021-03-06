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

import gnu.bytecode.ClassType;

/**
   A package located in a directory.
 
   @version $Date$
   @author Daniel Bonniot
 */

class DirectoryCompiledContent extends CompiledContent
{
  static DirectoryCompiledContent create(Package pkg, File directory)
  {
    if (!directory.exists())
      return null;

    DirectoryCompiledContent res = new DirectoryCompiledContent(pkg, directory);
    if (res.isValid())
      return res;

    return null;
  }

  DirectoryCompiledContent(Package pkg, File directory)
  {
    this.pkg = pkg;
    this.directory = directory;

    this.itf = getInterface();
  }

  private File itf;

  /** @return true if this directory indeed hosts a Nice package. */
  private boolean isValid()
  {
    return itf != null;
  }

  Content.Unit[] getDefinitions()
  {
    return new Content.Unit[]{ new Content.Unit(read(itf), itf) };
  }

  ClassType readClass(String name)
  {
    InputStream s = getFileStream(bossa.util.Util.simpleName(name) + ".class");
    if (s == null)
      return null;
    
    ClassType res = null;
    s = new BufferedInputStream( s );
    try{ res = gnu.bytecode.ClassFileInput.readClassType(name, s); }
    catch(LinkageError e){}
    catch(IOException e){}
    
    return res;
  }
  
  private File getInterface()
  {
    File itf = new File(directory, "package.nicei");
    File dispatchFile = new File(directory, "dispatch.class");

    if (!itf.exists())
      return null;
    
    bytecode = readClass(pkg.getName() + "." + Package.packageClassName);
    dispatch = readClass(pkg.getName() + ".dispatch");

    if (bytecode == null || dispatch == null)
      return null;

    lastCompilation = Math.min(itf.lastModified(), 
			       dispatchFile.lastModified());

    return itf;
  }

  private BufferedReader read(File f)
  {
    try{
      return new BufferedReader(new FileReader(f));
    }
    catch(FileNotFoundException e){
      User.error(nice.tools.util.System.prettyPrint(f) +
		 " of package " + pkg.getName() + " could not be found");
      return null;
    }
  }

  void addClasses(java.util.Set/*<Content.Stream>*/ classes)
  {
    addClasses(classes, directory);
  }

  static void addClasses(java.util.Set/*<Content.Stream>*/ classes, File directory)
  {
    File[] files = directory.listFiles
      (new FileFilter()
	{ 
	  public boolean accept(File f)
	  { 
	    String name = f.getName();

            if (name.equals("package.nicei"))
              return true;

	    return name.endsWith(".class") && f.isFile();
	  }
	}
       );

    for (int i = 0; i < files.length; i++)
      try{
        classes.add(new Content.Stream
	  (new BufferedInputStream(new FileInputStream(files[i])),
           files[i].getName()));
      } catch(FileNotFoundException e) {}
  }

  private InputStream getFileStream(String name)
  {
    File f = new File(directory, name);

    try{
      return new FileInputStream(f);
    }
    catch(FileNotFoundException e){}

    return null;
  } 

  public String getName()
  {
    return nice.tools.util.System.prettyPrint(directory);
  }
  
  public String toString()
  {
    return "Compiled package in: " + getName();
  }
  
  private Package pkg;
  private File directory;
}
