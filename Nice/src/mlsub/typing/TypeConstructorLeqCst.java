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

// File    : TypeConstructorLeqCst.java
// Created : Fri Jun 02 17:10:52 2000 by Daniel Bonniot
//$Modified: Fri Jun 16 16:13:22 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * Constraint on type constructors.
 * 
 * @author Daniel Bonniot
 */

public final class TypeConstructorLeqCst extends AtomicConstraint
{
  public TypeConstructorLeqCst(TypeConstructor t1, TypeConstructor t2)
  {
    this.t1 = t1;
    this.t2 = t2;

    identifyVariances(t1, t2);
  }

  /**
   * Assert that t1 and t2 have the same variance.
   *
   * This is not necessary, as it would be discovered later 
   * in the constraint solver.
   * But it seems good to discover it sooner, 
   * for efficiency reasons
   * (and for error reporting if we added a check).
   */
  private static void identifyVariances(TypeConstructor t1, TypeConstructor t2)
  {
    if(t1.variance==null && t2.variance!=null)
      t1.setVariance(t2.variance);
    else if(t2.variance==null && t1.variance!=null)
      t2.setVariance(t1.variance);
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
    Object tt1, tt2;
    tt1 = map.get(t1);
    tt2 = map.get(t2);
    
    if(tt1==null && tt2==null)
      return this;
    
    if(tt1==null)
      tt1 = t1;
    else if(tt2==null)
      tt2 = t2;
    
    return new TypeConstructorLeqCst
      ((TypeConstructor) tt1, (TypeConstructor) tt2);
  }

  public void enter()
  throws TypingEx
  {
    Typing.leq(t1, t2);
  }
  
  public String toString()
  {
    return t1+" < "+t2;
  }

  private TypeConstructor t1;
  private TypeConstructor t2;

  public TypeConstructor t1() { return t1; }
  public TypeConstructor t2() { return t2; }
}
