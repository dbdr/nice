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
//$Modified: Sat Jul 24 19:16:54 1999 by bonniot $

package bossa.typing;

import bossa.util.*;
import bossa.syntax.*;
import java.util.*;

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
   * @param symbols a collection of TypeSymbols,
   *   which represent the new symbols of the context
   * @param context the constraint to ass to the current context
   */
  public static void enter(Collection symbols, Constraint context, String message)
  {
    Debug.println("# "+message+" Entering new typing context : "+
		  Util.map("",", ","",symbols)+
		  " "+context+"\n");
  }

  public static void enter(Type t, String message)
  {
    enter(t.getTypeParameters(),t.getConstraint(), message);
  }

  /**
   * Leaves the last typing context
   *
   */
  public static void leave()
  {
    Debug.println("\n# Leaving typing context\n");
  }

  public static void implies()
  {
    Debug.println("IMPLIES");
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
	leq((Type)i1.next(),(Type)i2.next());
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
    Debug.println(t1+" <: "+t2);
  }

  public static void leq(PolytypeConstructor t1, PolytypeConstructor t2) 
    throws TypingEx
  {
    Debug.println(t1+" <: "+t2);
  }

  /****************************************************************
   * Domains 
   ****************************************************************/

  public static void in(Type type, Domain domain)
    throws TypingEx
  {
    Debug.println(type+" in "+domain);
  }
  
  /**
   * Checks wether types belong to domains
   *
   * @param types a collection of Types
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
      in((Type)t.next(),(Domain)d.next());
  }
}
