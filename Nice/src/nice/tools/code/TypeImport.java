/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.code;

import gnu.bytecode.*;

import bossa.util.User;
import bossa.util.Debug;

import java.util.*;
import java.io.File;
import java.net.URL;

/**
   Utilities to import types from native libraries.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class TypeImport
{
  public static void setRuntime(String file)
  {
    runtime = file;
  }

  private static String runtime;

  public static String getRuntime() { return runtime; }

  public static Type lookup(bossa.syntax.LocatedString className)
  {
    return lookup(className.toString(), className.location());
  }

  public static Type lookup(String className, bossa.util.Location loc)
  {
    Type res = null;

    try {
      res = lookupQualified(className);
    }
    catch(NoClassDefFoundError e) {

      // Note: this can also happen when a class with similar name but with 
      // different case exists (on case-insensitive file-systems like FAT).

      User.error(loc, "Class " + className + " depends on class " + 
		 e.getMessage().replace('/', '.') +
		 ", which is not available on the classpath");
    }

    if (res != null)
      return res;

    String[] pkgs = bossa.syntax.Node.getGlobalTypeScope().module.listImplicitPackages();
    for (int i = 0; i < pkgs.length; i++)
      {
	Type found = lookupQualified(pkgs[i] + "." + className);
	if (found != null)
	  {
	    if (i == 0) // The current package: no ambiguity possible
	      return found;

	    if (res != null)
	      User.error(loc, "Ambiguity for native class " + className + 
			 ":\n" + res.getName() + " and " + found.getName() +
			 " both exist");

	    res = found;
	  }
      }
    if (res != null)
      return res;

    int lastDot = className.lastIndexOf('.');
    if (lastDot != -1)
      {
	char[] chars = className.toCharArray();
	chars[lastDot] = '$';
	return lookup(new String(chars), loc);
      }

    return null;
  }

  public static Type lookupQualified(String className)
  {
    Type res = Type.lookupType(className);
    if (res != null)
      return res;

    Class c = lookupQualifiedJavaClass(className);

    if (c == null)
      return null;

    return gnu.bytecode.Type.make(c);
  }

  /****************************************************************
   * On the fly lookup of java types
   ****************************************************************/

  /** Search className in opened packages too */
  static java.lang.Class lookupJavaClass(String className)
  {
    Class res = lookupQualifiedJavaClass(className);

    if (res != null)
      return res;
    
    String[] pkgs = bossa.syntax.Node.getGlobalTypeScope().module.listImplicitPackages();
    for (int i = 0; i < pkgs.length; i++)
	{
	  res = lookupQualifiedJavaClass(pkgs[i] + "." + className);
	  if(res != null)
	    return res;
	}
    return null;
  }

  static HashMap stringToReflectClass = new HashMap();

  /**
      Searches a native class given by its fully qualified name
      in the user classpath.
      
      This is to be prefered to Class.forName, which searches 
      in compiler's runtime classpath.
      
      This method does not search in opened packages.
      It uses a hash-table, to speed up multiple lookups on the same name.

      @return the java.lang.Class object corresponding to the class name,
      or null if the class does not exists or is ill-formed.
  */
  public static Class lookupQualifiedJavaClass(String className)
  {
    if (stringToReflectClass.containsKey(className))
      return (Class) stringToReflectClass.get(className);

    Class c = null;

    try {
      c = classLoader.loadClass(className);
    }
    catch(ClassNotFoundException e) {} // The class does not exist.
    catch(NoClassDefFoundError e)
      {
        User.error("Class "+className+" depends on class "+
		  e.getMessage().replace( '/', '.' ) + " which is not available on the classpath." );
      }
    catch(UnsupportedClassVersionError e)
      {
	User.error("Class "+className+" could not be loaded.  The version of its class "+
			"file is not supported by the running JVM." );
      }

    stringToReflectClass.put(className, c);

    return c;
  }

  private static java.net.URLClassLoader classLoader;
  private static String currentClasspath = "NOT INITIALIZED";

  public static void setClasspath(String classpath)
  {
    /* Cache: do not reset the classloader if the classpath is unchanged.
       This it especially important as it seems the previous classloader
       and its classes do not get garbage collected.
    */
    if (currentClasspath.equals(classpath))
      return;

    currentClasspath = classpath;

    URL[] path = nice.tools.locator.dispatch.parsePath
      (classpath,
       new gnu.mapping.Procedure1() {
         public Object apply1(Object o) {
           String message = (String) o;
           User.warning(message);
           return null;
         }
       });

    classLoader = new java.net.URLClassLoader(path, /* no parent */ null);
  }
}
