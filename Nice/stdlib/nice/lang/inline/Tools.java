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

package nice.lang.inline;

import gnu.bytecode.*;
import gnu.expr.*;

/**
   Utility functions for inlined operators.

   @version $Date$
   @author Daniel Bonniot
*/

class Tools
{
  static StackTarget intTarget = new StackTarget(Type.int_type);

  static Type type(char typeChar)
  {
    switch(typeChar)
      {
      case 'z': return Type.boolean_type;
      case 'b': return Type.byte_type;
      case 's': return Type.short_type;
      case 'c': return Type.char_type;
      case 'i': return Type.int_type;
      case 'l': return Type.long_type;
      case 'f': return Type.float_type;
      case 'd': return Type.double_type;
      case 'o': return Type.pointer_type;
      default:  return null;
      }
  }

  static PrimType numericType(char typeChar)
  {
    switch(typeChar)
      {
      case 'i': return Type.int_type;
      case 'l': return Type.long_type;
      case 'f': return Type.float_type;
      case 'd': return Type.double_type;
      default:  return null;
      }
  }
}
