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
// Created : Fri Jun 02 17:02:45 2000 by Daniel Bonniot
//$Modified: Thu Aug 31 11:14:10 2000 by Daniel Bonniot $

package mlsub.typing;

import mlsub.typing.lowlevel.*;

import java.util.Map;

/**
 * Atomic constraint.
 * 
 * @author Daniel Bonniot
 */

public final class Constraint
{
  public Constraint(TypeSymbol[] binders,
		    AtomicConstraint[] atoms)
  {
    this.binders = binders;
    if(binders!=null)
      nbinders = binders.length;
    
    this.atoms = atoms;
    if(atoms!=null)
      natoms = atoms.length;
  }

  /** Ensures that null is returned if there are no binders and atoms */
  public final static Constraint create(TypeSymbol[] binders,
					AtomicConstraint[] atoms)
  {
    if(binders==null && atoms==null)
      return True;
    if(binders!=null && binders.length==0)
      throw new Error("Please optimize");
    if(atoms!=null && atoms.length==0)
      throw new Error("Please optimize");
    
    return new Constraint(binders, atoms);
  }

  /**
     Empty constraint. private use only. 
  */
  private Constraint()
  {
  }

  public static final Constraint True = null;
  
  public static final boolean hasBinders(Constraint c)
  {
    return c!=null && c.binders!=null;
  }

  public TypeSymbol[] binders()
  {
    return binders;
  }

  public AtomicConstraint[] atoms()
  {
    return atoms;
  }

  /****************************************************************
   * Manipulation
   ****************************************************************/

  public static Constraint and(Constraint c1, Constraint c2)
  {
    final int
      nbinders1 = (c1 == null ? 0 : c1.nbinders),
      nbinders2 = (c2 == null ? 0 : c2.nbinders),
      natoms1   = (c1 == null ? 0 : c1.natoms),
      natoms2   = (c2 == null ? 0 : c2.natoms);
    
    if(nbinders1==0 && nbinders2==0 && natoms1==0 && natoms2==0)
      return True;
    
    Constraint res = new Constraint();
    res.nbinders = nbinders1 + nbinders2;
    res.natoms = natoms1 + natoms2;

    if(res.nbinders>0)
      {
	res.binders = new TypeSymbol[res.nbinders];
	for(int i=0; i<nbinders1; i++)
	  res.binders[i] = c1.binders[i];
	for(int i=0; i<nbinders2; i++)
	  res.binders[nbinders1 + i] = c2.binders[i];
      }
    
    if(res.natoms>0)
      {
	res.atoms = new AtomicConstraint[res.natoms];
	for(int i=0; i<natoms1; i++)
	  res.atoms[i] = c1.atoms[i];
	for(int i=0; i<natoms2; i++)
	  res.atoms[natoms1 + i] = c2.atoms[i];
      }
    
    return res;
  }
  
  public static Constraint and(Constraint c1, 
			       TypeSymbol t0, TypeSymbol t1,
			       AtomicConstraint a1, AtomicConstraint a2)
  {
    final int
      nbinders = (c1 == null ? 0 : c1.nbinders) + 2,
      natoms   = (c1 == null ? 0 : c1.natoms)   + 2;
    
    Constraint res = new Constraint();
    res.nbinders = nbinders;
    res.natoms = natoms;

    res.binders = new TypeSymbol[nbinders];
    for(int i=0; i<nbinders - 2; i++)
      res.binders[i] = c1.binders[i];
    res.binders[nbinders - 2] = t0;
    res.binders[nbinders - 1] = t1;
    
    res.atoms = new AtomicConstraint[res.natoms];
    for(int i=0; i < natoms - 2; i++)
      res.atoms[i] = c1.atoms[i];
    res.atoms[natoms - 2] = a2;
    res.atoms[natoms - 1] = a1;
    
    return res;
  }
  
  static Constraint and(TypeSymbol m, Constraint c1, Constraint c2,
			AtomicConstraint a1, AtomicConstraint a2)
  {
    Constraint res = new Constraint();

    final int
      nbinders1 = (c1 == null ? 0 : c1.nbinders),
      nbinders2 = (c2 == null ? 0 : c2.nbinders),
      natoms1   = (c1 == null ? 0 : c1.natoms),
      natoms2   = (c2 == null ? 0 : c2.natoms);
    
    res.nbinders = nbinders1 + nbinders2 + 1;
    res.binders = new TypeSymbol[res.nbinders];
    for(int i=0; i<nbinders1; i++)
      res.binders[i] = c1.binders[i];
    for(int i=0; i<nbinders2; i++)
      res.binders[nbinders1 + i] = c2.binders[i];
    res.binders[res.nbinders-1] = m;
    
    res.natoms = natoms1 + natoms2 + 2;
    res.atoms = new AtomicConstraint[res.natoms];
    for(int i=0; i<natoms1; i++)
      res.atoms[i] = c1.atoms[i];
    for(int i=0; i<natoms2; i++)
      res.atoms[natoms1+i] = c2.atoms[i];
    res.atoms[res.natoms-2] = a2;
    res.atoms[res.natoms-1] = a1;

    return res;
  }
  
