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

package bossa.syntax;

import java.util.ListIterator;

/**
   A Nice module.

   @version $Date$
   @author Daniel Bonniot
 */

public abstract class Module implements mlsub.compilation.Module
{
  public abstract ListIterator listImplicitPackages();

  public abstract String mangleName(String name);
}
