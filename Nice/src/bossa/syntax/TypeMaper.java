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
   @author Daniel Bonniot (bonniot@users.sf.net)
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
    return lookup(name.toString(), name.location());
  }

  public TypeSymbol lookup(String name)
  {
    return lookup(name, null);
  }

  public TypeSymbol lookup(String name, bossa.util.Location loc)
  {
    TypeSymbol res = (TypeSymbol) inner.get(name);
    if (res != null)
      return res;
    else
      return global.lookup(name, loc);
  }
}
