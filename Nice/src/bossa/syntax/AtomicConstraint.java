/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
   A constraint atom. See childs
   
   @see Constraint

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
public abstract class AtomicConstraint extends Node
{
  AtomicConstraint()
  {
    super(Node.down);
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/
  
  abstract mlsub.typing.AtomicConstraint resolve(TypeScope scope);
  
  static mlsub.typing.AtomicConstraint[] resolve(TypeScope scope, Collection c)
  {
    if (c.size() == 0)
      return null;
    
    mlsub.typing.AtomicConstraint[] res = 
      new mlsub.typing.AtomicConstraint[c.size()];

    int n = 0;
    for (Iterator i = c.iterator(); i.hasNext(); )
      res[n++] = ((AtomicConstraint) i.next()).resolve(scope);

    return res;
  }

  /****************************************************************
   * Misc
   ****************************************************************/

  /**
   * Returns a string that represents a constraint element
   * another element was introduced in comparison of.
   *
   * For instance, in <Num N | ... >
   * getParentFor(N) should be "Num".
   * Used to reproduce a parsable form of the constraint.
   *
   * @param tc a constraint element
   * @return the representation of its "parent", or <code>null</code>
   */
  String getParentFor(mlsub.typing.TypeConstructor tc)
  {
    return null;
  }

  public static AtomicConstraint sureTypeVar(mlsub.typing.MonotypeVar tv)
  {
    return AtomicConstraint.create
      (new mlsub.typing.MonotypeLeqTcCst(tv, PrimitiveType.sureTC));
  }

  boolean isSureConstraintFor(mlsub.typing.MonotypeVar mv)
  {
    return false;
  }

  /****************************************************************
   * Wrapper for lowlevel AtomicConstraint
   ****************************************************************/

  static final AtomicConstraint create(mlsub.typing.AtomicConstraint atom)
  {
    return new Wrapper(atom);
  }
  
  private static final class Wrapper extends AtomicConstraint
  {
    Wrapper(mlsub.typing.AtomicConstraint atom)
    {
      this.atom = atom;
    }
    
    private final mlsub.typing.AtomicConstraint atom;
    
    mlsub.typing.AtomicConstraint resolve(TypeScope scope)
    {
      return atom;
    }

    String getParentFor(mlsub.typing.TypeConstructor tc)
    {
      if(atom instanceof mlsub.typing.TypeConstructorLeqCst)
	{
	  mlsub.typing.TypeConstructorLeqCst leq = 
	    (mlsub.typing.TypeConstructorLeqCst) atom;
	  if(leq.t1()==tc)
	    return leq.t2().toString();
	  else
	    return null;
	}
      if(atom instanceof mlsub.typing.ImplementsCst)
	{
	  mlsub.typing.ImplementsCst leq = 
	    (mlsub.typing.ImplementsCst) atom;
	  if(leq.tc()==tc)
	    return leq.itf().toString();
	  else
	    return null;
	}
      
      return null;
    }

    boolean isSureConstraintFor(mlsub.typing.MonotypeVar mv)
    {
      return atom instanceof mlsub.typing.MonotypeLeqTcCst &&
	((mlsub.typing.MonotypeLeqTcCst) atom).m == mv;
    }

    public String toString()
    {
      return atom.toString();
    }
  }
}
