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
//$Modified: Fri Nov 05 15:43:54 1999 by bonniot $

package bossa.util;

import bossa.util.*;
import java.util.*;
import java.io.*;

/**
 * Static class for debug output
 * 
 * @author bonniot
 */

public abstract class Debug
{
  public static void println(String msg)
  {
    System.out.println(msg);
  }

  final public static Properties props;
  static 
  {
    props=new Properties();
    try {
      FileInputStream in =
        new FileInputStream(new File(System.getProperty("user.home"),
                                     ".bossa.conf"));
      props.load(new BufferedInputStream(in));
      in.close();
      //props.list(System.out);
    }
    catch (Exception e) {
      println("Can't read "+
	      System.getProperty("user.home")+
	      "/.bossa.conf");
    }
  }

  public static boolean getBoolean(boolean defaultValue, String name)
  {
    String value = props.getProperty(name);
    if (value == null)
      return defaultValue;
    else
      return value.equals("true");
  }

  public static final boolean 
    K0		= getBoolean(false,"debug.K0"),
    typing	= getBoolean(false,"debug.typing"),
    engine	= getBoolean(false,"debug.engine"),
    modules	= getBoolean(false,"debug.modules"),
    IDs		= getBoolean(false,"debug.IDs"),
    overloading = getBoolean(false,"debug.overloading"),
    errorMsg	= getBoolean(false,"debug.errorMsg");
}
