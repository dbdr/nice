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

import mlsub.typing.lowlevel.*;

/**
   The kind of types qualified by their nullness.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class NullnessKind implements AtomicKind 
{
  public static NullnessKind instance = new NullnessKind();

  public static void initialize(TypeConstructor maybeTC)
  {
    maybe = maybeTC;
  }

  public int arity() { return 1; }

  private static TypeConstructor maybe;

  public Monotype freshMonotype()
  {
    TypeConstructor tc = new TypeConstructor(instance);
    Typing.introduce(tc);
    try {
      Typing.leq(tc, maybe);
    } catch(TypingEx ex) {
      bossa.util.Internal.error("Nullness creation error");
    }

    Monotype raw = new MonotypeVar();
    Typing.introduce(raw);

    return new MonotypeConstructor(tc, new Monotype[]{ raw });
  }
  
  public void register(Element e)
  {
  }
  

  public void leq(Element e1, Element e2, boolean initial)
    throws Unsatisfiable
  {
    if(initial) throw new InternalError("initial leq in Nullness");
    leq(e1,e2);
  }

  public void leq(Element e1, Element e2)
    throws Unsatisfiable
  {
    MonotypeConstructor m1 = mc(e1), m2 = mc(e2);
    
    Engine.leq(m1.getTC(), m2.getTC());
    Engine.leq(m1.getTP()[0], m2.getTP()[0]);
  }
  
  private MonotypeConstructor mc(Element e)
  {
    try
      {
	return (MonotypeConstructor) ((Monotype) e).equivalent();
      }
    catch(ClassCastException ex)
      {
	throw new InternalError
	  (e + " was expected to be a monotype constructor, " +
	   " it's a " + e.getClass());
      }
  }
  
  public mlsub.typing.lowlevel.Engine.Constraint getConstraint()
  {
    return mlsub.typing.lowlevel.Engine.getConstraint(this);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "Nullness kind";
  }
}
