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

package bossa.syntax;

/**
   A macro.

   This interface should be implemented by classes that are used
   to implement inlined methods, and that need to do some semantic checking
   on their actual arguments.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public interface Macro extends gnu.expr.Inlineable
{
  /** 
      For each call site, this method is called with the actual arguments
      of the macro. This can be used to check special requirements on the
      arguments that could not be expressed in the type of the macro.
      For instance, it might be necessary to check that a certain argument
      is assignable if it is modified by the macro.
  */
  void checkSpecialRequirements(Expression[] arguments);
}
