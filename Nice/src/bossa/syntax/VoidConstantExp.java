/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;

/**
   The void constant.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class VoidConstantExp extends ConstantExp
{
  public VoidConstantExp()
  {
    className = voidName;
    value = gnu.mapping.Values.empty;
  }

  private static LocatedString voidName = 
    new LocatedString("void", Location.nowhere());

  public String toString()
  {
    return "{}";
  }
}
