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
//$Modified: Thu Aug 31 18:17:47 2000 by Daniel Bonniot $

package mlsub.typing;

import mlsub.typing.lowlevel.Engine;
import java.util.ArrayList;

/**
   A constrained monotype.
   
   @author Daniel Bonniot
 */

public final class Polytype
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
    if (t1 == t2)
      return t1;

    MonotypeVar t = new MonotypeVar();
    
    Constraint c = Constraint.and(t, t1.constraint,t2.constraint,
				  new MonotypeLeqCst(t1.monotype,t),
				  new MonotypeLeqCst(t2.monotype,t));
    
    Polytype res = new Polytype(c,t);
    return res;
  }
  
  public static Polytype union(Polytype[] types)
  {
    if (types.length == 0) return null;

    MonotypeVar t = new MonotypeVar();
    
    Constraint c = new Constraint(new TypeSymbol[]{t}, null);
    for (int i = 0; i < types.length; i++)
      c = Constraint.and(c, types[i].constraint, new MonotypeLeqCst(types[i].monotype, t));
    
    Polytype res = new Polytype(c,t);
    return res;
  }
  
  /****************************************************************
   * Functional types
   ****************************************************************/

  public Monotype[] domain()
  {
    Monotype m = monotype.equivalent();
    if (!(m instanceof FunType))
      return null;
    
    return ((FunType) m).domain();
  }

  public Monotype codomain()
  {
    Monotype m = monotype.equivalent();
    if (!(m instanceof FunType))
      return null;
    
    return ((FunType) m).codomain();
  }

  /****************************************************************
   * Accessors
   ****************************************************************/

  /**
   * Returns the domain of a functional polytype.
   *
   * @return a 'tuple' Domain
   */
  public Domain getDomain()
  {
    Monotype[] domains = domain();

    if(domains == null)
      throw new InternalError("getDomain on non functional polytype "+this);
    
    return new Domain(constraint, new TupleType(domains));
  }
  
  /****************************************************************
   * Simplification
   ****************************************************************/

  public void simplify()
  {
    if (!Constraint.hasBinders(constraint))
      return;

    ArrayList binders = new ArrayList(), atoms = new ArrayList();

    Engine.startSimplify();
    try{
      Constraint.assert(constraint);
      Engine.satisfy();
      monotype.tag(Variance.COVARIANT);      
      Engine.simplify(binders, atoms);
    }
    catch(mlsub.typing.lowlevel.Unsatisfiable e){
      throw new InternalError("Simplifying ill-formed polytype");
    }
    catch(TypingEx e){
      throw new InternalError("Simplifying ill-formed polytype");
    }
    finally{
      Engine.stopSimplify();
    }

    monotype = monotype.canonify();

    int nbinders = binders.size(), natoms = atoms.size();
    constraint = Constraint.create
      (nbinders == 0 ? null 
       : (TypeSymbol[]) binders.toArray(new TypeSymbol[nbinders]),
       natoms   == 0 ? null 
       : (AtomicConstraint[]) atoms.toArray(new AtomicConstraint[natoms]));
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
