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

package nice.tools.util;

import java.io.File;
import java.util.jar.JarFile;

import java.util.Date;
import java.text.DateFormat;

import java.util.List;
import java.util.ArrayList;

/**
   Communication with the system environment.
   
   @version $Date$
   @author Daniel Bonniot
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
    return prettyPrintFile(f.toString());
  }
  
  /** @see #prettyPrint(java.io.File) */
  public static String prettyPrint(JarFile f)
  {
    return prettyPrintFile(f.getName());
  }
  
  public static String prettyPrintFile(String name)
  {
    if (name != null && name.startsWith(home))
      return "~" + name.substring(homeLength);
    else
      return name;
  }

  /**
     Return a file from a string describing it.
     Performs ~ expansion (~ -> user.home property).
  */
  public static File getFile(String file)
  {
    if (file.charAt(0) == '~')
      file = home + file.substring(1);

    return new File(file);
  }

  private static final String home = java.lang.System.getProperty("user.home");
  private static final int homeLength = home.length();  

  /**
     Format dates.
  */

  public static String date(long date)
  {
    return longDate.format(new Date(date));
  }

  private static final DateFormat longDate = DateFormat.getDateTimeInstance();

  /**
     String operations.
  */

  public static String[] split(String str, char separator)
  {
    List res = new ArrayList();
    int ntx = 0;
    int pos = 0;
    while(ntx > -1)
      {
        ntx = str.indexOf(separator, pos);
        if (ntx > -1)
          {
            if (ntx > 0)
              {
                res.add(str.substring(pos, ntx));
              }
            pos = ntx + 1;
          }
        else
          {
            res.add(pos == 0 ? str : str.substring(pos));
          }
      }

    return (String[]) res.toArray(new String[res.size()]);
  }
}
