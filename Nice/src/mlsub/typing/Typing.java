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

package mlsub.typing;

import java.util.*;

import mlsub.typing.lowlevel.Engine;
import mlsub.typing.lowlevel.Element;
import mlsub.typing.lowlevel.Unsatisfiable;

/**
   Static class for comparing types

   @version $Date$
   @author Daniel Bonniot
 */
public final class Typing
{
  /****************************************************************
   * Typing contexts
   ****************************************************************/

  /**
     Enters a new typing context.
     
     If an enter() completed successfully,
     a matching leave() MUST be issued some time later by the caller.
   */
  public static int enter()
  {
    if(dbg) 
      Debug.println("ENTER "+level);

    Engine.enter();
    return level++;
  }

  // used to verify that enter abd leaves match
  static int level = 0;
  
  /**
     Enters a new typing context
     
     @param message A debug message to know where we are
   */
  public static int enter(String message)
  {
    if(message != null && dbg) Debug.println("## Typechecking "+message);
    return enter();
  }

  static public void introduce(Element e)
  {    
    if(dbg) 
      Debug.println("Typing introduced " + e);

    // a monotype var introduced earlier must be given a new null kind
    // the cleaner way would be to reset it to null when we leave this level
    // but it would be a pain, and the result is the same
    if(e instanceof MonotypeVar)
      e.setKind(null);
    
    Engine.register(e);
  }
  
  static public void introduce(Element[] elements)
  {
    if(elements == null)
      return;
    
    for(int i = 0; i<elements.length; i++)
      introduce(elements[i]);
  }
  
  // TODO: fix this?
  // pb with Interface being a type symbol
  static public void introduceTypeSymbols(TypeSymbol[] elements)
  {
    for(int i = 0; i<elements.length; i++)
      introduce((Element) elements[i]);
    
  }
  
