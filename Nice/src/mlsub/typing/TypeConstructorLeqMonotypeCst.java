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
 * Constraint a type constructor to be lower than a monotype's head.
 *
 * The constraint is posed on the raw monotype, after the nullness marker.
 * 
 * @author Daniel Bonniot
 */

public final class TypeConstructorLeqMonotypeCst extends AtomicConstraint
{
  public TypeConstructorLeqMonotypeCst(TypeConstructor t1, Monotype t2)
  {
    this.t1 = t1;
    this.t2 = t2;
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
    
    return new TypeConstructorLeqMonotypeCst
      ((TypeConstructor) tt1, (Monotype) tt2);
  }

  public void assert()
  throws TypingEx
  {
    // Use the raw type of t2, after the nullness marker.
    Typing.leq(t1, ((MonotypeConstructor) t2.equivalent()).getTP()[0]);
  }
  
  public String toString()
  {
    return t1 + " < " + t2;
  }

  private TypeConstructor t1;
  private Monotype t2;

  public TypeConstructor t1() { return t1; }
  public Monotype t2() { return t2; }
}
