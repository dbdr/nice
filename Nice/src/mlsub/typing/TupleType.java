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
// Created : Wed Aug 02 15:21:55 2000 by Daniel Bonniot
//$Modified: Wed Aug 02 17:55:47 2000 by Daniel Bonniot $

package mlsub.typing;

import mlsub.typing.lowlevel.Kind;

/**
   A tuple type.
   
   @author Daniel Bonniot
 */

public class TupleType extends Monotype
{
  public TupleType(Monotype[] types)
  {
    this.types = types;

    kind = TupleKind.get(types.length);
  }

  public boolean isRigid()
  {
    return Monotype.isRigid(types);
  }
  
  Monotype substitute(java.util.Map map)
  {
    return new TupleType(Monotype.substitute(map, types));
  }
  
  /****************************************************************
   * low-level interface
   ****************************************************************/

  public int getId() 		{ throw new Error(); }
  public void setId(int value) 	{ throw new Error(); }
  
  final Kind kind;
  
  public Kind getKind() 	  { return kind; }
  public void setKind(Kind value) { throw new Error(); }
  
  /****************************************************************
   * Misc.
   ****************************************************************/

  public boolean equals(Object o)
  {
    if (!(o instanceof TupleType))
      return false;
    
    return types.equals(((TupleType) o).types);
  }
  
  public String toString()
  {
    return bossa.util.Util.map("<", ", ", ">", types);
  }

  Monotype[] types;
}
