/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

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
    identifyVariances();
  }

  /**
     Assert that the type constructor and the interface have the same variance.
     
     This is not necessary, as it would be discovered later in the constraint 
     solver. However it is good to discover it sooner, for efficiency reasons
     (less inequalities get frozen) and for earlier error reporting.
  */
  private void identifyVariances()
  {
    if (tc.variance == null && itf.variance != null)
      tc.setVariance(itf.variance);
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
