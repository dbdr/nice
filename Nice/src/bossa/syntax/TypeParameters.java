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

import java.util.*;
import bossa.util.*;
import mlsub.typing.Variance;

/**
   Type parameters.
   Holds a colloction of Monotype.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
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
    if(v==null)
      Internal.error(s,s+" has no variance");
    this.content = Monotype.freshs(v.size,s);
  }

  static TypeParameters fromMonotypeVars(Collection symbols)
  {
    List res = new ArrayList(symbols);
    return new TypeParameters(res);
  }
  
  mlsub.typing.Monotype[] resolve(TypeScope ts)
  {
    return Monotype.resolve(ts,content);
  }

  public String toString()
  {
    return Util.map("<",", ",">", content);
  }

  public int size()
  {
    return content.size();
  }

  public Iterator iterator()
  {
    return content.iterator();
  }

  public boolean equals(TypeParameters that)
  {
    return content.equals(that.content);
  }
  
  public List /* of Monotype */ content;
}
