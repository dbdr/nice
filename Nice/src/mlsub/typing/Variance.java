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

// File    : Variance.java
// Created : Fri Jul 23 12:15:46 1999 by bonniot
//$Modified: Thu Sep 21 16:34:37 2000 by Daniel Bonniot $

package mlsub.typing;

import java.util.*;

import mlsub.typing.lowlevel.*;

/**
   Variance of a type constructor.
   
   There is one instance of this class per Variance,
   so pointer equality can be used on variances.
   
   @author Daniel Bonniot
 */
public final class Variance 
  implements AtomicKind /* Variance is the Kind of MonotypeConstructors */
{
  private Variance(int[] signs)
  {
    this.size = signs.length;
    this.signs = signs;
  }

  /** 
      Array specifying subtyping properties for each parameter:
      covariant, contravariant or invariant.
  */
  private int[] signs;

  public static final Variance make(int[] signs)
  {
    int encoding = encoding(signs);
    Variance res = get(encoding);
    
    if (res == null)
      return set(encoding, new Variance(signs));
    else
      return res;    
  }

  /**
     @return the variance of the rankth element
  */
  public int getVariance(int rank)
  {
    return signs[rank];
  }

  /** Compute a small integer that uniquely identifies a variance. */
  private static int encoding(int[] signs)
  {
    int res = 0;

    int base = 1;
    for (int i=0; i<signs.length; i++)
      {
	res = res + (signs[i] + 2) * base;
	base *= 4;
      }
    return res;
  }
  
  /****************************************************************
   * Repository of created variances
   ****************************************************************/

  private static final ArrayList variances = new ArrayList(64);

  private static Variance get(int index)
  {
    if (index >= variances.size())
      return null;
    return (Variance) variances.get(index);
  }
  
  private static Variance set(int index, Variance v)
  {
    // make the list grow
    while (index >= variances.size())
      variances.add(null);
    
    variances.set(index, v);
    return v;
  }
  
  /****************************************************************
   * Empty Variance
   ****************************************************************/

  public static Variance empty()
  {
    return empty;
  }
  
  private static Variance empty = Variance.make(new int[0]);
  
  public mlsub.typing.lowlevel.Engine.Constraint getConstraint()
  {
    return mlsub.typing.lowlevel.Engine.getConstraint(this);
  }

  public Monotype freshMonotype(boolean existential)
  {
    TypeConstructor tc = new TypeConstructor(this);
    Typing.introduce(tc);

    Monotype[] tp = MonotypeVar.news(this.size, existential);
    Typing.introduce(tp);

    return new MonotypeConstructor(tc, tp);
  }
  
  public void register(Element e)
  {
//      if(!(e instanceof MonotypeConstructor))
//        return;
    
//      MonotypeConstructor mc = (MonotypeConstructor) e;
//      TypeConstructor tc = mc.getTC();
//      Engine.Constraint k = Engine.getConstraint(this);
    
//      if(tc.getKind()!=k)
//        if(tc.getKind()==null)
//  	Engine.setKind(tc,k);
//        else
//  	throw new TypingEx("Bad kinding for "+e);
  }
  

  /****************************************************************
   * Interfaces
   ****************************************************************/

  private Vector interfaces = new Vector(10);
  
  int newInterface(Interface i)
  {
    int res = getConstraint().newInterface();
    if (res >= interfaces.size())
      interfaces.setSize(res + 1);
    interfaces.set(res, i);
    return res;
  }

  public Interface getInterface(int iid)
  {
    return (Interface) interfaces.get(iid);
  }
  
  void subInterface(int i1, int i2)
  {
    getConstraint().subInterface(i1,i2);
  }
  
  void initialImplements(int x, int iid)
  {
    getConstraint().initialImplements(x,iid);
  }
  
  void initialAbstracts(int x, int iid)
  {
    getConstraint().initialAbstracts(x,iid);
  }
  
  void indexImplements(int x, int iid) throws Unsatisfiable
  {
    getConstraint().indexImplements(x,iid);
  }
  
  public void leq(Element e1, Element e2, boolean initial)
    throws Unsatisfiable
  {
    if(initial) throw new InternalError("initial leq in Variance");
    leq(e1,e2);
  }

  public void leq(Element e1, Element e2)
    throws Unsatisfiable
  {
    MonotypeConstructor m1 = mc(e1), m2 = mc(e2);
    
    mlsub.typing.lowlevel.Engine.leq(m1.getTC(), m2.getTC());
    assertEq(m1.getTP(), m2.getTP());
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
  
  /**
   * Asserts the inequalities between type parameters 
   * belonging to this variance
   *
   * @param tp1 a least Collection of Monotypes
   * @param tp2 a greatest Collection of Monotypes
   */
  public void assertEq(Monotype[] tp1, Monotype[] tp2)
    throws mlsub.typing.lowlevel.Unsatisfiable
  {
    if (size==0)
      if (tp1==null && tp2==null)
	//OK
	return;
      else
	throw new InternalError
	  ("Incorrect sizes " + 
	   (tp1 == null ? "null" : Integer.toString(tp1.length)) + 
	   " and " + 
	   (tp2 == null ? "null" : Integer.toString(tp2.length)));
    
    if (tp1 == null || tp1.length != size)
      throw new BadSizeEx(size, tp1 == null ? 0 : tp1.length);
    if (tp2 == null || tp2.length != size)
      throw new BadSizeEx(size, tp2 == null ? 0 : tp2.length);

    for(int i=0; i<size; i++)
      switch(signs[i]){
      case COVARIANT:
	mlsub.typing.lowlevel.Engine.leq(tp1[i],tp2[i]);
	break;
      case CONTRAVARIANT:
	mlsub.typing.lowlevel.Engine.leq(tp2[i],tp1[i]);
	break;
      case INVARIANT:
	mlsub.typing.lowlevel.Engine.leq(tp1[i],tp2[i]);
	mlsub.typing.lowlevel.Engine.leq(tp2[i],tp1[i]);
	break;
      }
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<size; i++)
      {
	if (i>0) 
	  sb.append(", ");
	switch(signs[i]){
	case COVARIANT:
	  sb.append("+");
	  break;
	case CONTRAVARIANT:
	  sb.append("-");
	  break;
	case INVARIANT:
	  sb.append("=");
	  break;
	}
      }
    
    return "Variance (" + sb.toString() + ")";
  }
  
  private int size;

  public int arity() { return size; }

  /****************************************************************
   * Simplification
   ****************************************************************/

  static public final int
    COVARIANT     = +1,
    CONTRAVARIANT = -1,
    INVARIANT     =  0;

  void tag(Monotype[] monotypes, int variance)
  {
    if (monotypes == null) return;
    
    for (int i = 0; i<monotypes.length; i++)
      monotypes[i].tag(variance * signs[i]);
  }  
}

