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
   An abstract parent for Compilation.

   Allows methods to be in the Java style.

   @author  (bonniot@users.sourceforge.net)
 */

public abstract class CompilationInterface 
  extends mlsub.compilation.Compilation
{
  public abstract void error(bossa.util.UserError ex);
  public abstract void warning(bossa.util.Location loc, String message);
  public abstract void exitIfErrors();

  public abstract void progress(Package pkg, String phase);

  public abstract void addNumberOfDeclarations(int number);
  public abstract int  getNumberOfDeclarations();
  public abstract void addProgress(float weight);
}

