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

package bossa.syntax;

public final class NiceUtils
{
  public static gnu.expr.Expression doInline(gnu.mapping.Procedure1 proc)
  {
     return nice.tools.code.Inline.inline(proc);
  }

  public static gnu.expr.Expression doInline(gnu.mapping.Procedure1 proc, gnu.expr.Expression arg1)
  {
     return nice.tools.code.Inline.inline(proc, arg1);
  }

  public static gnu.expr.Expression doInline(gnu.mapping.Procedure2 proc, gnu.expr.Expression arg1, gnu.expr.Expression arg2)
  {
     return nice.tools.code.Inline.inline(proc, arg1, arg2);
  }

  public static gnu.mapping.Procedure getThrowInstance()
  {
    return nice.lang.inline.Throw.instance;
  }


  static ClassLoader inlineLoader;

  static
  {
    String inlinedMethodsRepository = System.getProperty("nice.inlined");
    if (inlinedMethodsRepository != null)
      {
        inlineLoader = new nice.tools.util.DirectoryClassLoader
          (new java.io.File[]{ new java.io.File(inlinedMethodsRepository) },
           null)
          {
            protected Class loadClass(String name, boolean resolve)
              throws ClassNotFoundException
            {
              /* Change the default behviour, which is to look up the 
                 parent classloader first. Instead, look it up after this one,
                 so that the inlined methods are found here, but the
                 interfaces they implement are found in the system classloader,
                 so that the casts for using them succeed.
              */
              Class res = findLoadedClass(name);

              if (res == null)
                try {
                  res = this.findClass(name);
                } 
                catch (ClassNotFoundException ex) {}

              if (res == null)
                {
                  ClassLoader parent = getParent();
                  // A JVM may represent the system classloader by null.
                  if (parent == null)
                    parent = ClassLoader.getSystemClassLoader();
                  res = parent.loadClass(name);
                }

              if (resolve && res != null)
                resolveClass(res);

              return res;
            }
          };
      }
  }

}

