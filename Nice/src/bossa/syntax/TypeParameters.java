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
    this(Monotype.toArray(typeParameters));
  }

  public TypeParameters(Monotype[] typeParameters)
  {
    this.content = typeParameters;
  }

  mlsub.typing.Monotype[] resolve(TypeMap ts)
  {
    return Monotype.resolve(ts, content);
  }

  public String toString()
  {
    return Util.map("<",", ",">", content);
  }

  public Monotype[] content;
}
