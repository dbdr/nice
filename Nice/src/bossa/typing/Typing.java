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
//$Modified: Thu Nov 04 14:49:21 1999 by bonniot $

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
    enter("");
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
    if(dbg) Debug.println("Typing.introduce "+e);
    
    //TODO: this is DIRTY !
    if(e instanceof MonotypeVar)
      e.setKind(null);
    
    Engine.register(e);
    if(dbg) Debug.println("Typing introduced "+e);
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
	  Internal.warning("introducing a "+o.getClass());
      }
  }
  
  /**
   * Leaves the last typing context
   *
   */
  public static void leave()
    throws TypingEx
  {
    if(dbg) Debug.println("LEAVE");
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
    if(dbg) Debug.println("IMPLIES");
    try{
      Engine.implies();
    }
    catch(Unsatisfiable e){
      throw new TypingEx("Not satisfiable "+e.getMessage());
    }
    
  }

  private static void unsatisfiable(TypingEx e) throws TypingEx
  {
    Engine.backtrack();
    throw e;
  }
  
  private static void unsatisfiable(BadSizeEx e) throws BadSizeEx
  {
    Engine.backtrack();
    throw e;
  }
  
  /****************************************************************
   * Assertions
   ****************************************************************/

  public static void leq(Collection c1, Collection c2)
    throws TypingEx
  {
    Internal.error(c1.size()!=c2.size(),"Unequal sizes in leq");
    Iterator i1=c1.iterator();
    Iterator i2=c2.iterator();
    
    while(i1.hasNext())
      {
	leq((Polytype)i1.next(),
	    (Polytype)i2.next());
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
  
  public static void initialLeq(TypeConstructor t, Collection c)
    throws TypingEx
  {
    Iterator i=c.iterator();
    
    while(i.hasNext())
      initialLeq(t,(TypeConstructor)i.next());
  }
  
  public static void leq(TypeConstructor t, Collection c)
    throws TypingEx
  {
    Iterator i=c.iterator();
    
    while(i.hasNext())
      leq(t,(TypeConstructor)i.next());
  }
  
  /****************************************************************
   * Testing Polytype <= Polytype
   ****************************************************************/

  public static void leq(Polytype t1, Polytype t2) 
    throws TypingEx
  {
    if(dbg) Debug.println("Polytype leq: "+t1+" <: "+t2);
    
    if(dbg) enter("#"); else enter();
    
    t2.getConstraint().assert();

    implies();
    
    t1.getConstraint().assert();
    leq(t1.getMonotype(),t2.getMonotype());

    leave();
  }

  /****************************************************************
   * Monotypes
   ****************************************************************/

  public static void leq(Monotype m1, Monotype m2)
    throws TypingEx
  {
    if(dbg) Debug.println("Monotype leq :"+m1+" <: "+m2);
    try{
      Engine.leq(m1,m2);
    }
    catch(Unsatisfiable e){
      //e.printStackTrace();
      throw new TypingEx("Typing.leq("+m1+","+m2+") [was "+e.getMessage()+"]");
    }    
  }
  
  /****************************************************************
   * Type constructors
   ****************************************************************/

  public static void initialLeq(TypeConstructor t1, TypeConstructor t2)
    throws TypingEx
  {
    try{
      Engine.leq(t1,t2,true);
    }
    catch(Unsatisfiable e){
      throw new TypingEx("Not satisfiable 4:"+e.getMessage());
    }
  }
  
  public static void leq(TypeConstructor t1, TypeConstructor t2)
    throws TypingEx
  {
    try{
      Engine.leq(t1,t2,false);
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
    if(dbg) Debug.println(type+" in "+domain);
    
    type.getConstraint().assert();
    domain.constraint.assert();
    leq(type.getMonotype(),domain.monotype);
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
      unsatisfiable(new BadSizeEx(expected, actual));

    Iterator t=types.iterator();
    Iterator d=domains.iterator();
    while(t.hasNext())
      in((Polytype)t.next(),(Domain)d.next());
  }

  /****************************************************************
   * Interfaces assertions
   ****************************************************************/

  public static void assertLeq(InterfaceDefinition i, InterfaceDefinition j)
  {
    if(dbg) Debug.println(i+" < "+j);
    User.error(!(i.variance.equals(j.variance)),i+" and "+j+" are not comparable");
    i.variance.subInterface(i.itf,j.itf);
  }
  
  public static void assertLeq(InterfaceDefinition itf, Collection c)
  {
    for(Iterator i=c.iterator();i.hasNext();)
      assertLeq(itf,((Interface)i.next()).definition);
  }
  
  public static void assertImp(TypeConstructor t, InterfaceDefinition i, boolean initial)
    throws TypingEx
  {
    if(dbg) Debug.println(t+" imp "+i);
    try{
      Engine.setKind(t,i.variance.getConstraint());
    }
    catch(Unsatisfiable e){
      unsatisfiable(new TypingEx
		    (t+" cannot implement "+i
		     +":\n"+t+" has variance "+t.getKind()+", "
		     +i+" has variance "+i.variance.getConstraint()));
    }

    try{
      if(initial)
	t.variance.initialImplements(t.getId(),i.itf);
      else
	t.variance.indexImplements(t.getId(),i.itf);
    }
    catch(Unsatisfiable e){
      unsatisfiable(new TypingEx(e.getMessage()));
    }
  }
  
  public static void assertAbs(TypeConstructor t, InterfaceDefinition i)
    throws TypingEx
  {
    if(dbg) Debug.println(t+" abs "+i);

    Internal.error(Engine.isRigid(t),
		   "Abstraction required on a rigid type constructor : \n"+
		   t+" required to abstract "+i);
    
    i.variance.initialAbstracts(t.getId(),i.itf);
  }

  public static void assertImp(TypeConstructor t, Collection c, boolean initial)
    throws TypingEx
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      assertImp(t,((Interface)i.next()).definition,initial);
  }
  
  public static void assertAbs(TypeConstructor t, Collection c)
    throws TypingEx
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      assertAbs(t,((Interface)i.next()).definition);
  }

  public static boolean dbg = Debug.typing;
}
