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
//$Modified: Tue Nov 16 19:23:18 1999 by bonniot $

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
    if(domain==Domain.bot)
      return;
    
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

  /****************************************************************
   * Rigid tests
   ****************************************************************/

  public static boolean testRigidLeq(TypeConstructor t1, TypeConstructor t2)
  {
    if(t1.getKind()!=t2.getKind())
      Internal.error("Bad kinding in isRigidLeq for "+t1+" and "+t2);

    if(t1.getKind()==null)
      Internal.error("Null kind for "+t1+" and "+t2);
    
    return ((Engine.Constraint) t1.getKind()).isLeq(t1,t2);
  }
  
  /****************************************************************
   * Enumeration
   ****************************************************************/

  public static List enumerate(Domain domain)
  {
    List res = new ArrayList();

    Monotype[] tags = domain.getTuple();

    try
      {
	enter();
	domain.constraint.assert();
	setFloatingKinds(tags,0,res);
	leave();
      }
    catch(TypingEx e){
      Internal.error("The domain to enumerate is not well formed");
    }
    catch(Unsatisfiable e){
      Internal.error("This shouldn't happen");
    }

    return res;
  }
  
  private static final void setFloatingKinds(Monotype[] tags, int minFloating, List res) throws Unsatisfiable
  {
    // Possible optimization: the successive values of minFloating 
    // for which getKind==null are always the same, compute once.
    while(minFloating<tags.length && tags[minFloating].getKind()!=null)
      minFloating++;

    if(minFloating<tags.length)
      for(Iterator k = Engine.listConstraints();
	  k.hasNext();)
	{
	  Engine.Constraint c = (Engine.Constraint) k.next();
	  if(c.hasConstants())
	    Engine.forceKind(tags[minFloating],c);
	  setFloatingKinds(tags,minFloating+1,res);
	}
    else
      res.addAll(enumerateTags(tags));
  }
  
  private static List enumerateTags(Monotype[] tags)
  {
    List tuples = new ArrayList(); /* of List of TypeConstructor */
    List kinds = new ArrayList(tags.length); // at most one kind per tag
    List observers = new ArrayList(tags.length); // idem

    for(int i=0;i<tags.length;i++)
      {
	Engine.Constraint k = Engine.getConstraint(tags[i].getKind());
	bossa.engine.BitVector obs;
	
	int idx=kinds.indexOf(k);
	if(idx<0)
	  {
	    kinds.add(k);
	    observers.add(obs=new bossa.engine.BitVector());
	  }
	else
	  obs = (bossa.engine.BitVector) observers.get(idx);
	
	TypeConstructor constTC = tags[i].getTC();
	TypeConstructor varTC = new TypeConstructor(new LocatedString("enum"+i,Location.nowhere()),constTC.variance);
	varTC.enumerateTagIndex=i;
	introduce(varTC);
	obs.set(varTC.getId());
	try{
	  k.leq(varTC,constTC);
	  k.reduceDomainToConcrete(varTC);
	}
	catch(Unsatisfiable e){
	  Internal.error("Typing.enumerate");
	}
      }
    
    tuples.add(new TypeConstructor[tags.length]);
    
    Object[] a = kinds.toArray();
    Engine.Constraint[] pKinds = new Engine.Constraint[a.length];
    System.arraycopy(a,0,pKinds,0,a.length);
    
    a = observers.toArray();
    bossa.engine.BitVector[] pObs = new bossa.engine.BitVector[a.length];
    System.arraycopy(a,0,pObs,0,a.length);
    
    enumerateInConstraints(pKinds,pObs,tuples,tags.length);
    
    return tuples;
  }
  
  private static void enumerateInConstraints(Engine.Constraint[] kinds,bossa.engine.BitVector[] observers,
					     final List tuples,int width)
  {
    final boolean[] first=new boolean[1]; // using boolean[1] is a trick to access it from the closure
    for(int act=0;act<kinds.length;act++)
      {
	first[0]=true;
	final int ancientSize=tuples.size();
	
	final bossa.engine.BitVector obs = observers[act];
	final bossa.engine.Engine.Constraint kind = kinds[act];
	
	kind.enumerate
	  (obs,
	   new bossa.engine.LowlevelSolutionHandler()
	     {
	       public void handle()
		 {
		   if(first[0])
		     first[0]=false;
		   else
		     // copy the ancientSize first elements at the end
		     {
		       for(int i=0;i<ancientSize;i++)
			 tuples.add(((Object[])tuples.get(i)).clone());
		     }
		   for (int x = obs.getLowestSetBit();
			x != bossa.engine.BitVector.UNDEFINED_INDEX;
			x = obs.getNextBit(x))
		     {
		       TypeConstructor var,sol;
		       var=(TypeConstructor) kind.getElement(x);
		       sol=(TypeConstructor) kind.getElement(getSolutionOf(x));
			 
		       for(int i=0;i<ancientSize;i++)
			 ((TypeConstructor[])tuples.get(i))[var.enumerateTagIndex]=sol;
		     }
		 }
	     }
	   );
      }
  }

  public static boolean dbg = Debug.typing;
}
