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

// File    : Engine.java
// Created : Tue Jul 27 15:34:53 1999 by bonniot
//$Modified: Thu Aug 26 18:25:11 1999 by bonniot $

package bossa.engine;

import java.util.*;

import bossa.util.*;

/**
 * Public interface to lowlevel constraint implication checker
 * 
 * All accesses are done through this Engine
 * @author bonniot
 */

public abstract class Engine
{
  /**
   * Enters a new typing context
   *
   */
  public static void enter()
  {
    if(dbg) Debug.println("Enter");
    floating.mark();
    frozenLeqs.mark();
    for(Iterator i=kinds.iterator();
	i.hasNext();)
      { 
	K k=(K)i.next();
	k.mark();
	k.rigidify();
      }
  }
  
  /**
   * Rigidify the current constraint
   * This means no further assumptions will be made on it
   *
   * @exception Unsatisfiable if the current context was not well kinded
   */
  public static void implies()
    throws Unsatisfiable
  {
    assertFrozens();

    if(dbg) Debug.println("Implies");

    for(Iterator i=kinds.iterator();
	i.hasNext();)
      { 
	K k=(K)i.next();
	k.satisfy();
	k.rigidify();
      }
  }
  
  /**
   * Returns to the state we had before the last 'enter'
   *
   * @exception Unsatisfiable if the constraint was not satisfiable
   */
  public static void leave()
    throws Unsatisfiable
  {
    assertFrozens();

    if(dbg) Debug.println("Leave");
      
    for(Iterator i=kinds.iterator();
	i.hasNext();)
      { 
	K k=(K)i.next();
	k.satisfy();
      }
    backtrack();
  }
  
  private static void backtrack()
  {
    for(Iterator i=kinds.iterator();
	i.hasNext();)
      { 
	K k=(K)i.next();
	k.backtrack();
      }
    floating.backtrack();
    for(Iterator i=floating.iterator();i.hasNext();)
      ((Element)i.next()).setKind(null);
    floating.endOfIteration();
    frozenLeqs.backtrack();
  }

  /**
   * Iterates leq on two collections of Element
   *
   * @param e1 the smaller elements
   * @param e2 the greater elements
   * @exception Unsatisfiable 
   */
  public static void leq(Collection e1, Collection e2) 
    throws Unsatisfiable
  {
    Internal.error(e1.size()!=e2.size(),"Bad size in Engine.leq(Collections)");
    
    Iterator i1=e1.iterator(),i2=e2.iterator();
    for(int i=e1.size();i>0;i--)
      leq((Element)i1.next(),(Element)i2.next());
  }
      
  /**
   * Asserts that elements have some ordering relation
   *
   * @param e1 a value of type 'Element'
   * @param e2 a value of type 'Element'
   * @exception Unsatisfiable if the constraint is not satisfiable.
   * However this fact may also be discovered later
   */
  public static void leq(Element e1, Element e2) 
    throws Unsatisfiable
  {
    Kind k1=e1.getKind(),k2=e2.getKind();
    
    if(k1!=null)
      if(k2!=null)
	{
	  if(k1.equals(k2))
	    k1.leq(e1,e2);
	  else
	    {
	      if(dbg) 
		Debug.println("Bad kinding discovered by Engine : "+
			      k1+" != "+k2+
			      " for elements "+e1+" and "+e2);
	      throw new LowlevelUnsatisfiable("Bad Kinding");
	    }
	}
      else
	{
	  setKind(e2,k1);
	  k1.leq(e1,e2);
	}
    else
      if(k2!=null)
	{
	  setKind(e1,k2);
	  k2.leq(e1,e2);
	}
      else // k1==null and k2==null
	{
	  if(!(floating.contains(e1)))
	    {
	      floating.add(e1);
	      Internal.warning("Engine: floating added 1 : "+e1);
	    }
	  if(!(floating.contains(e2)))
	    {
	      floating.add(e2);
	      Internal.warning("Engine: floating added 2 : "+e2);
	    }
	  if(dbg)
	    Debug.println("Freezing "+e1+" <: "+e2+
			  " ("+e1.getId()+" <: "+e2.getId()+")");
	  frozenLeqs.add(new Leq(e1,e2));
	}
  }
  
  /****************************************************************
   * New nodes
   ****************************************************************/
  
  /**
   * Prepare a new Element to be used
   */
  public static void register(Element e)
  {
    if(dbg) Debug.println("Registering "+e);
    
    if(e.getKind()!=null)
      e.getKind().register(e);
    else
      floating.add(e);
  }
  
