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

/**
   The bytecode type of a tuple.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

import gnu.bytecode.*;

public class TupleType extends SpecialArray
{
  TupleType (Type arrayType, Type[] componentTypes)
  {
    super (arrayType);
    this.componentTypes = componentTypes;
  }

  TupleType (Type[] componentTypes)
  {
    this(Types.lowestCommonSupertype(componentTypes), componentTypes);
  }

  public Type[] componentTypes;

  public static gnu.expr.Expression createExp
    (Type arrayType, Type[] componentTypes,
     gnu.expr.Expression[] components)
  {
    return new gnu.expr.ApplyExp
      (new nice.tools.code.LiteralArrayProc
       (new TupleType(arrayType, componentTypes), components.length),
       components);
  }
}
