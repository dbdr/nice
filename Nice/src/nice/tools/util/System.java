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

// File    : System.java
// Created : Tue Aug 01 15:12:24 2000 by Daniel Bonniot
//$Modified: Tue Aug 01 17:53:32 2000 by Daniel Bonniot $

package nice.tools.util;

import java.io.File;
import java.util.jar.JarFile;

/**
 * Communication with the system environment.
 * 
 * @author Daniel Bonniot
 */

public class System
{
  /**
     Return a string to nicely display the file.

     If the file lies in the user home directory
     (as indicated by the <tt>user.home</tt> system property),
     replace this prefix with "~".

     Eg: 
     	/udir/bonniot/Nice/stdlib/nice/lang/package.nicei
     is pretty printed as:
     	~/Nice/stdlib/nice/lang/package.nicei
   */
  public static String prettyPrint(File f)
  {
    return prettyPrint(f.toString());
  }
  
  /** @see #prettyPrint(java.io.File) */
  public static String prettyPrint(JarFile f)
  {
    return prettyPrint(f.getName());
  }
  
  private static String prettyPrint(String name)
  {
    if (name.startsWith(home))
      return "~" + name.substring(homeLength);
    else
      return name;
  }

  private static final String home = java.lang.System.getProperty("user.home");
  private static final int homeLength = home.length();  
}
