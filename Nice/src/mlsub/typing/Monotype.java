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

// File    : Monotype.java
// Created : Thu Jul 01 19:28:28 1999 by bonniot
//$Modified: Tue Jun 13 15:20:42 2000 by Daniel Bonniot $

package mlsub.typing;

import java.util.*;

/**
 * A monomorphic type.
 */
abstract public class Monotype implements mlsub.typing.lowlevel.Element
{
  /**
     A zero length Monotype array.
     Can be shared, since it's empty, thus immutable.
  */
  static final Monotype[] zeroMonotypes = new Monotype[0];

  public TypeConstructor getTC()
  {
    return null;
  }
  
  public Monotype[] getTP()
  {
    return null;
  }

  final public boolean isConcrete()
  {
    // No Monotype lives at runtime.
    return false;
  }
  
  /****************************************************************
   * Substitution
   ****************************************************************/

  /**
   * Perform type symbol substitution inside the monotype.
   *
   * Does not need to create a new object, but must not
   * imperatively modify the monotype.
   *
   * @param map a map from TypeSymbols to TypeSymbols
   * @return a monotype with substitution performed
   */
  abstract Monotype substitute(Map map);
  
  final static Monotype[] substitute(java.util.Map map, Monotype[] ms)
  {
    if(ms==null) return null;
    
    Monotype[] res = new Monotype[ms.length];
    for(int i=ms.length-1; i>=0; i--)
      res[i] = ms[i].substitute(map);
    return res;
  }

  /****************************************************************
   * Functional types
   ****************************************************************/

  /** the list of input Monotypes if this type is functional */
  public Monotype[] domain()
  {
    return null;
  }

  /** the return type if this type is functional */
  public Monotype codomain()
  {
    return null;
  }
}

