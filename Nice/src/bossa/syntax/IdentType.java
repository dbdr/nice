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
//$Modified: Tue Jul 13 16:28:48 1999 by bonniot $
// Description : Base type (not a function) before scoping resolution

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class IdentType extends Monotype
{
  public IdentType(LocatedString name, Collection parameters)
  {
    this.name=name;
    this.parameters=parameters;
  }

  Monotype cloneType()
  {
    return this;
  }

  Monotype resolve(TypeScope typeScope)
  {
    TypeSymbol s=typeScope.lookup(this);
    User.error(s==null,name.location(),"Unknown type \""+name+"\"");
    return new TypeSymbolType(s,resolve(typeScope,parameters));
  }

  public String toString()
  {
    return "\""+name+Util.map("<",", ",">",parameters)+"\"";
  }

  LocatedString name;
  private Collection parameters;
}
