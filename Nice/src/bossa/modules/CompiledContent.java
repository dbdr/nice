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

/**
   An abstract package source, where source or interface files 
   and compiled code if applicable can be found.
   
   @version $Date$
   @author Daniel Bonniot
 */

abstract class CompiledContent
{
  abstract Content.Unit[] getDefinitions();

  /** Date of the last succesful compilation of this package. */
  long lastCompilation;

  gnu.bytecode.ClassType bytecode;
  gnu.bytecode.ClassType dispatch;

  abstract void addClasses(java.util.Set/*<Content.Stream>*/ classes);

  /**
     Read a compiled class.

     @param name the fully qualified name of the class
  */
  abstract gnu.bytecode.ClassType readClass(String name);

  /** return a short name to display this package source
      (typically a file name, an URL, ...) */
  abstract public String getName();

  /** return a longer string that identifies the type of package source too. */
  abstract public String toString();

  static CompiledContent create(Package pkg, java.net.URL url)
  {
    if (url.getProtocol().equals("file"))
      {
        File dir = new File(url.getFile()).getParentFile();
        return DirectoryCompiledContent.create(pkg, dir);
      }

    if (url.getProtocol().equals("jar"))
      {
        String jar = url.getFile();
        jar = jar.substring(jar.indexOf(':') + 1, jar.indexOf('!'));
        try {
          return JarCompiledContent.create
            (pkg, new java.util.jar.JarFile(jar));
        }
        catch(IOException e) {
          return null;
        }
      }

    return null;
  }
}
