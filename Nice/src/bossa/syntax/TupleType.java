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

import mlsub.typing.*;
import mlsub.typing.Monotype;
import mlsub.typing.TupleKind;


/**
   A tuple of types.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class TupleType extends bossa.syntax.Monotype
{
  public TupleType(List /* of Monotype */ types, Location location)
  {
    this.types = types;
    this.location = location;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  public Monotype resolve(TypeMap typeScope)
  {
    return new mlsub.typing.TupleType
      (bossa.syntax.Monotype.resolve(typeScope, types));
  }

  bossa.syntax.Monotype substitute(Map map)
  {
    return new bossa.syntax.TupleType
      (bossa.syntax.Monotype.substitute(map, types), location);
  }

  boolean containsAlike()
  {
    return bossa.syntax.Monotype.containsAlike(types);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return location;
  }
  private Location location;

  public String toString()
  {
    return Util.map("<",", ",">", types);
  }
  
  private List types;
}
