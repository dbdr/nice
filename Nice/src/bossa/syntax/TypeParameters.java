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
//$Modified: Wed Sep 15 15:38:47 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.typing.Variance;

/**
 * Ground type parameters.
 * Holds a colloction of Monotype.
 */
public class TypeParameters
{
  /**
   * Constructs type parameters
   *
   * @param typeParameters a collection of Monotype
   */
  public TypeParameters(List typeParameters)
  {
    if(typeParameters==null)
      typeParameters=new ArrayList(0);
    this.content=typeParameters;
  }

  TypeParameters(LocatedString s, Variance v)
  {
    Internal.error(v==null,s,s+" has no variance");
    this.content=Monotype.freshs(v.size,s);
  }
  
  static TypeParameters fromSymbols(Collection symbols)
  {
    List res=new ArrayList(symbols.size());
    Iterator i=symbols.iterator();
    while(i.hasNext())
      {
	TypeSymbol s=(TypeSymbol) i.next();
	if(s instanceof ClassDefinition)
	  res.add(new MonotypeConstructor(((ClassDefinition) s).tc,null,null));
	else if(s instanceof MonotypeVar)
	  res.add((MonotypeVar)s);
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

  public int size()
  {
    return content.size();
  }

  public Iterator iterator()
  {
    return content.iterator();
  }
  
  public List /* of Monotype */ content;
}
