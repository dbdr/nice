/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : TypeSymbolType.java
// Created : Fri Jul 09 15:32:38 1999 by bonniot
//$Modified: Fri Jul 09 20:55:27 1999 by bonniot $
// Description : Type made of a symbol

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class TypeSymbolType extends Type
{
  public TypeSymbolType(TypeSymbol s, Collection parameters)
  {
    this.symbol=s;
    this.parameters=parameters;
  }

  Type resolve(TypeScope s)
  {
    Internal.warning("resolve in TypeSymbolType : it has already been done");
    return this;
  }

  Type instantiate(Collection c)
  {
    //TODO:
    return this;
  }

  public String toString()
  {
    Internal.warning(symbol==null,"null symbol");
    if(symbol==null)
      return "?";
    Internal.warning(parameters==null,"null parameters in "+symbol.name);
    return 
      symbol.name.toString()
      + Util.map("<",", ",">",parameters);
  }

  TypeSymbol symbol;
  Collection /* of Type */ parameters;
}
