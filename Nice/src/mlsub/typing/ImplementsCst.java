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

// File    : ImplementsCst.java
// Created : Fri Aug 27 10:45:33 1999 by bonniot
//$Modified: Fri Jun 16 16:14:18 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * A type constructor implements an interface.
 * 
 * @author bonniot
 */

public final class ImplementsCst extends AtomicConstraint
{
  public ImplementsCst(TypeConstructor tc, Interface itf)
  {
    this.tc = tc;
    this.itf = itf;
  }

  /**
   * Perform type symbol substitution inside the constraint.
   *
   * Does not need to create a new object, but must not
   * imperatively modify the constraint.
   *
   * @param map a map from TypeSymbols to TypeSymbols
   * @return an atomic constraint with substitution performed
   */
  AtomicConstraint substitute(java.util.Map map)
  {
    Object ttc;
    ttc = map.get(tc);
    
    if (ttc == null)
      return this;
    
    return new ImplementsCst((TypeConstructor) ttc, itf);
  }

  /**
   * Enter the constraint into the typing context.
   */
  void assert() throws TypingEx
  {
    Typing.assertImp(tc, itf, false);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return tc+":"+itf;
  }

  private TypeConstructor tc;
  private Interface itf;

  public TypeConstructor tc() { return tc; }
  public Interface itf()      { return itf; }
}
