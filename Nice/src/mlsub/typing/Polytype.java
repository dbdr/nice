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
import java.util.Map;

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

  public final boolean isMonomorphic()
  {
    return ! Constraint.hasBinders(constraint);
  }

  public Polytype cloneType()
  {
    //Optimization
    if (isMonomorphic())
      return this;

    Map map = new java.util.HashMap();

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
      constraint.enter();
    }
    finally{
      Typing.leave();
    }
  }
  
    
  /****************************************************************
   * Computations on types
   ****************************************************************/

  public static Polytype apply(Polytype funt, Polytype[] parameters)
  {
    return apply(funt.constraint, (FunType) funt.monotype, parameters);
  }

  public static Polytype apply(Constraint cst, FunType type, 
			       Polytype[] parameters)
  {
    Monotype codom = type.codomain();
    /*
      Optimization:
      If we know codom is a constant,
      the constraint parameters<dom is useless.
    */
    if(codom.isRigid())
      return new Polytype(Constraint.True, codom);

    Monotype[] dom = type.domain();

    cst = Constraint.and
      (Polytype.getConstraint(parameters),
       cst,
       MonotypeLeqCst.constraint(Polytype.getMonotype(parameters), dom));
    
    Polytype res = new Polytype(cst, codom);
    res.simplified = false;
    return res;
  }

  public static Polytype union(Polytype t1, Polytype t2)
  {
    if (t1 == t2)
      return t1;

    MonotypeVar t = new MonotypeVar();
    
    Constraint c = Constraint.and(t, t1.constraint,t2.constraint,
				  new MonotypeLeqCst(t1.monotype,t),
				  new MonotypeLeqCst(t2.monotype,t));
    
    Polytype res = new Polytype(c,t);
    res.simplified = false;
    return res;
  }
  
  public static Polytype union(Polytype[] types)
  {
    if (types.length == 0) return bottom();

    /* Even if there is only one type, we quantify over all super-types.
       This is needed when a non-variant type constructor like []
       is added around the returned type. Probably this means a wrong
       spec for this union function. 
    */

    MonotypeVar t = new MonotypeVar();
    
    Constraint c = new Constraint(new TypeSymbol[]{t}, null);
    for (int i = 0; i < types.length; i++)
      c = Constraint.and(c, types[i].constraint, new MonotypeLeqCst(types[i].monotype, t));
    
    Polytype res = new Polytype(c,t);
    res.simplified = false;
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

  /*
    Whether this polytype is in simplified form.
    The default value is true, so it shoud be set to false
    for any polytype constructed in a way that does not guaranty it to
    be simplified.
  */
  private boolean simplified = true;
  
  public void setNotSimplified()
  {
    simplified = false;
  }

  public void simplify()
  {
    if (!Constraint.hasBinders(constraint) || simplified || Polytype.noSimplify)
      return;

    ArrayList binders = new ArrayList(), atoms = new ArrayList();

    Engine.startSimplify();
    try{
      Constraint.enter(constraint);
      Engine.satisfy();
      monotype.tag(Variance.COVARIANT);      
      Engine.simplify(binders, atoms);
    }
    catch(mlsub.typing.lowlevel.Unsatisfiable e){
      // Avoid looping.
      simplified = true;
      throw new InternalError("Simplifying ill-formed polytype: " + this);
    }
    catch(TypingEx e){
      // Avoid looping.
      simplified = true;
      throw new InternalError("Simplifying ill-formed polytype: " + this);
    }
    finally{
      Engine.stopSimplify();
    }

    int nbinders = binders.size(), natoms = atoms.size();

    if (nbinders >= constraint.binders().length)
      {
        // The "simplified" version is longer than the original, so we
        // keep the original.
        simplified = true;
        return;
      }

    monotype = monotype.canonify();

    constraint = Constraint.create
      (nbinders == 0 ? null 
       : (TypeSymbol[]) binders.toArray(new TypeSymbol[nbinders]),
       natoms   == 0 ? null 
       : (AtomicConstraint[]) atoms.toArray(new AtomicConstraint[natoms]));

    simplified = true;
  }
  
  /** Try to simplify this type.
      @return false if the type is ill-formed. 
  */
  public boolean trySimplify()
  {
    try {
      simplify();
      return true;
    } catch(InternalError e) {
      return false;
    }
  }

  /****************************************************************
   * Misc
   ****************************************************************/
  
  public String toString()
  {
    // We want a simple form for printing
    try {
      simplify();
    } catch(InternalError e) {
      return Constraint.toString(constraint) + monotype.toString() +
      " (Ill-formed type)";
    }

    return Constraint.toString(constraint) + String.valueOf(monotype);
  }

  public String toStringNoSimplify()
  {
    return Constraint.toString(constraint) + String.valueOf(monotype);
  }

  private Constraint constraint;
  private Monotype monotype;

  public static boolean noSimplify;
}