  /**
     Leaves the last typing context.
   */
  public static int leave()
    throws TypingEx
  {
    if(dbg) 
      Debug.println("LEAVE "+(level-1));
    if (dbg && level-1 <= 0)
      bossa.util.Internal.printStackTrace();
    
    try{
      Engine.leave();
    }
    catch(Unsatisfiable e){
      if(dbg) e.printStackTrace();
      throw new TypingEx("Unsatisfiable 1:"+e.getMessage());
    }
    return --level;
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

  public static void createInitialContext()
  {
    try{
      Engine.createInitialContext();
    }
    catch(Unsatisfiable e){
      throw new InternalError("Initial context is not satisfiable");
    }
  }
  
  public static void releaseInitialContext()
  {
    Engine.releaseInitialContext();
  }
  
  /****************************************************************
   * Assertions
   ****************************************************************/

  public static void leq(Collection c1, Collection c2)
    throws TypingEx
  {
    if(c1.size() != c2.size()) 
      throw new InternalError("Unequal sizes in leq");

    Iterator i1 = c1.iterator();
    Iterator i2 = c2.iterator();
    
    while(i1.hasNext())
      {
	leq((Polytype)i1.next(),
	    (Polytype)i2.next());
      }
  }
  
  public static void leqMono(Monotype[] c1, Monotype[] c2)
    throws TypingEx
  {
    if(c1.length != c2.length) 
      throw new InternalError("Unequal sizes in leqMono");

    for(int i = 0; i<c1.length; i++)
      leq(c1[i], c2[i]);
  }
  
  public static void initialLeq(TypeConstructor t, TypeConstructor[] ts)
    throws TypingEx
  {
    for(int i = 0; i<ts.length; i++)
      initialLeq(t, ts[i]);
  }
  
  public static void leq(TypeConstructor t, Collection c)
    throws TypingEx
  {
    Iterator i = c.iterator();
    
    while(i.hasNext())
      leq(t,(TypeConstructor)i.next());
  }
  
  /** Test that t is leq that m's head */
  public static void leq(TypeConstructor t, Monotype m)
  throws TypingEx
  {
    if(t == null)
      return;
    
    Variance v = t.variance;
    if(v == null)
      throw new InternalError("Don't know how to handle this");
    
    try{
      Engine.setKind(m, v);
    }
    catch(Unsatisfiable e){
      throw new TypingEx(t+" < "+m+"'s head");
    }
    leq(t, ((MonotypeConstructor) m.equivalent()).getTC());
  }
  
  public static void leq(TypeConstructor[] ts, Monotype[] ms)
  throws TypingEx
  {
    for(int i = 0; i < ts.length; i++)
      leq(ts[i], ms[i]);
  }
  
  /****************************************************************
   * Testing Polytype <= Polytype
   ****************************************************************/

  public static void leq(Polytype t1, Polytype t2) 
    throws TypingEx
  {
    
    if (!(Constraint.hasBinders(t1.getConstraint()) || 
	  Constraint.hasBinders(t2.getConstraint())))
      {
	leq(t1.getMonotype(), t2.getMonotype());
	return;
      }
    
    if(dbg)
      Debug.println("Polytype leq: "+t1+" <: "+t2);

    int l;
    if(dbg) l=enter("#"); else l=enter();
    
    try{
      Constraint.assert(t2.getConstraint());

      implies();
    
      Constraint.assert(t1.getConstraint());
      leq(t1.getMonotype(), t2.getMonotype());
    }
    finally{
      if(leave() != l)
	throw new InternalError("Unmatched enters and leaves");
    }
  }

  /** Particular case. */
  public static void leq(Polytype t1, Monotype m2) 
    throws TypingEx
  {
    if (!(Constraint.hasBinders(t1.getConstraint())))
      {
	leq(t1.getMonotype(), m2);
	return;
      }
    
    if(dbg)
      Debug.println("Polytype leq: "+t1+" <: "+m2);

    int l;
    if(dbg) l=enter("#"); else l=enter();
    
    try{
      implies();
    
      Constraint.assert(t1.getConstraint());
      leq(t1.getMonotype(), m2);
    }
    finally{
      if(leave() != l)
	throw new InternalError("Unmatched enters and leaves");
    }
  }

  /****************************************************************
   * Monotypes
   ****************************************************************/

  public static void leq(Monotype m1, Monotype m2)
    throws TypingEx
  {
    if(dbg) 
      Debug.println("Monotype leq: " + m1 + " <: " + m2);

    try{
      Engine.leq(m1,m2);
    }
    catch(Unsatisfiable e){
      if(dbg) 
	e.printStackTrace();
      throw new TypingEx("Typing.leq(" + m1 + ", " + m2 + ")" +
			 " [was " + e.getMessage()+"]");
    }    
  }
  
  public static void eq(Monotype m1, Monotype m2)
    throws TypingEx
  {
    leq(m1,m2);
    leq(m2,m1);
  }
  
  /****************************************************************
   * Type constructors
   ****************************************************************/

  public static void initialLeq(TypeConstructor t1, TypeConstructor t2)
    throws TypingEx
  {
    if(dbg) Debug.println("Initial leq: "+t1+" < "+t2);
    
    try{
      Engine.leq(t1,t2,true);
    }
    catch(Unsatisfiable e){
      throw new KindingEx(t1,t2);
    }
  }
  
  public static void leq(TypeConstructor t1, TypeConstructor t2)
    throws TypingEx
  {
    if(dbg) Debug.println("TC leq: "+t1+" < "+t2);
    
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

  /** Test if d1 is a subdomain of d2. */
  public static void leq(Domain d1, Domain d2)
    throws TypingEx
  {
    if(dbg) Debug.println(d1+" leq "+d2);
    
    if(d1 == Domain.bot)
      return;

    enter();
    try{
      Constraint.assert(d1.getConstraint());

      Typing.implies();

      Constraint.assert(d2.getConstraint());
      leq(d1.getMonotype(), d2.getMonotype());
    }
    finally{
      leave();
    }
  }
  
  /** Test if a polytype is in a domain. */
  public static void in(Polytype type, Domain domain)
    throws TypingEx
  {
    if(dbg) Debug.println(type+" in "+domain);
    
    if(domain == Domain.bot)
      return;
    
    Constraint.assert(type.getConstraint());
    Constraint.assert(domain.getConstraint());
    leq(type.getMonotype(),domain.getMonotype());
  }
  
  /** Test if a monotype is in a domain. */
  public static void in(Monotype type, Domain domain)
    throws TypingEx
  {
    if(dbg) Debug.println(type+" in "+domain);
    
    if(domain == Domain.bot)
      return;
    
    Constraint.assert(domain.getConstraint());
    leq(type, domain.getMonotype());
  }
  
  /**
   * Checks wether types belong to domains
   *
   * @param types a collection of Polytypes
   * @param domains a collection of Domains
   * @exception TypingEx
   */  
  public static void in(Polytype[] types,
			Domain[] domains)
    throws TypingEx
  {
    int expected = domains.length;
    int actual = types.length;
    if(expected != actual)
      throw new BadSizeEx(expected, actual);

    for(int i = 0; i<actual; i++)
      in(types[i], domains[i]);
  }

  /**
   * Checks wether monotypes belong to domains.
   * This is just the special case where all polytypes are monomorphic.
   *
   * @param types a collection of Monotypes
   * @param domains a collection of Domains
   * @exception TypingEx
   */
  public static void in(Monotype[] types,
			Domain[] domains)
    throws TypingEx
  {
    int expected = domains.length;
    int actual = types.length;
    if(expected != actual)
      throw new BadSizeEx(expected, actual);

    for(int i = 0; i<actual; i++)
      in(types[i], domains[i]);
  }

  /****************************************************************
   * Interfaces assertions
   ****************************************************************/

  public static void assertLeq(Interface i, Interface j)
  throws KindingEx
  {
    if(dbg) Debug.println(i+" < "+j);
    if(!(i.variance.equals(j.variance)))
      throw new KindingEx(i,j);
    i.variance.subInterface(i.itf,j.itf);
  }
  
  public static void assertLeq(Interface itf, Interface[] is)
  throws KindingEx
  {
    for (int i = is.length; --i >= 0;)
      assertLeq(itf, is[i]);
  }
  
  public static void assertImp(TypeConstructor t, Interface i, 
			       boolean initial)
    throws TypingEx
  {
    if(dbg) Debug.println(t+" imp "+i);

    try{
      Engine.setKind(t,i.variance.getConstraint());
    }
    catch(Unsatisfiable e){
      throw new TypingEx
	(t+" cannot implement "+i
	 +":\n"+t+" has variance "+t.getKind()+", "
	 +i+" has variance "+i.variance.getConstraint());
    }

    try{
      if(initial)
	t.variance.initialImplements(t.getId(),i.itf);
      else
	t.variance.indexImplements(t.getId(),i.itf);

      TypeConstructor tc = i.associatedTC();
      if(tc != null)
	Engine.leq(t, tc, initial);
    }
    catch(Unsatisfiable e){
      throw new TypingEx(e.getMessage());
    }
  }
  
  public static void assertAbs(TypeConstructor t, Interface i)
    throws TypingEx
  {
    if(dbg) Debug.println(t+" abs "+i);

    if(Engine.isRigid(t))
      throw new InternalError
	("Abstraction required on a rigid type constructor : \n"+
	 t+" required to abstract "+i);
    
    i.variance.initialAbstracts(t.getId(),i.itf);
  }

  public static void assertImp(TypeConstructor t, 
			       Interface[] is, 
			       boolean initial)
    throws TypingEx
  {
    for(int i = 0; i<is.length; i++)
      assertImp(t, is[i], initial);
  }
  
  public static void assertAbs(TypeConstructor t, Interface[] is)
    throws TypingEx
  {
    for (int i = 0; i<is.length; i++)
      assertAbs(t, is[i]);
  }

  /****************************************************************
   * Rigid tests
   ****************************************************************/

  public static boolean testRigidLeq(TypeConstructor t1, TypeConstructor t2)
  {
    if(t1.getKind() == null 
       || t2.getKind() == null)
      throw new InternalError("Null kind for "+t1+" or "+t2);
    
    if(t1.getKind() != t2.getKind())
      return false;

    return ((Engine.Constraint) t1.getKind()).isLeq(t1,t2);
  }
  
  /**
     Find a type constructor greater than the parameter
     that can exist at runtime (isConcrete() is true).
  */
  public static TypeConstructor lowestRigidSuperTC(TypeConstructor tc)
  {
    Engine.Constraint cst = (Engine.Constraint) tc.getKind();
    
    if (!cst.isValid(tc))
      // we are not in the context for TC anymore
      // be careful to call lowestRigidSuperTC when the context is appropriate.
      {
	System.out.println
	  ("Warning: lowestRigidSuperTC called inapropriately for " + tc);
	return null;
      }
    
    // the allowWidening last parameter should be removed if always use it as false
    return (TypeConstructor) cst.lowestRigidSuperElement(tc, false);
  }
  
  /****************************************************************
   * Enumeration
   ****************************************************************/

  /**
   * Enumerate all the tuples of tags in a Domain
   *
   * @return a List of TypeConstructor[]
   *   an element of an array is set to null
   *   if it cannot be matched (e.g. a function type)
   */
  public static List enumerate(Domain domain)
  {
    List res = new ArrayList();

    Monotype[] tags = null;
    
    if (domain.getMonotype().equivalent() instanceof TupleType)
      tags = ((TupleType) domain.getMonotype().equivalent()).types;
    
    if (tags == null)
      throw new InternalError("enumerate should be done on a tuple domain");

    try
      {
	int l=enter();

	try{
	  Constraint.assert(domain.getConstraint());
	  setFloatingKinds(tags,0,res);
	}
	finally{
	  if(leave() != l)
	    throw new InternalError("Unmatched enter and leaves");
	}
      }
    catch(TypingEx e){
      // There is no solution
      return new LinkedList();
    }
    catch(Unsatisfiable e){
      throw new InternalError("This shouldn't happen");
    }

    return res;
  }
  
  private static final void setFloatingKinds(Monotype[] tags, 
					     int minFloating, 
					     List res) 
    throws Unsatisfiable
  {
    // Possible optimization: the successive values of minFloating 
    // for which getKind == null are always the same, compute once.
    while(minFloating<tags.length 
	  && tags[minFloating].getKind() != null
	  && tags[minFloating].getKind() != Engine.variablesConstraint)
      minFloating++;

    if(minFloating<tags.length)
      {
	for(Iterator cs = Engine.listConstraints();
	    cs.hasNext();)
	  {
	    Engine.Constraint c = (Engine.Constraint) cs.next();
	  
	    if(c.hasConstants())
	      {
		if(linkDbg)
		  Debug.println("Choosing kind "+c+" for "+tags[minFloating]);
		
		if(tags[minFloating] instanceof MonotypeVar)
		  Engine.forceKind(tags[minFloating],c.associatedKind);
		else
		  Engine.forceKind(tags[minFloating],c);

		// recursive call
		setFloatingKinds(tags,minFloating+1,res);
		tags[minFloating].setKind(null);
	    }
	  }
      }
    else
      res.addAll(enumerateTags(tags));
  }
  
  private static List enumerateTags(Monotype[] tags)
  {
    List tuples = new ArrayList(); /* of List of TypeConstructor */
    List kinds = new ArrayList(tags.length); /* euristic: 
						at most one kind per tag */
    List observers = new ArrayList(tags.length); // idem

    Engine.enter();
    try{
      
      for(int i = 0;i<tags.length;i++)
	{
	  Engine.Constraint k = Engine.getConstraint(tags[i].getKind());
	  mlsub.typing.lowlevel.BitVector obs;
	
	  int idx = kinds.indexOf(k);
	  if(idx<0)
	    {
	      kinds.add(k);
	      observers.add(obs = new mlsub.typing.lowlevel.BitVector());
	    }
	  else
	    obs = (mlsub.typing.lowlevel.BitVector) observers.get(idx);
	
	  // ignore non matchable kinds
	  if(tags[i].getKind() instanceof FunTypeKind ||
	     tags[i].getKind() instanceof TupleKind)
	    {
	      continue;
	    }

	  TypeConstructor constTC = null;
	  if (tags[i].equivalent() instanceof MonotypeConstructor)
	    constTC = ((MonotypeConstructor) tags[i].equivalent()).getTC();
	  if(constTC == null)
	    throw new InternalError
	      (tags[i].getKind() + " is not a valid kind in enumerate");
	
	  TypeConstructor varTC = 
	    new TypeConstructor(constTC.variance);
      	
	  varTC.enumerateTagIndex = i;
	  introduce(varTC);
	  obs.set(varTC.getId());
	  try{
	    k.leq(varTC, constTC);
	    k.reduceDomainToConcrete(varTC);
	  }
	  catch(Unsatisfiable e){
	    // tuples is empty here
	    return tuples;
	  }
	}
    
      Object[] a = kinds.toArray();
      Engine.Constraint[] pKinds = new Engine.Constraint[a.length];
      System.arraycopy(a,0,pKinds,0,a.length);
      
      a = observers.toArray();
      mlsub.typing.lowlevel.BitVector[] pObs = new mlsub.typing.lowlevel.BitVector[a.length];
      System.arraycopy(a,0,pObs,0,a.length);
      
      enumerateInConstraints(pKinds,pObs,tuples,tags.length);
    }
    finally{
      Engine.backtrack();
    }
    
    return tuples;
  }
  
  private static void enumerateInConstraints
    (Engine.Constraint[] kinds,
     mlsub.typing.lowlevel.BitVector[] observers,
     final List tuples,
     final int width)
  {
    final boolean[] first = new boolean[1]; /* using final boolean[] 
					       is a trick to access it 
					       from the closure */
    for(int act = 0; act<kinds.length;act++)
      {
	first[0] = true;
	final int ancientSize = (tuples.size()==0 ? 1 : tuples.size());
	
	final mlsub.typing.lowlevel.BitVector obs = observers[act];
	final mlsub.typing.lowlevel.Engine.Constraint kind = kinds[act];
	
	kind.enumerate
	  (obs,
	   new mlsub.typing.lowlevel.LowlevelSolutionHandler()
	     {
	       public void handle()
		 {
		   if(first[0])
		     {
		       first[0] = false;
		       if(tuples.size() == 0)
			 tuples.add(new TypeConstructor[width]);
		     }
		   else
		     // copy the ancientSize first elements at the end
		     {
		       for(int i = 0; i<ancientSize; i++)
			 tuples.add(((Object[])tuples.get(i)).clone());
		     }
		   for (int x = obs.getLowestSetBit();
			x != mlsub.typing.lowlevel.BitVector.UNDEFINED_INDEX;
			x = obs.getNextBit(x))
		     {
		       TypeConstructor var,sol;
		       var = (TypeConstructor) kind.getElement(x);
		       sol = (TypeConstructor) kind.getElement(getSolutionOf(x));
		       for(int i = 0; i<ancientSize; i++)
			 ((TypeConstructor[])tuples.get(i))[var.enumerateTagIndex] = sol;
		     }
		 }
	     }
	   );
      }
  }

  public static boolean dbg;
  public static boolean linkDbg;
}
