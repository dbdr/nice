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

import mlsub.typing.TypeSymbol;

/**
   A mapping from type names to type symbols.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class TypeMaper implements TypeMap
{
  public TypeMaper(nice.tools.ast.SymbolTable inner, TypeScope global)
  {
    this.inner = inner;
    this.global = global;
  }
  
  nice.tools.ast.SymbolTable inner;
  TypeScope global;
  
  public TypeSymbol lookup(LocatedString name)
  {
    TypeSymbol res = (TypeSymbol) nice.tools.ast.dispatch.get(inner, name.toString());
    if (res != null)
      return res;
    else
      return global.lookup(name);
  }
}
