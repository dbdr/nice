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

// File    : DirectorySource.java
// Created : Tue Aug 01 16:24:45 2000 by Daniel Bonniot
//$Modified: Wed Oct 04 11:52:04 2000 by Daniel Bonniot $

package bossa.modules;

import bossa.util.*;
import java.io.*;

import gnu.bytecode.ClassType;

/**
 * A package located in a directory.
 * 
 * @author Daniel Bonniot
 */

class DirectorySource extends PackageSource
{
  static DirectorySource create(Package pkg, File directory)
  {
    if (!directory.exists())
      return null;

    DirectorySource res = new DirectorySource(pkg, directory);
    if (res.isValid())
      return res;

    return null;
  }

  DirectorySource(Package pkg, File directory)
  {
    this.pkg = pkg;
    this.directory = directory;

    this.sources = getSources();
    this.itf = getInterface();
  }

  private File[] sources;
  private File itf;

  /** @return true if this directory indeed hosts a Nice package. */
  private boolean isValid()
  {
    return sources.length > 0 || itf != null;
  }

  Unit[] getDefinitions(boolean forceReload)
  {
    lastModification = maxLastModification(sources);
    lastCompilation = itf == null ? -1 : itf.lastModified();
    
    if (!forceReload && lastModification <= lastCompilation)
      return new Unit[]{ new Unit(read(itf), itf.toString()) };

    if (Debug.modules && !forceReload && itf != null)
      Debug.println(pkg + " has changed, recompiling");
    
    if (sources.length == 0)
      User.error(pkg.name, 
		 "Package " + pkg.getName() + 
		 " has no source file in " +
		 nice.tools.util.System.prettyPrint(directory));
    
    sourcesRead = true;
    Unit[] res = new Unit[sources.length];
    for(int i = 0; i<res.length; i++)
      res[i] = new Unit(read(sources[i]), sources[i].toString());
    
    return res;
  }
  
  private long maxLastModification(File[] files)
  {
    long res = 0;
    for(int i = 0; i<files.length; i++)
      {
	long time = files[i].lastModified();
	if (time>res) res = time;
      }
    return res;
  }
  
  private File[] getSources()
  {
    File[] res = directory.listFiles
      (new FilenameFilter()
	{ 
	  public boolean accept(File dir, String name)
	  { return name.endsWith(pkg.sourceExt); }
	}
       );
    if (res == null)
      User.error(pkg, "Could not list source files in " + getName());
    
    // put nice.lang.prelude first if it exists
    if (pkg.name.toString().equals("nice.lang"))
      for(int i = 0; i<res.length; i++)
	if (res[i].getName().equals("prelude" + pkg.sourceExt))
	  {
	    File tmp = res[i];
	    res[i] = res[0];
	    res[0] = tmp;
	    break;
	  }
    
    return res;
  }

  private ClassType readClass(String name)
  {
    InputStream s = getFileStream(name);
    if (s == null)
      return null;
    
    ClassType res = null;    
    try{ res = gnu.bytecode.ClassFileInput.readClassType(s); }
    catch(LinkageError e){}
    catch(IOException e){}
    
    return res;
  }
  
  private File getInterface()
  {
    File itf = new File(directory, "package.nicei");

    if (!itf.exists())
      return null;
    
    bytecode = readClass("package.class");
    dispatch = readClass("dispatch.class");

    if (bytecode == null || dispatch == null)
      return null;

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
  
  InputStream getBytecodeStream()
  {
    return getFileStream("package.class");
  }
  
  private InputStream getFileStream(String name)
  {
    File f = new File(directory, name);

    if (f != null)
      try{
	return new FileInputStream(f);
      }
      catch(FileNotFoundException e){}

    return null;
  } 

  File getOutputDirectory()
  {
    return directory;
  }
  
  public String getName()
  {
    return nice.tools.util.System.prettyPrint(directory);
  }
  
  public String toString()
  {
    return "Directory package source: " + getName();
  }
  
  private Package pkg;
  private File directory;
}
