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

/**
 * A package located in a directory.
 * 
 * @author Daniel Bonniot
 */

class DirectorySource extends PackageSource
{
  static DirectorySource create(Package pkg, File directory)
  {
    if (directory.exists())
      return new DirectorySource(pkg, directory);
    return null;
  }

  DirectorySource(Package pkg, File directory)
  {
    this.pkg = pkg;
    this.directory = directory;
  }

  Unit[] getDefinitions(boolean forceReload)
  {
    File[] sources = getSources();
    File itf = getInterface();

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

  private File getInterface()
  {
    File itf = new File(directory, "package.nicei");

    if (!itf.exists())
      return null;
    
    InputStream s = getBytecodeStream();
    if (s == null)
      {
	User.warning("Bytecode for " + this.getName() + 
		     " was not found, altough its interface exists.\n"+
		     "Ignoring and recompiling");
	return null;
      }
    
    try{ bytecode = gnu.bytecode.ClassFileInput.readClassType(s); }
    catch(LinkageError e){}
    catch(IOException e){}

    if (bytecode != null)
      return itf;
    
    User.warning("Bytecode for " + this + 
		 " is not correct, altough its interface exists.\n"+
		 "Ignoring and recompiling");
    
    return null;
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
    File f = new File(directory, "package.class");

    try{
      if (f != null)
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
