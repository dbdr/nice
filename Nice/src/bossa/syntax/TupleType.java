/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : TupleType.java
// Created : Wed Aug 02 19:00:48 2000 by Daniel Bonniot
//$Modified: Wed Aug 02 19:06:18 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

import mlsub.typing.*;
import mlsub.typing.Monotype;
import mlsub.typing.TupleKind;


/**
   A tuple of types.
   
   @author Daniel Bonniot
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

  public Monotype resolve(TypeScope typeScope)
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
  
  public boolean equals(Object o)
  {
    if (!(o instanceof TupleType))
      return false;
    TupleType that = (TupleType) o;
    
    return types.equals(that.types);
  }
  
  private List types;
}
