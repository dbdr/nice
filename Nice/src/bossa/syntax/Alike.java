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

import bossa.util.*;
import java.util.*;

/**
   The "alike" syntactic keyword in Monotypes

   @version $Date$
   @author Daniel Bonniot
 */

public class Alike extends Monotype
{
  public Alike(List parameters, Location location)
  {
    this.parameters = parameters;
    this.location = location;
  }

  // Alike should be removed as soon as types are constructed.

  public mlsub.typing.Monotype resolve(TypeMap ts)
  {
    User.error(this, "\"alike\" can only be used in class method definitions");
    return null;
  }

  void typecheck()
  {
    Internal.error("Alike not resolved");
  }

  // common ID object
  static Object id = new Object();
  
  Monotype substitute(Map map)
  {
    Object res = map.get(id);

    if (res == null)
      return this;
    else
      return new MonotypeConstructor
	((mlsub.typing.TypeConstructor) res, 
	 new TypeParameters(Monotype.substitute(map, parameters)), 
	 location);
  }

  boolean containsAlike()
  {
    return true;
  }
  
  Monotype cloneType()
  {
    return this;
  }
  
  public String toString()
  {
    return "alike"+Util.map("<", ", ", ">", parameters);
  }

  public Location location()
  {
    return location;
  }
  
  private List parameters;
  private Location location;
}
