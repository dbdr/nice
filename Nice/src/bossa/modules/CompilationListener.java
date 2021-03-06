/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.modules;

/**
   The listener interface for compilation events.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

import bossa.util.*;


public interface CompilationListener
{
  void error  (Location location, String message);
  void warning(Location location, String message);

  /** A bug occured in the compiler. 
      @param url the adress where a bug report should be submitted.
   */
  void bug(String stackTrace, String url);

  /** Reports the progress of compilation.
      phase can be: parsing, type-checking, generating code, ...
      the package can be null if the phase applies to the whole program 
      (testing dispatch, creating the archive, compiling to native code, ...).
  */
  void progress(String packageName, String phase);

  /** Gives an approximation of how much of the compilation has been completed.
      @param proportion the current progress 
        (0.0 = just started, 1.0 = complete).
  */
  void progress(float proportion);
}
