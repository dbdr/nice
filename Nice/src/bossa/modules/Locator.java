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

import java.io.*;
import java.util.LinkedList;
import java.util.jar.JarFile;
import bossa.util.Debug;

/**
   Locates package definitions.

   @version $Date$
   @author Daniel Bonniot (Daniel.Bonniot@inria.fr)
 */

final class Locator
{
  Locator (Compilation compilation)
  {
    sourceRoots = splitPath(compilation.sourcePath);
    packageRoots = splitPath(compilation.packagePath + File.pathSeparator + 
			     compilation.destinationDir + File.pathSeparator + 
			     Debug.getProperty("nice.systemJar", ""));
  }

  Content find (Package pkg)
  {
    SourceContent source = null;
    CompiledContent compiled = null;

    String filesystemName = pkg.getName().replace('.', File.separatorChar);
    
    for (int i = 0; source == null && i < sourceRoots.length; i++)
      // source files cannot be in Jar files
      if (sourceRoots[i] instanceof File)
	source = DirectorySourceContent.create
	  (pkg, new File((File) sourceRoots[i], filesystemName));

    for (int i = 0; compiled == null && i < packageRoots.length; i++)
      if (packageRoots[i] instanceof File)
	compiled = DirectoryCompiledContent.create
	  (pkg, new File((File) packageRoots[i], filesystemName));
      else
	compiled = JarCompiledContent.create(pkg, (JarFile) packageRoots[i]);
    
    Content res = new Content(pkg, source, compiled);

    if (Debug.modules)
      Debug.println("Locating " + pkg.getName() + ":\n" + res);

    return res;
  }

  /****************************************************************
   * Private
   ****************************************************************/
  
  /** where to find source files. */
  private final Object[] sourceRoots;

  /** where to find compiled packages. */
  private final Object[] packageRoots;

  private static Object[] splitPath (String path)
  {
    LinkedList res = new LinkedList();
    
    int start = 0;
    // skip starting separators
    while (start<path.length() && 
	   path.charAt(start) == File.pathSeparatorChar)
      start++;
    
    while(start<path.length())
      {
	int end = path.indexOf(File.pathSeparatorChar, start);
	if (end == -1)
	  end = path.length();
	
	String pathComponent = path.substring(start, end);
	if (pathComponent.length() > 0)
	  {
	    File f = nice.tools.util.System.getFile(pathComponent);
	    // Ignore non-existing directories and archives
	    if (f.exists())
	      {
		if (pathComponent.endsWith(".jar"))
		  try{
		    res.add(new JarFile(f));
		  }
		  catch(IOException e){}
		else
		  res.add(f);
	      }
	  }
	start = end + 1;
      }

    return res.toArray(new Object[res.size()]);
  }
}
