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

// File    : Typing.java
// Created : Tue Jul 20 11:57:17 1999 by bonniot
//$Modified: Thu Jul 29 16:20:45 1999 by bonniot $

package bossa.typing;

import java.util.*;

import bossa.util.*;
import bossa.syntax.*;

import bossa.engine.Engine;
import bossa.engine.Element;
import bossa.engine.Unsatisfiable;

/**
 * Static class for comparing types
 */
abstract public class Typing
{
  /****************************************************************
   * Typing contexts
   ****************************************************************/

  /**
   * Enters a new typing context
   *
   */
  public static void enter()
  {
    enter(null);
  }

  /**
   * Enters a new typing context
   *
   * @param message A debug message to know where we are
   */
  public static void enter(String message)
  {
    if(message!=null && dbg) Debug.println("## Typechecking "+message);
    Engine.enter();
  }

  /**
   * Enters a new typing context
   *
   * @param symbols a collection of TypeSymbols,
   *   which represent the new symbols of the context
   * @param context the constraint to ass to the current context
   */
  public static void enter(Collection symbols, String message)
  {
    if(dbg) Debug.println("## Typechecking "+message+
		  Util.map(" [",", ","]",symbols)
		  );
    Engine.enter();
    
    introduce(symbols);
  }

  static public void introduce(Element e)
  {
    //TODO: this is DIRTY !
    if(e instanceof MonotypeVar)
      e.setKind(null);
    
    Engine.register(e);
  }
  
  static public void introduce(Collection elements)
  {
    Iterator i=elements.iterator();
    while(i.hasNext())
      {
	Object o=i.next();
	if(o instanceof Element)
	  introduce((Element) o);
	else
	  Internal.warning("introducing "+o.getClass());
      }
  }
  
  /**
   * Leaves the last typing context
   *
   */
  public static void leave()
    throws TypingEx
  {
    try{
      Engine.leave();
    }
    catch(Unsatisfiable e){
      throw new TypingEx("Unsatisfiable 1:"+e.getMessage());
    }
    
  }

  public static void implies()
    throws TypingEx
  {
    try{
      Engine.implies();
    }
    catch(Unsatisfiable e){
      throw new TypingEx("Not satisfiable "+e.getMessage());
    }
    
  }
  
  /****************************************************************
   * Assertions
   ****************************************************************/

  public static void leq(Collection c1, Collection c2)
    throws TypingEx
  {
    Internal.error(c1.size()!=c2.size(),"Unequal sizes in assertLeq");
    Iterator i1=c1.iterator();
    Iterator i2=c2.iterator();
    
    while(i1.hasNext())
      {
	leq((Type)i1.next(),
	    (Type)i2.next());
      }
  }
  
  public static void leqMono(Collection c1, Collection c2)
    throws TypingEx
  {
    Internal.error(c1.size()!=c2.size(),"Unequal sizes in leqMono");
    Iterator i1=c1.iterator();
    Iterator i2=c2.iterator();
    
    while(i1.hasNext())
      {
	leq((Monotype)i1.next(),
	    (Monotype)i2.next());
      }
  }
  
  public static void leq(TypeConstructor t, Collection c)
    throws TypingEx
  {
    Iterator i=c.iterator();
    
    while(i.hasNext())
      {
	leq(t,(TypeConstructor)i.next());
      }
  }
  
  /****************************************************************
   * Testing Polytype <= Polytype
   ****************************************************************/

  public static void leq(Type t1, Type t2) 
    throws TypingEx
  {
    if(t1.getClass()
       !=t2.getClass())
      throw new KindingEx(t1,t2);
    if(t1 instanceof Polytype)
      leq((Polytype) t1,(Polytype) t2);
    else
      leq((PolytypeConstructor) t1, (PolytypeConstructor) t2);
  }

  public static void leq(Polytype t1, Polytype t2) 
    throws TypingEx
  {
    if(dbg) Debug.println("Polytype leq: "+t1+" <: "+t2);
    
    if(dbg)
      enter("#");
    else
      enter();
    
    t2.constraint.assert();

    if(dbg) Debug.println("IMPLIES");
    implies();
    
    t1.constraint.assert();
    leq(t1.monotype,t2.monotype);

    if(dbg) Debug.println("LEAVE");
    leave();
  }

