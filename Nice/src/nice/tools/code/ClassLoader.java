/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.code;

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
    this.locator = new nice.tools.locator.Locator
      (classpath,
       new gnu.mapping.Procedure1() {
         public Object apply1(Object o) {
           return null;
         }
       });
  }

  public ClassType load(String className)
  {
    String name = className.replace('.', '/') + ".class";

    java.net.URLConnection resource = locator.get(name);

    if (resource == null)
      return null;

    try {
      InputStream input = resource.getInputStream();
      ClassType res = new ClassType();
      registrar.register(className, res);
      new ClassFileInput(res, input);
      return res;
    }
    catch (IOException ex) {
      return null;
    }
  }

  /****************************************************************
   * Private implementation
   ****************************************************************/

  /** where to find compiled packages. */
  private final nice.tools.locator.Locator locator;

  /** Map from class names to types.
      Used to avoid loading several copies of the same class.
   */
  private final Registrar registrar;

  public abstract static class Registrar
  {
    public abstract void register(String className, ClassType type);
  }
}
