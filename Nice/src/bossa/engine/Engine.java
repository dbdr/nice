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
//$Modified: Thu Jul 29 00:18:17 1999 by bonniot $

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
  public static void enter()
  {
    if(dbg) Debug.println("Enter");
    
    for(Iterator i=kinds.iterator();
	i.hasNext();)
      ((K)i.next()).mark();
  }
  
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
	k.backtrack();
      }
  }

  private static void leq(Kind k, Element e1, Element e2) 
    throws Unsatisfiable
  {
    if(k.isBase())
      {
	if(dbg) Debug.println(k+"  "+e1+":"+e1.getId()+" <= "+e2+":"+e2.getId());

	getConstraint(k).leq(e1.getId(),e2.getId());
      }
    else
      k.leq(e1,e2);
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
      
  public static void leq(Element e1, Element e2) 
    throws Unsatisfiable
  {
    Kind k1=e1.getKind(),k2=e2.getKind();
    
    if(k1!=null)
      if(k2!=null)
	{
	  if(k1.equals(k2))
	    leq(k1,e1,e2);
	  else
	    {
	      Debug.println("Bad kinding discovered by Engine : "+k1+" != "+k2);
	      throw new LowlevelUnsatisfiable("Bad Kinding");
	    }
	}
      else
	{
	  setKind(e2,k1);
	  leq(k1,e1,e2);
	}
    else
      if(k2!=null)
	{
	  setKind(e1,k2);
	  leq(k2,e1,e2);
	}
      else
	{
	  if(!(floating.contains(e1)))
	    floating.add(e1);
	  if(!(floating.contains(e2)))
	    floating.add(e2);
	  
	  frozenLeqs.add(new Leq(e1,e2));
	}
  }
  
//    public static void leq(Element e1, Element e2) 
//      throws Unsatisfiable
//    {
    
//    }
  
  /****************************************************************
   * New nodes
   ****************************************************************/
  
  public static void register(Element e)
  {
    if(dbg) Debug.println("Registering "+e);
    
    if(e.getKind()!=null)
      if(e.getKind().isBase())
	e.setId(getConstraint(e.getKind()).extend());
      else
	Internal.error("Don't know what to do in Engine.register");
    else
      floating.add(e);
  }
  
  /****************************************************************
   *                          Private
   ****************************************************************/

  static class Leq
  {
    Leq(Element e1,Element e2)
    {
      this.e1=e1;
      this.e2=e2;
    }
    Element e1,e2;
  }

  static ArrayList frozenLeqs=new ArrayList();
  
  private static void setKind(Element e, Kind k)
    throws Unsatisfiable
  {
    // Usefull since this is recursive
    if(e.getKind()!=null)
      return;
    
    e.setKind(k);
    if(k.isBase())
      register(e);
    
    floating.remove(e);
    //Propagates the kind to all comparable elements

    //TODO: modifiy frozenLeqs in place
    final ArrayList newFrozenLeqs=new ArrayList(frozenLeqs.size());
    
    for(Iterator i=frozenLeqs.iterator();
	i.hasNext();)
      {
	Leq leq=(Leq)i.next();
	if(leq.e1==e)
	  {
	    setKind(leq.e2,k);
	    k.leq(leq.e1,leq.e2);
	  }
	else if(leq.e2==e)
	  {
	    setKind(leq.e1,k);
	    k.leq(leq.e1,leq.e2);
	  }
	else
	  newFrozenLeqs.add(leq);
      }
    frozenLeqs=newFrozenLeqs;
  }
  
  static void assertFrozens()
    throws Unsatisfiable
  {
    for(Iterator i=floating.iterator();
	i.hasNext();)
      {
	Element e=(Element) i.next();
	
	if(dbg) Debug.println("Registering variable "+e);
	
	e.setKind(variablesKind);
	e.setId(variablesK.extend());
      }
    floating.clear();
    	
    for(Iterator i=frozenLeqs.iterator();
	i.hasNext();)
      {
	Leq leq=(Leq)i.next();
	variablesKind.leq(leq.e1,leq.e2);
      }
    frozenLeqs=new ArrayList();
  }
  
  static final ArrayList floating=new ArrayList(); // elements whose kind is not known yet
  
  static final K variablesK=new K();
  static final Kind variablesKind = 
    new Kind()
  {
    public void leq(Element e1, Element e2)
      throws Unsatisfiable
    {
      if(dbg) Debug.println("[variables] "+e1+":"+e1.getId()+" <= "+e2+":"+e2.getId());
      variablesK.leq(e1.getId(),e2.getId());
    }

    public boolean isBase() { return true; }
    
  };
  
  static final HashMap createKindsMap()
  {
    HashMap res=new HashMap();
    res.put(variablesKind,variablesK);
    return res;
  }
  
  static final HashMap kindsMap=createKindsMap();
  
  static final ArrayList createKinds()
  {
    ArrayList res=new ArrayList(10);
    res.add(variablesK);
    return res;
  }
  
  static final ArrayList kinds=createKinds();
  
  static K getConstraint(Kind kind)
  {
    K res=(K) kindsMap.get(kind);
    if(res!=null)
      return res;
    res=new K();
    res.mark();
    kinds.add(res);
    kindsMap.put(kind,res);
    return res;
  }
  
  private static class K extends K0
  {
    protected void indexMerged(int src, int dest) {
      if(dbg) Debug.println(this+" merged "+src+" and "+dest);
    }
    protected void indexMoved(int src, int dest) {
      
    }
    protected void indexDiscarded(int index) {
      
    }
  }

  static boolean dbg = false;
}
