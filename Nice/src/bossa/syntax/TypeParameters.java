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

// File    : TypeParameters.java
// Created : Mon Jul 12 17:51:12 1999 by bonniot
//$Modified: Wed Jul 28 21:39:14 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Ground type parameters
 */
public class TypeParameters
{
  /**
   * Constructs type parameters
   *
   * @param typeParameters a collection of Monotype
   */
  public TypeParameters(Collection typeParameters)
  {
    if(typeParameters==null)
      typeParameters=new ArrayList(0);
    this.content=typeParameters;
  }

  TypeParameters(LocatedString s, Variance v)
  {
    this.content=Monotype.freshs(v.size,s);
  }
  
  static TypeParameters fromSymbols(Collection symbols)
  {
    Collection res=new ArrayList(symbols.size());
    Iterator i=symbols.iterator();
    while(i.hasNext())
      {
	TypeSymbol s=(TypeSymbol) i.next();
	res.add(new MonotypeConstructor(((ClassDefinition) s).tc,null));
      }
    return new TypeParameters(res);
  }

  TypeParameters resolve(TypeScope ts)
  {
    return new TypeParameters(Monotype.resolve(ts,content));
  }

  public String toString()
  {
    return Util.map("<",", ",">",true,content);
  }

  int size()
  {
    return content.size();
  }

  Iterator iterator()
  {
    return content.iterator();
  }
  
  Collection /* of Monotype */ content;
}
