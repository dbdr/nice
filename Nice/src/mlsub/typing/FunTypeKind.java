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

// File    : FunTypeKind.java
// Created : Wed Jul 28 17:51:02 1999 by bonniot
//$Modified: Wed Aug 30 16:16:34 2000 by Daniel Bonniot $

package mlsub.typing;

import mlsub.typing.lowlevel.*;

/**
 * The Arrow kind.
 * 
 * @author bonniot
 */

public class FunTypeKind implements Kind
{
  public static FunTypeKind get(int domainArity)
  {
    if (funtypeKinds[domainArity] == null)
      funtypeKinds[domainArity] = new FunTypeKind(domainArity);
    return funtypeKinds[domainArity];
  }
  
  private static FunTypeKind[] funtypeKinds;
  public static void reset() { funtypeKinds = new FunTypeKind[40]; }
  
  private FunTypeKind(int domainArity)
  {
    this.domainArity = domainArity;
    // forces the creation of the constraint
    // we don't want it to be created during link.
    Engine.getConstraint(this);
  }

  public Monotype freshMonotype()
  {
    Monotype codomain = new MonotypeVar();
    Typing.introduce(codomain);
    
    Monotype[] domain = MonotypeVar.news(domainArity);
    Typing.introduce(domain);
    
    return new FunType(this, domain, codomain);
  }
  
  public void register(Element e)
  {
  }
  
  public void leq(Element e1, Element e2, boolean initial)
    throws Unsatisfiable
  {
    if(initial)
      throw new InternalError("initial leq in FunTypeKind");
    leq(e1,e2);
  }
  
  public void leq(Element e1, Element e2)
    throws Unsatisfiable
  {
    FunType t1= ft(e1), t2= ft(e2);
    
    Engine.leq(t2.domain(), t1.domain());
    Engine.leq(t1.codomain(), t2.codomain());
  }
  
  private FunType ft(Element e)
  {
    try
      {
	return (FunType) ((Monotype) e).equivalent();
      }
    catch(ClassCastException ex)
      {
	throw new InternalError
	  (e + " was expected to be a functional type, " +
	   " it's a " + e.getClass());
      }
  }
 
  public String toString()
  {
    return "Fun(" + domainArity + ")";
  }
  
  public int domainArity;
  
}
