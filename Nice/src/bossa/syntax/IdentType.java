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

// File    : IdentType.java
// Created : Fri Jul 02 17:30:17 1999 by bonniot
//$Modified: Fri Jul 09 19:23:28 1999 by bonniot $
// Description : Base type (not a function)

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class IdentType extends Type
{
  public IdentType(LocatedString name, Collection parameters)
  {
    this.name=name;
    this.parameters=parameters;
  }

  Type instantiate(Collection typeParameters)
  {
    //TODO : transform to error
    Internal.warning("IdentType can not be instanciated, it shouldn't live so late in the passes");
    return this;
  }

  Type resolve(TypeScope scope)
  {
    TypeSymbol s=scope.lookup(this);
    User.warning(s==null,"Unknown type : "+name);
    return new TypeSymbolType(s,resolve(scope,parameters));
  }

  public String toString()
  {
    return "\""+name+Util.map("<",", ",">",parameters)+"\"";
  }

  public String toStringExtern()
  /* it is usefull only for FunType */
  {
    return toString();
  }

  LocatedString name;
  private Collection parameters;
}
