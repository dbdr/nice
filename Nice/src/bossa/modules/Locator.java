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
import bossa.util.User;

/**
   Locates package definitions.

   @version $Date$
   @author Daniel Bonniot (Daniel.Bonniot@inria.fr)
 */

final class Locator
{
  Locator (Compilation compilation, String classpath)
  {
    sources = new nice.tools.locator.Locator
      (compilation.sourcePath,
       new gnu.mapping.Procedure1() {
         public Object apply1(Object o) {
           String message = (String) o;
           User.warning(message);
           return null;
         }
       });

    packages = new nice.tools.locator.Locator
      (classpath,
       new gnu.mapping.Procedure1() {
         public Object apply1(Object o) {
           String message = (String) o;
           User.warning(message);
           return null;
         }
       });
  }

  Content find (Package pkg)
  {
    SourceContent source = null;
    CompiledContent compiled = null;

    String name = pkg.getName().replace('.', '/');

    java.net.URLConnection sroot = sources.get(name);
    java.net.URLConnection croot = packages.get(name + "/package.nicei");

    if (sroot != null)
      source = DirectorySourceContent.create(pkg, sroot.getURL());

    if (croot != null)
      compiled = CompiledContent.create(pkg, croot.getURL());

    Content res = new Content(pkg, source, compiled);

    if (Debug.modules)
      Debug.println("Locating " + pkg.getName() + ":\n" + res);

    return res;
  }

  /****************************************************************
   * Private
   ****************************************************************/

  /** where to find source files. */
  private final nice.tools.locator.Locator sources;

  /** where to find compiled packages. */
  private final nice.tools.locator.Locator packages;
}
