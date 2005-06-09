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

  public static void setMaybe(TypeConstructor tc)
  {
    maybe = tc;
  }

  public static void setSure(TypeConstructor tc)
  {
    sure = tc;
  }

  public int arity() { return 1; }

  private static TypeConstructor maybe, sure;

  public Monotype freshMonotype(boolean existential)
  {
    TypeConstructor tc = new TypeConstructor(instance);
    introduce(tc);

    MonotypeVar raw = new MonotypeVar(existential);
    Typing.introduce(raw);

    return new MonotypeConstructor(tc, new MonotypeVar[]{ raw });
  }

  /**
     @param base The type variable we create this fresh constructed
                 monotype for.
  */
  public Monotype persistentFreshMonotype(MonotypeVar base)
  {
    TypeConstructor tc = new TypeConstructor(instance);

    /* It's important to give the raw variable the same name as the base one,
       so that for the syntactic type <T> ... !T we don't end up printing !t9.
    */
    MonotypeVar raw = new MonotypeVar(base.getName());

    return new MonotypeConstructor(tc, new MonotypeVar[]{ raw });
  }
  
  static void introduce(TypeConstructor tc)
  {
    tc.getKind().register(tc);
    try {
      Typing.leq(tc, maybe);
      Typing.leq(sure, tc);
    } catch(TypingEx ex) {
      bossa.util.Internal.error("Nullness creation error");
    }
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
    Monotype m1 = (Monotype) e1;
    Monotype m2 = (Monotype) e2;

    if (m1.isUnknown())
      {
        m2.setUnknown(false, true);
        return;
      }

    if (m2.isUnknown())
      {
        m1.setUnknown(true, false);
        return;
      }

    MonotypeConstructor mc1 = mc(m1), mc2 = mc(m2);
    
    Engine.leq(mc1.getTC(), mc2.getTC());
    Engine.leq(mc1.getTP()[0], mc2.getTP()[0]);
  }
  
  private MonotypeConstructor mc(Monotype m)
  {
    try
      {
	return (MonotypeConstructor) m.equivalent();
      }
    catch(ClassCastException ex)
      {
	throw new InternalError
	  (m + " was expected to be a monotype constructor, " +
	   " it's a " + m.getClass());
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