  static Constraint and(Constraint c1, Constraint c2, AtomicConstraint a1)
  {
    Constraint res = new Constraint();

    final int
      nbinders1 = (c1 == null ? 0 : c1.nbinders),
      nbinders2 = (c2 == null ? 0 : c2.nbinders),
      natoms1   = (c1 == null ? 0 : c1.natoms),
      natoms2   = (c2 == null ? 0 : c2.natoms);
    
    res.nbinders = nbinders1 + nbinders2;
    res.binders = new TypeSymbol[res.nbinders];
    for(int i=0; i<nbinders1; i++)
      res.binders[i] = c1.binders[i];
    for(int i=0; i<nbinders2; i++)
      res.binders[nbinders1 + i] = c2.binders[i];
    
    res.natoms = natoms1 + natoms2 + 1;
    res.atoms = new AtomicConstraint[res.natoms];
    for(int i=0; i<natoms1; i++)
      res.atoms[i] = c1.atoms[i];
    for(int i=0; i<natoms2; i++)
      res.atoms[natoms1+i] = c2.atoms[i];
    res.atoms[res.natoms-1] = a1;

    return res;
  }
  
  /**
     Conjunction of the given constraints.
     
     Leaves the parameters unmodified.
  */
  public static Constraint and(Constraint[] cs, Constraint c1, Constraint c2)
  {
    final int
      nbinders1 = (c1 == null ? 0 : c1.nbinders),
      nbinders2 = (c2 == null ? 0 : c2.nbinders),
      natoms1   = (c1 == null ? 0 : c1.natoms),
      natoms2   = (c2 == null ? 0 : c2.natoms);

    int lenBinders = nbinders1 + nbinders2;
    int lenAtoms = natoms1 + natoms2;
    
    for(int i=0; i<cs.length; i++)
      {
	Constraint c = cs[i];
	if (c == True)
	  continue;
	
	lenBinders += cs[i].nbinders;
	lenAtoms += cs[i].natoms;
      }

    if (lenAtoms==0 && lenBinders==0)
      return True;

    Constraint res = new Constraint();
    if(lenBinders>0)
      {
	res.nbinders = lenBinders;
	res.binders = new TypeSymbol[lenBinders];
	for(int i=0; i<nbinders2; i++)
	  res.binders[--lenBinders] = c2.binders[i];
	for(int i=0; i<nbinders1; i++)
	  res.binders[--lenBinders] = c1.binders[i];
      }
    
    if(lenAtoms>0)
      {
	res.natoms = lenAtoms;
	res.atoms = new AtomicConstraint[lenAtoms];
	for(int i=0; i<natoms2; i++)
	  res.atoms[--lenAtoms] = c2.atoms[i];
	for(int i=0; i<natoms1; i++)
	  res.atoms[--lenAtoms] = c1.atoms[i];
      }
    
    for(int j=0; j<cs.length; j++)
      {
	Constraint c = cs[j];
	if (c == True)
	  continue;
	
	for(int i=0; i<c.nbinders; i++)
	  res.binders[--lenBinders] = c.binders[i];

	for(int i=0; i<c.natoms; i++)
	  res.atoms[--lenAtoms] = c.atoms[i];
      }
    
    return res;
  }
  
  /****************************************************************
   * Typechecking
   ****************************************************************/

  public static void enter(Constraint c)
    throws TypingEx
  {
    if( c != null)
      c.enter();
  }
  
  public void enter()
    throws TypingEx
  {
    if (binders != null) 
      Typing.introduceTypeSymbols(binders);

    if (atoms != null)
      for (int i=0; i<natoms; i++)
	atoms[i].enter();
  }
  
  public void enter(boolean existential)
    throws TypingEx
  {
    if (existential && binders != null)
      for(int i=0; i < nbinders; i++)
        if (binders[i] instanceof MonotypeVar)
          ((MonotypeVar) binders[i]).setExistential();

    enter();
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    StringBuffer res = new StringBuffer("<");
    for(int i=0; i<nbinders; i++)
      {
	res.append(binders[i].toString());
	if(i<nbinders-1) res.append(", ");
      }
    if(nbinders>0 && natoms>0)
      res.append(" | ");
    for(int i=0; i<natoms; i++)
      {
	res.append(String.valueOf(atoms[i]));
	if(i<natoms-1) res.append(", ");
      }
    res.append("> ");
    return res.toString();
  }
  
  public static String toString(Constraint c)
  {
    if (c == True)
      return "";
    else
      return c.toString();
  }
  
  private TypeSymbol[] binders;
  /** length of the binders array */
  private int nbinders;
  
  private AtomicConstraint[] atoms;
  /** length of the atoms array */
  private int natoms;
}