  /****************************************************************
   *                          Private
   ****************************************************************/

  private static class Leq
  {
    Leq(Element e1,Element e2)
    {
      this.e1=e1;
      this.e2=e2;
    }
    public String toString()
    {
      return e1+" <: "+e2;
    }
    Element e1,e2;
  }

  private static final BackableList frozenLeqs=new BackableList();
  
  // TODO: This is for sure quite slow and ugly
  private static void setKind(Element element, Kind k)
    throws Unsatisfiable
  {
    Stack s=new Stack();

    s.push(element);
    while(!s.empty())
      {
	Element e=(Element)s.pop();
	
	if(e.getKind()!=null)
	  if(e.getKind()==k)
	    continue;
	  else
	    Internal.error("Bad kinding in setKind for "+e);
	
	e.setKind(k);
	k.register(e);
	
	floating.remove(e);

	//Propagates the kind to all comparable elements
	for(Iterator i=frozenLeqs.iterator();
	    i.hasNext();)
	  {
	    Leq leq=(Leq)i.next();
	    if(leq.e1==e)
	      if(leq.e2.getKind()==null)
		s.push(leq.e2);
	      else
		{
		  Internal.error(leq.e1.getKind()!=leq.e2.getKind(),
				 "Bad kinding in Engine.setKind 1");
		  i.remove();
		  k.leq(leq.e1,leq.e2);
		}
	    else if(leq.e2==e)
	      if(leq.e1.getKind()==null)
		s.push(leq.e1);
	      else
		{
		  Internal.error(leq.e1.getKind()!=leq.e2.getKind(),
				 "Bad kinding in Engine.setKind 2");
		  i.remove();
		  k.leq(leq.e1,leq.e2);
		}
	  }
	frozenLeqs.endOfIteration();
      }
  }
  
  /**
   * Enter all the 'floating' elements into the variablesKind
   * and add their frozen constraints
   *
   */
  private static void assertFrozens()
    throws Unsatisfiable
  {
    for(Iterator i=floating.iterator();
	i.hasNext();)
      {
	Element e=(Element) i.next();
	
	if(dbg) Debug.println("Registering variable "+e);
	
	e.setKind(variablesKind);
	variablesKind.register(e);
      }
    floating.endOfIteration();
    floating.clear();
    
    for(Iterator i=frozenLeqs.iterator();
	i.hasNext();)
      {
	Leq leq=(Leq)i.next();
	variablesKind.leq(leq.e1,leq.e2);
      }
    frozenLeqs.endOfIteration();
    frozenLeqs.clear();
  }
  
  /** The elements that have not yet been added to a Kind  */
  private static final BackableList floating=new BackableList();
  
  /** The constraint of type variables */
  private static final K variablesKind=new K(){
//      public String toString()
//      { 
//        return "Kind of type variables";
//      }
  };
  
  private static final ArrayList createKinds()
  {
    ArrayList res=new ArrayList(10);
    res.add(variablesKind);
    return res;
  }
  
  /** The list of Kinds */
  private static final ArrayList kinds=createKinds();
  
  /** Maps a Kind to its lowlevel constraint */
  private static final HashMap kindsMap=new HashMap();
  
  public static K getConstraint(Kind kind)
  {
    K res=(K) kindsMap.get(kind);
    if(res!=null)
      return res;

    if(dbg) Debug.println("Creating new Lowlevel constraint for "+kind);
    
    res=new K();
    res.mark();
    kinds.add(res);
    kindsMap.put(kind,res);
    return res;
  }
  
  private static class K extends K0
    implements Kind
  {
    public final void leq(Element e1, Element e2)
      throws Unsatisfiable
    {
      if(dbg) Debug.println(e1+" <: "+e2+" ("+e1.getId()+" <: "+e2.getId()+")");
      try{ 
	leq(e1.getId(),e2.getId()); 
      }
      catch(Unsatisfiable e){
	// We call backtrack here to go the good state
	// since the leave() method will not be called
	// (or the call to backtrack() in leave will not occur
	// if we are in leave now)
	Engine.backtrack();
	throw e;
      }
    }
    
    public final void register(Element e)
    {
      e.setId(extend());
      if(dbg) Debug.println(e+" has id "+e.getId());
    }
    
    protected void indexMerged(int src, int dest) {
      Internal.warning("Engine: Indexes merged");
    }
    protected void indexMoved(int src, int dest) {
      Internal.warning("Engine: Index moved");
    }
    protected void indexDiscarded(int index) {
      Internal.warning("Engine: Index discarded");
    }
  }

  static final boolean dbg = false;
}
