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

// File    : Domain.java
// Created : Fri Jun 02 16:59:06 2000 by Daniel Bonniot
//$Modified: Fri Jun 09 17:26:40 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * A constrained monotype.
 * 
 * @author Daniel Bonniot
 */

public class Polytype
{
  public Polytype(Constraint constraint,
		  Monotype monotype)
  {
    this.constraint = constraint;
    this.monotype = monotype;
  }

  public Polytype(Monotype monotype)
  {
    this.constraint = null;
    this.monotype = monotype;
  }

  public Polytype cloneType()
  {
    //Optimization
    if(!Constraint.hasBinders(constraint))
      return this;

    java.util.Map map = new java.util.HashMap();
    TypeSymbol[] binders = constraint.binders();
    TypeSymbol[] newBinders = new TypeSymbol[binders.length];
    
    for(int i = 0; i<binders.length; i++)
      {
	newBinders[i] = binders[i].cloneTypeSymbol();
	map.put(binders[i], newBinders[i]);
      }

    return new Polytype
      (new Constraint(newBinders, 
		      AtomicConstraint.substitute(map, constraint.atoms())), 
       monotype.substitute(map));
  }
  
  public Constraint getConstraint()
  {
    return constraint;
  }
  
  public static Constraint[] getConstraint(Polytype[] p)
  {
    Constraint[] res = new Constraint[p.length];
    
    for(int i=0; i<p.length; i++)
      res[i] = p[i].getConstraint();
    
    return res;
  }

  public Monotype getMonotype()
  {
    return monotype;
  }

  public static Monotype[] getMonotype(Polytype[] p)
  {
    Monotype[] res = new Monotype[p.length];
    
    for(int i=0; i<p.length; i++)
      res[i] = p[i].getMonotype();
    
    return res;
  }

  public static final Polytype bottom()
  {
    MonotypeVar alpha = new MonotypeVar();
    return new Polytype(new Constraint(new TypeSymbol[]{alpha}, null), alpha);
  }
				       
  
  /****************************************************************
   * Typechecking
   ****************************************************************/

  public void checkWellFormedness()
  throws TypingEx
  {
    // Optimization
    if(!Constraint.hasBinders(constraint))
      return;
    
    Typing.enter();
    
    try{
      // Explanation for the assert(false) statement:
      // We just want to check the type is well formed,
      // so there is not need to enter top implementations.
      // This is just an optimization, this shouldn't
      // change anything.
      constraint.assert(false);
    }
    finally{
      Typing.leave();
    }
  }
  
    
  /****************************************************************
   * Computations on types
   ****************************************************************/

  public static Polytype union(Polytype t1, Polytype t2)
  {
    MonotypeVar t = new MonotypeVar();
    
    Constraint c = Constraint.and(t, t1.constraint,t2.constraint,
				  new MonotypeLeqCst(t1.monotype,t),
				  new MonotypeLeqCst(t2.monotype,t));
    
    Polytype res = new Polytype(c,t);
    return res;
  }
  
  /****************************************************************
   * Functional types
   ****************************************************************/

  public Monotype[] domain()
  {
    return monotype.domain();
  }

  public Monotype codomain()
  {
    return monotype.codomain();
  }

  /****************************************************************
   * Accessors
   ****************************************************************/

  /**
   * Returns the domain of a functional polytype.
   *
   * @return a 'tuple' Domain
   */
  public TupleDomain getDomain()
  {
    Monotype[] domains = monotype.domain();

    if(domains==null)
      throw new InternalError("getDomain on non functional polytype "+this);
    
    return new TupleDomain(constraint,domains);
  }
  
  /****************************************************************
   * Misc
   ****************************************************************/
  
  public String toString()
  {
    return Constraint.toString(constraint) + monotype.toString();
  }

  private Constraint constraint;
  private Monotype monotype;  
}
