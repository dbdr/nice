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

// File    : Constraint.java
// Created : Fri Jul 02 17:51:35 1999 by bonniot
//$Modified: Tue Jan 25 15:59:34 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A constraint between type constructors
 *
 * @see AtomicConstraint
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
  
  Constraint(MonotypeVar binder, List atomics)
  {
    super(Node.upper);
    List binders=new ArrayList(1);
    binders.add(binder);
    construct(binders,atomics);
  }
      
  private void construct(List binders, List atomics)
  {
    if(binders==null)
      binders=new ArrayList(0);
    else
      addTypeSymbols(binders);
    this.binders=binders;
    this.atomics=addChildren(atomics);
  }

  boolean hasBinders()
  {
    return binders.size()>0;
  }
  
  /**
   * The trivial constraint 
   *
   * @return a constraint with no binders, always true
   */
  public static final Constraint True()
  { 
    return new Constraint(new ArrayList(0),new ArrayList(0));
  }

  /**
   * Returns a new constraint.
   * The lists are new, but the list elements are the same.
   */
  private Constraint shallowClone()
  {
    return new Constraint((List)((ArrayList)binders).clone(),
			  (List)((ArrayList)atomics).clone());
  }

  public static Constraint and(Constraint c1, Constraint c2)
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
  
  /****************************************************************
   * Scoping
   ****************************************************************/

  void resolve()
  {
    atomics=AtomicConstraint.resolve(typeScope,atomics);
  }

  Constraint substitute(Map map)
  {
    // Substitution is not done on binders.
    // This is intended for imperative type parameters substitution only.
    return new Constraint(binders, AtomicConstraint.substitute(map,atomics));
  }
  
  /****************************************************************
   * Typechecking
   ****************************************************************/

  public void assert()
    throws bossa.typing.TypingEx
  {
    // All user defined variables implicitely implement Top
    assert(true);
  }
  
  void assert(boolean implementTop)
    throws bossa.typing.TypingEx
  {
    bossa.typing.Typing.introduce(binders);
    
    if(implementTop)
      for(Iterator i=binders.iterator();i.hasNext();)
	{
	  TypeSymbol s=(TypeSymbol)i.next();
	  if(s instanceof TypeConstructor)
	    {
	      TypeConstructor t=(TypeConstructor)s;
	      if(t.variance!=null)
		bossa.typing.Typing.assertImp
		  (t,InterfaceDefinition.top(t.variance.size),false);
	      else
		Internal.warning(t+" has no known variance, so I can't assert it implement some Top<n> interface");
	    }
	  else if(s instanceof MonotypeVar)
	    ((MonotypeVar)s).rememberToImplementTop();
	}

    AtomicConstraint.assert(atomics);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    if(atomics.size()==0)
      return Util.map("<",", ",">",binders,Printable.inConstraint);
    else if(binders.size()==0)
      return Util.map("<",", ",">",atomics);
    else 
      {
	// Put in a parsable format.
	String res="<";
	boolean first=true;
	
	Constraint c=this.shallowClone();

	for(Iterator i=c.binders.iterator();i.hasNext();)
	  {
	    TypeSymbol s=(TypeSymbol)i.next();
	    if(!(s instanceof TypeConstructor))
	      continue;

	    TypeConstructor tc=(TypeConstructor)s;
	    boolean ok=false;

	    for(Iterator j=c.atomics.iterator();j.hasNext();)
	      {
		AtomicConstraint atom=(AtomicConstraint)j.next();
		
		String parent = atom.getParentFor(tc);
		if(parent!=null)
		  {
		    if(first)
		      first=false;
		    else
		      res+=",";
		    res+=parent+" "+tc;
		    j.remove();
		    i.remove();
		    ok=true;
		    break;
		  }
	      }
	    Internal.error(!ok,tc,
			   "Unable to print the constraint in a parsable form because of "+tc);
	  }
		  
	return 
	  res+Util.map(res.length()>1 ? ", " : "",", ","",c.binders,Printable.inConstraint)
	  + Util.map(" | ",", ","",c.atomics)
	  + ">";
      }
  }

  /****************************************************************
   * Internal manipulation
   ****************************************************************/

  void addBinder(TypeSymbol s)
  {
    if(!binders.contains(s))
      binders.add(s);
  }
  
  /**
   * Adds binders that are not already present
   *
   * @param b a collection of TypeSymbol
   */
  private void addBinders(Collection b)
  {
    Iterator i=b.iterator();
    while(i.hasNext())
      addBinder((TypeSymbol) i.next());
  }

  void addAtom(AtomicConstraint atom)
  {
    atomics.add(atom);
  }
  
  public List /* of TypeSymbol */ binders;
  List /* of AtomicConstraint */ atomics;
}
