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
//$Modified: Thu Jul 22 10:25:48 1999 by bonniot $
// Description : Type made of a symbol

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class TypeSymbolType extends Monotype
{
  public TypeSymbolType(TypeSymbol s, Collection parameters)
  {
    this.symbol=s;
    if(parameters==null)
      this.parameters=new ArrayList(0);
    else
      this.parameters=parameters;
  }

  Monotype cloneType()
  {
    return this;
  }

  Monotype resolve(TypeScope typeScope)
  {
    // Nothing to do !
    return this;
  }

  VarScope memberScope()
  {
    return symbol.memberScope();
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return symbol.location().englobe(parameters);
  }

  public String toString()
  {
    Internal.warning(parameters==null,"null parameters in "+symbol.name);
    return 
      toStringBase()
      + Util.map("<",", ",">",parameters);
  }

  public String toStringBase()
  {
    Internal.error(symbol==null,"null symbol");
    return symbol.name.toString();
  }

  TypeSymbol symbol;
  Collection /* of Monotype */ parameters;
}
