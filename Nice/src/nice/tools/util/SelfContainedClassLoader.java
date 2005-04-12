/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2005                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.util;

/**
   A class loader that does not delegate to any parent, except
   java* classes to the system classloader.

   @author Daniel Bonniot (bonniot@users.sf.net)
 */

public class SelfContainedClassLoader extends java.net.URLClassLoader
{
  public SelfContainedClassLoader(java.net.URL[] urls)
  {
    super(urls);
  }

  protected Class loadClass(String name, boolean resolve)
    throws ClassNotFoundException
  {
    Class res = this.findLoadedClass(name);

    if (res == null)
      try {
        res = this.findClass(name);
      } catch (ClassNotFoundException e) {}

    if (res == null && name.startsWith("java"))
      {
        res = ClassLoader.getSystemClassLoader().loadClass(name);
      }

    if (res == null)
      throw new ClassNotFoundException(name);

    if (resolve)
      this.resolveClass(res);

    return res;
  }
}
