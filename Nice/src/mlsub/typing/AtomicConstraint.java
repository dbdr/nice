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

// File    : AtomicConstraint.java
// Created : Fri Jun 02 17:02:45 2000 by Daniel Bonniot
//$Modified: Fri Jun 09 15:17:59 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * Atomic constraint.
 * 
 * @author Daniel Bonniot
 */

public abstract class AtomicConstraint
{
  abstract void assert() throws TypingEx;

  /**
   * Perform type symbol substitution inside the constraint.
   *
   * Does not need to create a new object, but must not
   * imperatively modify the constraint.
   *
   * @param map a map from TypeSymbols to TypeSymbols
   * @return an atomic constraint with substitution performed
   */
  abstract AtomicConstraint substitute(java.util.Map map);

  final static AtomicConstraint[] substitute(java.util.Map map, 
					     AtomicConstraint[] atoms)
  {
    if(atoms==null)
      return null;
    
    AtomicConstraint[] res = new AtomicConstraint[atoms.length];
    for(int i=atoms.length-1; i>=0; i--)
      res[i] = atoms[i].substitute(map);
    return res;
  }
}
