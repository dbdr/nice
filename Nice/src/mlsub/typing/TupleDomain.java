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

// File    : TupleDomain.java
// Created : Fri Jun 02 18:31:04 2000 by Daniel Bonniot
//$Modified: Fri Jun 16 16:14:42 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * A domain which monotype is a tuple.
 * 
 * @author Daniel Bonniot
 */

public final class TupleDomain extends Domain
{
  public TupleDomain(Constraint cst, Monotype[] tuple)
  {
    super(cst, null);
    this.tuple = tuple;
  }

  Monotype[] getTuple()
  {
    return tuple;
  }
  
  private Monotype[] tuple;
}
