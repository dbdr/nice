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

package bossa.syntax;

/**
   A type used as an expression.

   @version $Date$
   @author daniel (bonniot@users.sourceforge.net)
 */

class TypeConstantExp extends ConstantExp
{
  TypeConstantExp(LocatedString name)
  {
    super(PrimitiveType.typeTC, name, name.toString(), name.location());
  }

  mlsub.typing.TypeConstructor representedType;
}
