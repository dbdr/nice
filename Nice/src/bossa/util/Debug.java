/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : Debug.java
// Created : Thu Jul 22 15:37:02 1999 by bonniot
//$Modified: Wed May 10 14:33:50 2000 by Daniel Bonniot $

package bossa.util;

import bossa.util.*;
import java.util.*;
import java.io.*;

/**
 * Static class for debug output.
 * 
 * @author bonniot
 */

public abstract class Debug
{
  public static void println(String msg)
  {
    System.out.println(msg);
  }

  public static void print(String msg)
  {
    System.out.print(msg);
  }

  final private static Properties props;
  static 
  {
    props=new Properties();
    try {
      File f = new File(System.getProperty("user.home"),
			".nice.conf");
      
      FileInputStream in = new FileInputStream(f);
      props.load(new BufferedInputStream(in));
      in.close();
    }
    catch (Exception e) {
      //Debug.println("Can't read "+f);
    }
  }

  public static boolean getBoolean(String name, boolean defaultValue)
  {
    String value = props.getProperty(name);
    if (value == null)
      return defaultValue;
    else
      return value.equals("true");
  }

  public static String getProperty(String name, String def)
  {
    String res = props.getProperty(name, null);
    if(res==null)
      res = System.getProperty(name, def);

    return res;
  }
  
  public static final boolean 
    resolution		= getBoolean("debug.resolution",false),
    K0			= getBoolean("debug.K0",false),
    typing		= getBoolean("debug.typing",false),
    engine		= getBoolean("debug.engine",false),
    modules		= getBoolean("debug.modules",false),
    IDs			= getBoolean("debug.IDs",false),
    overloading 	= getBoolean("debug.overloading",false),
    powerUser		= getBoolean("debug.powerUser",false),
    codeGeneration 	= getBoolean("debug.codeGeneration",false),
    javaTypes		= getBoolean("debug.javaTypes",false),
    linkTests           = getBoolean("debug.linkTests",false),
    passes		= getBoolean("debug.passes",false),

    alwaysDumpStack     = getBoolean("debug.alwaysDumpStack", false),
    ignorePrelude	= getBoolean("debug.ignorePrelude", false);
  

  public static final String
    defaultFile = props.getProperty("debug.defaultFile", null);
}
