/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import mlsub.typing.TypeSymbol;
import mlsub.typing.TypeConstructor;
import mlsub.typing.MonotypeVar;

/**
   A list of binders + atomic constraints.
   
   @see AtomicConstraint

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
public class Constraint extends Node
{
  /**
   * Creates the constraint \forall binders . atomics
   *
   * @param binders a collection of TypeSymbols
   * @param atomics a collection of AtomicConstraints
   */
  public Constraint(List binders, List atomics)
  {
    super(Node.upper);
    construct(binders,atomics);
  }
  
  public Constraint(TypeSymbol[] binders, List atomics)
  {
    super(Node.upper);
    construct(arrayToList(binders),atomics);
  }

  static Constraint create(TypeSymbol[] binders)
  {
    if (binders == null)
      return True;
    else
      return new Constraint(binders, null);
  }

  private List arrayToList(Object[] a)
  {
    if (a == null || a.length == 0)
      return null;

    List res = new ArrayList(a.length);
    for (int i = 0; i < a.length; i++)
      res.add(a[i]);
    return res;
  }

  private void construct(List binders, List atomics)
  {
    if(binders==null)
      binders = noBinders;
    else
      addTypeSymbols(binders);
    this.binders = binders;
    this.atomics = addChildren(atomics);
  }

  private static final List noBinders = new ArrayList(0);

  /**
   * The trivial constraint.
   *
   * This field is final, so pointer equality can be used 
   * to test whether a constraint is True.
   *
   * @return a constraint with no binders, always true
   */
  public static final Constraint True = 
    new Constraint(new ArrayList(0),new ArrayList(0));

  /**
   * Returns a new constraint.
   * The lists are new, but the list elements are the same.
   */
  Constraint shallowClone()
  {
    return new Constraint(cloneList(binders),
			  cloneList(atomics));
  }

  private List cloneList(List l)
  {
    if(l instanceof ArrayList)
      return (List) ((ArrayList) l).clone();
    else
      return (List) ((LinkedList) l).clone();
  }
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  mlsub.typing.Constraint resolveToLowlevel()
  { 
    TypeSymbol[] newBinders = null;
    if(binders.size()>0)
      {
	newBinders = new TypeSymbol[binders.size()];
	
	int n = 0;
	for(java.util.Iterator i = binders.iterator(); i.hasNext();)
	  newBinders[n++] = (TypeSymbol) i.next();
      }

    return mlsub.typing.Constraint.create
      (newBinders, AtomicConstraint.resolve(typeScope, atomics));
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    if(atomics.size()==0)
      return Util.map("<Any ",", Any ","> ",binders);
    else if(binders.size()==0)
      return Util.map("<",", ","> ",atomics);
    else 
      {
	// Put in a parsable format.
	StringBuffer res = new StringBuffer("<");
	boolean first = true;
	
	Constraint c = this.shallowClone();

	for(Iterator i = c.binders.iterator();i.hasNext();)
	  {
	    TypeSymbol s = (TypeSymbol)i.next();
	    if(!(s instanceof TypeConstructor))
	      // Handle the ! constraint
	      {
		MonotypeVar mv = (MonotypeVar) s;
		for(Iterator j = c.atomics.iterator(); j.hasNext();)
		  {
		    AtomicConstraint atom = (AtomicConstraint)j.next();
		    
		    if (atom.isSureConstraintFor(mv))
		      {
			if(first)
			  first = false;
			else
			  res.append(',');
			res.append('!').append(s);
			j.remove();
			i.remove();
			break;
		      }
		  }
		
		continue;
	      }

	    TypeConstructor tc = (TypeConstructor)s;
	    boolean ok = false;

	    for(Iterator j = c.atomics.iterator(); j.hasNext();)
	      {
		AtomicConstraint atom = (AtomicConstraint)j.next();
		
		String parent = atom.getParentFor(tc);
		if(parent!=null)
		  {
		    if(first)
		      first = false;
		    else
		      res.append(',');
		    res.append(parent).append(' ').append(tc);
		    j.remove();
		    i.remove();
		    ok = true;
		    break;
		  }
	      }
	    if(!ok)
	      Internal.error("Unable to print the constraint in a parsable form because of "+tc);
	  }
	
	res.append(Util.map((res.length()>1 ? ", " : "") + "Any ",", Any ","", 
			    c.binders));
	res.append(Util.map(" | ",", ","",c.atomics)).append("> ");
	return res.toString();
      }
  }

  /****************************************************************
   * Internal manipulation
   ****************************************************************/

  /**
     Add the binder to the front of the list of binders.
  */
  void addFirstBinder(TypeSymbol s)
  {
    // avoid the modification of the shared empty list
    if (binders == noBinders)
      binders = new ArrayList(4);

    binders.add(0, s);
    addTypeSymbol(s);
  }

  /**
     Add the binder if it is not already there.
  */
  void addBinder(TypeSymbol s)
  {
    // avoid the modification of the shared empty list
    if (binders == noBinders)
      binders = new ArrayList(4);

    if(!binders.contains(s))
      {
	binders.add(s);
	addTypeSymbol(s);
      }
  }

  /**
   * Adds binders that are not already present
   *
   * @param b a collection of TypeSymbol
   */
  void addBinders(TypeSymbol[] bs)
  {
    if(bs==null)
      return;
    
    for(int i = 0; i<bs.length; i++)
      addBinder(bs[i]);
  }

  void addAtom(AtomicConstraint atom)
  {
    atomics.add(atom);
  }

  void addAtoms(List l)
  {
    atomics.addAll(l);
  }

  List getBinders() { return binders; }
  List getAtoms() { return atomics; }

  TypeSymbol[] getBinderArray()
  {
    return (TypeSymbol[]) binders.toArray(new TypeSymbol[binders.size()]);
  }

  private List /* of TypeSymbol */ binders;
  private List /* of AtomicConstraint */ atomics;
}
