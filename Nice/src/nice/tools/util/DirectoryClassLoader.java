/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.util;

/**
   A class loader that loads from a list of directories.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

import java.io.*;

public class DirectoryClassLoader extends java.lang.ClassLoader
{
  /**
   * @param	dirs 	directories to load from
   * @param	parent	the parent classloader
   */
  public DirectoryClassLoader(File[] dirs, java.lang.ClassLoader parent) {
    super(parent);
    this.dirs = dirs;
  }

  private File[] dirs;

  /**
   * This is the method where the task of class loading
   * is delegated to our custom loader.
   *
   * @param	name the name of the class
   * @return	the resulting <code>Class</code> object
   * @exception	ClassNotFoundException if the class could not be found
   */
  protected Class findClass(String name) throws ClassNotFoundException
  {
    String path = name.replace('.', '/') + ".class";

    for (int i = 0; i < dirs.length; i++)
      {
        Class res = findClass(name, new File(dirs[i], path));
        if (res != null)
          return res;
      }

    throw new ClassNotFoundException(name);
  }

  private Class findClass(String name, File file)
  {
    FileInputStream fi = null;

    try {
      fi = new FileInputStream(file);
    } catch(FileNotFoundException e) {
      return null;
    }

    try {
      byte[] classBytes = new byte[fi.available()];
      fi.read(classBytes);
      return defineClass(name, classBytes, 0, classBytes.length);
    } catch (IOException e) {
      return null;
    } finally {
      try {
        fi.close();
      } catch (java.io.IOException e) {}
    }
  }

  protected java.net.URL findResource(String name)
  {
    for (int i = 0; i < dirs.length; i++)
      {
        File res = new File(dirs[i], name);
        if (res.exists())
          try {
            return res.toURL();
          }
          catch(java.net.MalformedURLException e) {
            // TODO: probably use the following when we drop JDK 1.3:
            // throw new RuntimeException(e);

            return null;
          }
      }

    return null;
  }
}
