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

// File    : PackageSource.java
// Created : Tue Aug 01 16:18:53 2000 by Daniel Bonniot
//$Modified: Tue Aug 01 18:32:57 2000 by Daniel Bonniot $

package bossa.modules;

import bossa.util.*;
import java.io.*;

/**
 * An abstract package source.
 * 
 * @author Daniel Bonniot
 */

abstract class PackageSource
{
  abstract BufferedReader[] getDefinitions(boolean forceReload);

  long lastModification;

  boolean sourcesRead;
  
  gnu.bytecode.ClassType bytecode;

  /** return the directory in which to place generated files from this package. */
  abstract File getOutputDirectory();

  /** 
      Return an IntputStream to read the existing bytecode, or null. 
      
      The bytecode here existed before the current compilation.
  */
  abstract InputStream getBytecodeStream();
  
  /** return a short name to display this package source. */
  abstract public String getName();

  /** return a longer string that identifies the type of package source too. */
  abstract public String toString();
}
