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

package mlsub.typing;

/**
   An atomic kind. Its elements cannot be further decomposed,
   they are compared in a constraint.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public interface AtomicKind extends mlsub.typing.lowlevel.Kind
{
  mlsub.typing.lowlevel.Engine.Constraint getConstraint();

  /** arity of the elements. */
  int arity();
}
