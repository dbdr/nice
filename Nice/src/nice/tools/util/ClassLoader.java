/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.util;

import gnu.bytecode.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.*;

/**
   A ClassLoader located classes on its classpath and returns
   their representation as a gnu.bytecode.ClassType.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class ClassLoader
{
  public ClassLoader(String classpath, Registrar registrar)
  {
    this.registrar = registrar;
    this.locations = splitPath(classpath);
  }

  public ClassType load(String className)
  {
    String filesystemName = className.replace('.', File.separatorChar) + ".class";
    String jarName = className.replace('.', '/') + ".class";

    for (int i = 0; i < locations.length; i++)
      {
	InputStream input = null;

	if (locations[i] instanceof File)
	  {
	    File dir = (File) locations[i];
	    File file = new File(dir, filesystemName);
	    try {
	      input = new FileInputStream(file);
	    }
	    catch(FileNotFoundException ex) {
	    }
	  }
	else
	  {
	    JarFile jar = (JarFile) locations[i];
	    JarEntry e = jar.getJarEntry(jarName);
	    if (e != null)
	      try {
		input = jar.getInputStream(e);
	      }
	      catch(java.io.IOException ex) {
	      }
	  }

	if (input != null)
	  {
	    ClassType res = new ClassType();
	    registrar.register(className, res);
	    try {
	      new ClassFileInput(res, input);
	      return res;
	    }
	    catch (IOException ex) {
	    }
	  }
      }

    return null;
  }

  /****************************************************************
   * Private implementation
   ****************************************************************/

  /** where to find compiled packages. */
  private final Object[] locations;

  /** Map from class names to types. 
      Used to avoid loading several copies of the same class.
   */
  private final Registrar registrar;

  public abstract static class Registrar
  {
    public abstract void register(String className, ClassType type);
  }

  private static Object[] splitPath (String path)
  {
    LinkedList res = new LinkedList();
    
    int start = 0;
    // skip starting separators
    while (start < path.length() && 
	   path.charAt(start) == File.pathSeparatorChar)
      start++;
    
    while(start < path.length())
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
