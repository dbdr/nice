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
  
  private void construct(List binders, List atomics)
  {
    if(binders==null)
      binders = new ArrayList(0);
    else
      addTypeSymbols(binders);
    this.binders = binders;
    this.atomics = addChildren(atomics);
  }

  //REMOVE
//    boolean hasBinders()
//    {
//      return binders.size()>0;
//    }
  
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
  private Constraint shallowClone()
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
  
  /*public static Constraint and(Constraint c1, Constraint c2)
  {
    Constraint res=c1.shallowClone();
    res.addBinders(c2.binders);
    res.atomics.addAll(c2.atomics);
    
    return res;
  }

  Constraint and(Collection c)
  {
    Constraint res=shallowClone();
    for(Iterator i=c.iterator();
	i.hasNext();)
      res.and((Constraint) i.next());
    return res;
  }

  void and(Constraint c)
  {
    this.addBinders(c.binders);
    this.atomics.addAll(c.atomics);
  }
  /*
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
	String res = "<";
	boolean first = true;
	
	Constraint c = this.shallowClone();

	for(Iterator i = c.binders.iterator();i.hasNext();)
	  {
	    TypeSymbol s = (TypeSymbol)i.next();
	    if(!(s instanceof TypeConstructor))
	      continue;

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
		      res += ",";
		    res += parent + " " + tc;
		    j.remove();
		    i.remove();
		    ok = true;
		    break;
		  }
		Debug.println(atom+"");
	      }
	    if(!ok)
	      Internal.error("Unable to print the constraint in a parsable form because of "+tc);
	  }
	
	return
	  res + 
	  Util.map((res.length()>1 ? ", " : "") + "Any ",", Any ","", 
		   c.binders) +
	  Util.map(" | ",", ","",c.atomics) + 
	  "> ";
      }
  }

  /****************************************************************
   * Internal manipulation
   ****************************************************************/

  /**
     Add the binder if it is not already there.
  */
  void addBinder(TypeSymbol s)
  {
    if(!binders.contains(s))
      {
	binders.add(0, s); // MethodDefinition assumes it is added first. 
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
  
  public List /* of TypeSymbol */ binders;
  List /* of AtomicConstraint */ atomics;
}
