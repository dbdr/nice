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
   A monotype's head is less than a type constructor.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class MonotypeLeqTcCst extends AtomicConstraint
{
  public MonotypeLeqTcCst(Monotype m, TypeConstructor tc)
  {
    this.m = m;
    this.tc = tc;
  }
  
  private final Monotype m;
  private final TypeConstructor tc;

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
    Monotype new1 = m.substitute(map);
    TypeConstructor new2 = (TypeConstructor) map.get(tc);
    if (new2 == null)
      new2 = tc;

    if (new1 == m && new2 == tc)
      return this;
    
    return new MonotypeLeqTcCst(new1, new2);
  }

  void enter() throws TypingEx
  {
    Typing.leq(m, tc);
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return m + " < " + tc;
  }
}
