/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
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
import mlsub.typing.lowlevel.BitVector;
import mlsub.typing.lowlevel.Element;
import mlsub.typing.lowlevel.Kind;

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
  /*
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
	  Constraint.enter(domain.getConstraint());
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
  */

  /**
   * Enumerate all the tuples of tags in a Domain
   *
   * If all[i] is false, we are not interested in getting all solutions 
   * for position i, only one witness is enough.
   *
   * @return a List of TypeConstructor[]
   *   an element of an array is set to null
   *   if it cannot be matched (e.g. a function type)
   */
  public static List enumerate(Constraint cst, Element[] tags, boolean[] all)
  {
    // XXX try LinkedList
    List res = new ArrayList();

    try
      {
	int l = Typing.enter();

	try{
	  Constraint.enter(cst);
	  setFloatingKinds(tags, all, 0, res, true);
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
  
  /** Try all combinations of Kinds for free type variables. 
      There are two stages:
      If doAll is true, set the tags for which all solutions must be listed.
      Otherwise, set the other tags, and stop as soon as one solution is found.
   */
  private static final void setFloatingKinds(Element[] tags, 
					     boolean[] all,
					     int minFloating, 
					     List res,
					     boolean doAll) 
    throws Unsatisfiable
  {
    while(minFloating<tags.length 
	  && (all[minFloating] != doAll || 
              isFixedKind(tags[minFloating].getKind())))
      minFloating++;

    if(minFloating<tags.length)
      {
        Element tag = tags[minFloating];
        
        if (tag.getKind() == TopMonotype.TopKind.instance)
          {
            // Tag is "Object". All TCs are solutions.
            tag = tags[minFloating] = new MonotypeVar("enumeration");
          }
        else
          {
            // There might be a garbagy Engine.variablesConstraint 
            // in the monotype variable variable
            tag.setKind(null);
          }

	for(Iterator cs = Engine.listConstraints(); cs.hasNext();)
	  {
	    Engine.Constraint c = (Engine.Constraint) cs.next();
	    
	    if (c.hasConstants() && 
		c != bossa.syntax.PrimitiveType.nullTC.getKind())
	      {
		if(linkDbg)
		  Debug.println("Choosing kind " + c + " for " + tag);
		
		if (tag instanceof MonotypeVar)
		  Engine.forceKind(tag, c.associatedKind);
		else
		  Engine.forceKind(tag, c);

		// recursive call
		setFloatingKinds(tags, all, minFloating + 1, res, doAll);
		tag.setKind(null);
	    }
	  }
      }
    else if (doAll)
      {
	try {
	  setFloatingKinds(tags, all, 0, res, false);
	}
	catch(SolutionFound ex) {
	  // Continue with modifying kinds of all-marked tags.
	}
      }
    else
      {
	List solutions = enumerateTags(tags, all);
	res.addAll(solutions);
	if (solutions.size() > 0) 
	  /*
	     We found solutions for the current choice of kinds for tags.
	     We might find more by changing the kinds of non all-marked tags,
	     but we are not interested in this. So we skip to the next choice
	     of kinds for all-marked tags.
	  */
	  throw new SolutionFound();
      }
  }

  private static boolean isFixedKind(Kind k)
  {
    return k != null
      && k != Engine.variablesConstraint
      && k != TopMonotype.TopKind.instance;
  }

  private static class SolutionFound extends RuntimeException {}

  private static List enumerateTags(Element[] tags, boolean[] all)
  {
    TagsList tuples = new TagsList(tags.length);
    List kinds = new ArrayList(tags.length); /* euristic: 
						at most one kind per tag */
    List observers = new ArrayList(tags.length); // idem

    // The variable TCs that will hold the solutions.
    TypeConstructor[] vars = new TypeConstructor[tags.length];

    Engine.enter();
    try{
      
      for(int i = 0;i<tags.length;i++)
	{
	  Engine.Constraint k = Engine.getConstraint(tags[i].getKind());
	  BitVector obs;
	  
	  int idx = kinds.indexOf(k);
	  if(idx<0)
	    {
	      kinds.add(k);
	      observers.add(obs = new BitVector());
	    }
	  else
	    obs = (BitVector) observers.get(idx);
	  
	  // ignore non matchable kinds
	  // XXX move up ?
	  if(tags[i].getKind() instanceof FunTypeKind ||
	     tags[i].getKind() instanceof TupleKind)
	    continue;

	  TypeConstructor constTC;
	  if (tags[i] instanceof TypeConstructor)
	    constTC = (TypeConstructor) tags[i];
	  else
	    constTC = ((Monotype) tags[i]).head();
	  
	  if(constTC == null)
	    throw new InternalError
	      (tags[i].getKind() + " is not a valid kind in enumerate");
	
	  TypeConstructor varTC = new TypeConstructor(constTC.variance);
	  vars[i] = varTC;

	  Typing.introduce(varTC);

          // We only observe those positions where all solutions are needed.
	  if (all[i])
            obs.set(varTC.getId());

	  try{
	    k.leq(varTC, constTC);
	    k.reduceDomainToConcrete(varTC);
	  }
	  catch(Unsatisfiable e){
	    return emptyList;
	  }
	}
    
      Engine.Constraint[] pKinds = (Engine.Constraint[])
	kinds.toArray(new Engine.Constraint[kinds.size()]);
      
      BitVector[] pObs = (BitVector[]) 
	observers.toArray(new BitVector[observers.size()]);

      if (enumerateInConstraints(pKinds, pObs, tuples, tags, vars, all))
	return emptyList;
    }
    finally{
      Engine.backtrack();
    }
    
    return tuples.tags;
  }

  private static List emptyList = new LinkedList();

  /** return true if there is no solution. */
  private static boolean enumerateInConstraints
    (Engine.Constraint[] kinds,
     BitVector[] observers,
     final TagsList tuples,
     final Element[] tags,
     final TypeConstructor[] vars,
     final boolean[] all)
  {
    for(int act = 0; act<kinds.length;act++)
      {
	tuples.startAddition();
	
	final BitVector obs = observers[act];
	final mlsub.typing.lowlevel.Engine.Constraint kind = kinds[act];
	
	kind.enumerate
	  (obs,
	   new mlsub.typing.lowlevel.LowlevelSolutionHandler()
	     {
	       public void handle()
		 {
                   // Check if this is really a solution, because of
                   // class constraints.
                   for (int index = 0; index < vars.length; index++)
                     {
                       /* If this index does not need all solutions
                          (i.e. all[index] is false), and the solution
                          sol does not pass checkClassConstraint, 
                          it might have been that some other solution, which
                          we don't generate, would pass. So in this case
                          we should restart to try other solutions for this
                          index to see if at least one passes or not.
                       */

		       TypeConstructor var,solution;
                       var = vars[index];

                       // We only deal with matchable tags, 
                       // and those that belong to this kind.
                       if (var == null || var.getKind() != kind)
                         continue;

		       solution = (TypeConstructor)
                         kind.getElement(getSolutionOf(var.getId()));
                       if (! checkClassConstraint(tags[index], solution))
                         return;
                     }

                   // It is a solution, let's add it to the list.
		   tuples.startEntry();
                   for (int index = 0; index < vars.length; index++)
		     {
		       TypeConstructor var, solution;
		       var = vars[index];

                       // We only deal with matchable tags, 
                       // and those that belong to this kind.
                       if (var == null || var.getKind() != kind)
                         continue;

		       solution = (TypeConstructor) 
                         kind.getElement(getSolutionOf(var.getId()));
		       tuples.set(index, solution);
		     }
		 }
	     }
	   );

	// If there is no solution for a variable in the current kind,
	// then handle() will never be called and endAddition() returns true.
	// There is therefore no global solution, and we return true.
	if (tuples.endAddition())
	  return true;
      }
    return false;
  }

  /**
     Return true if and only if sol can be the runtime tag of a 
     subtype of var. This especially involves checking the eventual
     constraints on the type parameters of the class sol.
  */
  private static boolean checkClassConstraint(Element var, 
                                              TypeConstructor sol)
  {
    bossa.syntax.ClassDefinition def = bossa.syntax.ClassDefinition.get(sol);
    
    if (def == null)
      return true;

    Constraint constraint = def.getResolvedConstraint();

    if (constraint == mlsub.typing.Constraint.True)
      return true;

    if (! (var instanceof Monotype))
      return true;

    Monotype m = (Monotype) var;

    MonotypeConstructor type = 
      new MonotypeConstructor(sol, def.getTypeParameters());

    try {
      constraint.enter();
      Engine.leq(type, var);

      return true;
    }
    catch (TypingEx ex) {
      return false;
    }
    catch (Unsatisfiable ex) {
      return false;
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
    private boolean first;
    final List tags = new ArrayList();

    /** Call before enumeration of the possibilities for some tags */
    void startAddition()
    {
      size = tags.size();
      first = true;
    }

    // return true if there is no solution
    boolean endAddition()
    {
      return first; 
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
