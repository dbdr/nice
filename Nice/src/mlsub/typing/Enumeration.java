/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2001                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package mlsub.typing;

import java.util.*;

import mlsub.typing.lowlevel.Engine;
import mlsub.typing.lowlevel.Unsatisfiable;

/**
   Enumeration of the type constructors in a domain.

   @version $Date$
   @author Daniel Bonniot
*/

public class Enumeration
{
  /**
   * Enumerate all the tuples of tags in a Domain
   *
   * @return a List of TypeConstructor[]
   *   an element of an array is set to null
   *   if it cannot be matched (e.g. a function type)
   */
  public static List enumerate(Domain domain)
  {
    // XXX try LinkedList
    List res = new ArrayList();

    Monotype[] tags = null;
    
    if (domain.getMonotype().equivalent() instanceof TupleType)
      tags = ((TupleType) domain.getMonotype().equivalent()).types;
    
    if (tags == null)
      throw new InternalError("enumerate should be done on a tuple domain");

    try
      {
	int l = Typing.enter();

	try{
	  Constraint.assert(domain.getConstraint());
	  setFloatingKinds(tags,0,res);
	}
	finally{
	  if (Typing.leave() != l)
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
  
  /** Try all combinations of Kinds for free type variables */
  private static final void setFloatingKinds(Monotype[] tags, 
					     int minFloating, 
					     List res) 
    throws Unsatisfiable
  {
    while(minFloating<tags.length 
	  && tags[minFloating].getKind() != null
	  && tags[minFloating].getKind() != Engine.variablesConstraint)
      minFloating++;

    if(minFloating<tags.length)
      {
	for(Iterator cs = Engine.listConstraints(); cs.hasNext();)
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
    TagsList tuples = new TagsList(tags.length);
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
	  // XXX move up ?
	  if(tags[i].getKind() instanceof FunTypeKind ||
	     tags[i].getKind() instanceof TupleKind)
	    continue;

	  TypeConstructor constTC = tags[i].head();
	  if(constTC == null)
	    throw new InternalError
	      (tags[i].getKind() + " is not a valid kind in enumerate");
	
	  TypeConstructor varTC = new TypeConstructor(constTC.variance);
	  
	  varTC.enumerateTagIndex = i;
	  Typing.introduce(varTC);
	  obs.set(varTC.getId());
	  try{
	    k.leq(varTC, constTC);
	    k.reduceDomainToConcrete(varTC);
	  }
	  catch(Unsatisfiable e){
	    return emptyList;
	  }
	}
    
      Object[] a = kinds.toArray();
      Engine.Constraint[] pKinds = new Engine.Constraint[a.length];
      System.arraycopy(a,0,pKinds,0,a.length);
      
      a = observers.toArray();
      mlsub.typing.lowlevel.BitVector[] pObs = new mlsub.typing.lowlevel.BitVector[a.length];
      System.arraycopy(a,0,pObs,0,a.length);
      
      enumerateInConstraints(pKinds,pObs,tuples);
    }
    finally{
      Engine.backtrack();
    }
    
    return tuples.tags;
  }

  private static List emptyList = new LinkedList();

  private static void enumerateInConstraints
    (Engine.Constraint[] kinds,
     mlsub.typing.lowlevel.BitVector[] observers,
     final TagsList tuples)
  {
    for(int act = 0; act<kinds.length;act++)
      {
	tuples.startAddition();
	
	final mlsub.typing.lowlevel.BitVector obs = observers[act];
	final mlsub.typing.lowlevel.Engine.Constraint kind = kinds[act];
	
	kind.enumerate
	  (obs,
	   new mlsub.typing.lowlevel.LowlevelSolutionHandler()
	     {
	       public void handle()
		 {
		   tuples.startEntry();
		   for (int x = obs.getLowestSetBit();
			x != mlsub.typing.lowlevel.BitVector.UNDEFINED_INDEX;
			x = obs.getNextBit(x))
		     {
		       TypeConstructor var,sol;
		       var=(TypeConstructor) kind.getElement(x);
		       sol=(TypeConstructor) kind.getElement(getSolutionOf(x));
		       tuples.set(var.enumerateTagIndex, sol);
		     }
		 }
	     }
	   );
      }
  }

  /** 
      A list of possible values for tags.

      Typical usage:
      <code>

      TagsList tags = new TagsList(width);
      for (...) 
      {
        ...
        tags.startAddition();
	for (each possibility for the tags in the group) 
	{
	  tags.startEntry();
	  tags.set(tag1, value1);
	  tags.set(tag1, value2);
	}
      }

      </code>
  */
  private static class TagsList
  {
    TagsList(int width)
    {
      this.width = width;
    }

    private int size, width;
    private boolean first = true;
    final List tags = new ArrayList();

    /** Call before enumeration of the possibilities for some tags */
    void startAddition()
    {
      size = tags.size();
      first = true;
    }

    /** Call before each possibility for these tags */
    void startEntry()
    {
      if (first)
	{
	  first = false;
	  if (tags.size() == 0)
	    {
	      tags.add(new TypeConstructor[width]);
	      size = 1;
	    }
	}
      else
	// copy the first elements at the end, 
	// so that they can be overwritten with the new values
	for(int i = 0; i < size; i++)
	  tags.add(((Object[]) tags.get(i)).clone());
    }

    /** Set the value of one tag */
    void set(int index, TypeConstructor tag)
    {
      for(int i = 0; i < size; i++)
	((TypeConstructor[]) tags.get(i))[index] = tag;
    }
  }

  public static boolean linkDbg;
}
