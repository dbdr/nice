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

import bossa.util.*;
import java.io.*;

/**
   An abstract package source, where source or interface files 
   and compiled code if applicable can be found.
   
   @version $Date$
   @author Daniel Bonniot
 */

abstract class SourceContent
{
  abstract Content.Unit[] getDefinitions();

  /** Date of the last modification of the source of this package. */
  long lastModification;

  /** Date of the last succesful compileation of this package. */
  long lastCompilation;

  boolean sourcesRead;
  
  /** @return the directory in which to place generated files 
      of this package. 
  */
  abstract File getOutputDirectory();

  /** return a short name to display this package source
      (typically a file name, an URL, ...) */
  abstract public String getName();

  /** return a longer string that identifies the type of package source too. */
  abstract public String toString();
}