  public static void leq(PolytypeConstructor t1, PolytypeConstructor t2) 
    throws TypingEx
  {
    if(dbg) Debug.println(t1+" <: "+t2);
    Collection tp1=t1.getTypeParameters();
    Collection tp2=t2.getTypeParameters();
    
    enter();
    leqMono(tp1,tp2);
    leqMono(tp2,tp1);
    implies();
    leq(t1.polytype,t2.polytype);
    leave();
  }

  /****************************************************************
   * Monotypes
   ****************************************************************/

  public static void leq(Monotype m1, Monotype m2)
    throws TypingEx
  {
    try{
      Engine.leq(m1,m2);
    }
    catch(Unsatisfiable e){
      if(dbg) e.printStackTrace();
      throw new TypingEx(e.getMessage());
    }
    
    
//      Collection dom1,dom2;
    
//      if((dom1=m1.domain())!=null)
//        {
//  	m2=m2.functionalCast(dom1.size());
//  	dom2=m2.domain();
//  	if(dom2==null)
//  	  throw new KindingEx(m1,m2);
//  	leqMono(dom2,dom1);
//  	leq(m1.codomain(),m2.codomain());
//        }
//      else
//      if((dom2=m2.domain())!=null)
//        {
//  	m1=m1.functionalCast(dom2.size());
//  	if((dom1=m1.domain())==null)
//  	  throw new KindingEx(m1,m2);
//  	leqMono(dom2,dom1);
//  	leq(m1.codomain(),m2.codomain());
//        }
//      else
//        {
//  	if(m1 instanceof MonotypeVar && m2 instanceof MonotypeVar)
//  	  Debug.println(m1+" <: "+m2);
//  	else
//  	  {
//  	    Variance v=null;
//  	    if(m1 instanceof MonotypeConstructor)
//  	      v=((MonotypeConstructor)m1).tc.variance;
//  	    else if(m2 instanceof MonotypeConstructor)
//  	      v=((MonotypeConstructor)m2).tc.variance;
//  	    else
//  	      Internal.error("No TypeConstructor in Typing.leq");
	    
//  	    TypeConstructor tc1=m1.decomposeTC(v);
//  	    TypeConstructor tc2=m2.decomposeTC(v);
//  	    leq(tc1,tc2);
//  	    try{
//  	      v.assertLeq(m1.decomposeTP(v),m2.decomposeTP(v));
//  	    }
//  	    catch(BadSizeEx e){
//  	      throw new TypingEx(m1+" and "+m2+" cannot be compared");
//  	    }
//  	  }
//        }
  }
  
  /****************************************************************
   * Type constructors
   ****************************************************************/

  public static void leq(TypeConstructor t1, TypeConstructor t2)
    throws TypingEx
  {
    try{
      Engine.leq(t1,t2);
    }
    catch(Unsatisfiable e){
      throw new TypingEx("Not satisfiable 4:"+e.getMessage());
    }
  }
  
  /****************************************************************
   * Domains 
   ****************************************************************/

  public static void in(Polytype type, Domain domain)
    throws TypingEx
  {
    type.constraint.assert();
    domain.constraint.assert();
    leq(type.monotype,domain.monotype);
    
  }
  
  /**
   * Checks wether types belong to domains
   *
   * @param types a collection of Polytypes
   * @param domains a collection of Domains
   * @exception BadSizeEx the lists have different size
   */
  
  public static void in(
			Collection types,
			Collection domains
			)
    throws BadSizeEx, TypingEx
  {
    int expected=domains.size();
    int actual=types.size();
    if(expected!=actual)
      throw new BadSizeEx(expected, actual);

    Iterator t=types.iterator();
    Iterator d=domains.iterator();
    while(t.hasNext())
      in((Polytype)t.next(),(Domain)d.next());
  }

  /****************************************************************
   * Interfaces
   ****************************************************************/

  public static void imp(TypeConstructor t, Interface i)
    throws TypingEx
  {
    Debug.println(t+" imp "+i);
  }
  
  public static void abs(TypeConstructor t, Interface i)
    throws TypingEx
  {
    Debug.println(t+" abs "+i);
  }

  public static void imp(TypeConstructor t, Collection c)
    throws TypingEx
  {
    Iterator i=c.iterator();
    
    while(i.hasNext())
      {
	imp(t,(Interface)i.next());
      }
  }
  
  public static void abs(TypeConstructor t, Collection c)
    throws TypingEx
  {
    Iterator i=c.iterator();
    
    while(i.hasNext())
      {
	abs(t,(Interface)i.next());
      }
  }

  static boolean dbg = false;
}
