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

import mlsub.typing.Variance;
import mlsub.typing.MonotypeVar;
import mlsub.typing.TypeSymbol;
import mlsub.typing.TypeConstructor;
import bossa.util.User;
import java.util.List;

/**
   An entity in which methods can be declared.

   It can have type parameters, that are implicitely added 
   to the englobed methods.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
*/

public abstract class MethodContainer extends Definition
{
  MethodContainer (LocatedString name, int propagate, 
		   MethodContainer.Constraint classConstraint, 
		   List variance)
  {
    super(name, propagate);

    this.name.prepend(module.getName() + ".");
    
    this.variance = makeVariance(variance);
    this.classConstraint = classConstraint;
  }

  /** The name of this method container without package qualification. */
  String getSimpleName()
  {
    String name = getName().toString();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  mlsub.typing.Variance variance;

  abstract mlsub.typing.TypeSymbol getTypeSymbol();

  private static Variance makeVariance(java.util.List typeParametersVariances)
  {
    int[] variances = new int[typeParametersVariances.size()];
    for (int i = typeParametersVariances.size(); --i >= 0;)
      if (typeParametersVariances.get(i) != null)
	if (typeParametersVariances.get(i) == Boolean.TRUE)
	  variances[i] = Variance.COVARIANT;
	else
	  variances[i] = Variance.CONTRAVARIANT;

    return Variance.make(variances);
  }

  /****************************************************************
   * Class type parameters
   ****************************************************************/

  public static class Constraint
  {
    /**
       @param cst an additional constraint for the type parameters
                  of this class.
    */
    public Constraint(bossa.syntax.Constraint cst, MonotypeVar[] typeParameters, List atoms)
    {
      if (cst == bossa.syntax.Constraint.True || cst == null) 
        {
          this.binders = typeParameters;
          this.typeParameters = typeParameters;
          this.atoms = atoms;
        }
      else
        {
          this.syntacticConstraint = cst.toString();
          this.atoms = cst.getAtoms();
          this.binders = (TypeSymbol[]) cst.getBinders().
            toArray(new TypeSymbol[cst.getBinders().size()]);
          findBinders(typeParameters);
        }
    }

    /**
       Replace those type parameters that have been introduced in the 
       constraint by their definition.
    */
    private void findBinders(MonotypeVar[] typeParameters)
    {
      this.typeParameters = new mlsub.typing.Monotype[typeParameters.length];
      for (int i = 0; i < typeParameters.length; i++) 
        {
          this.typeParameters[i] = findBinder(typeParameters[i]);
        }
    }

    private mlsub.typing.Monotype findBinder(MonotypeVar binder)
    {
      for (int i = 0; i < binders.length; i++) 
        if (binders[i].toString().equals(binder.getName()))
          {
            if (binders[i] instanceof MonotypeVar)
              return (MonotypeVar) binders[i];
            else
              // The type parameter must be in fact a type constructor
              // of variance zero.
              return new mlsub.typing.MonotypeConstructor
                ((TypeConstructor) binders[i], null);
          }

      // Not found. It was not introduced earlier, use it as the binder.
      return binder;
    }

    /** The binders of the constraint. */
    TypeSymbol[] binders;

    /** The type parameters of the class. */
    mlsub.typing.Monotype[] typeParameters;

    List atoms;

    String syntacticConstraint;
  }

  final Constraint classConstraint;
  mlsub.typing.AtomicConstraint[] resolvedConstraints;

  void resolve()
  {
    if (classConstraint != null)
      {
	TypeScope scope = new TypeScope(this.typeScope);
	try { scope.addSymbols(classConstraint.binders); }
	catch(TypeScope.DuplicateName ex) {}
	resolvedConstraints = 
	  AtomicConstraint.resolve(scope, classConstraint.atoms);
      }
  }

  /** The binders of the constraint. */
  public mlsub.typing.TypeSymbol[] getBinders ()
  {
    if (classConstraint == null)
      return null;
    else
      return classConstraint.binders;
  }

  /** The type parameters of the class. */
  public mlsub.typing.Monotype[] getTypeParameters ()
  {
    if (classConstraint == null)
      return null;
    else
      return classConstraint.typeParameters;
  }

  public mlsub.typing.Constraint getResolvedConstraint()
  {
    if (classConstraint == null)
      return mlsub.typing.Constraint.True;
    else
      return new mlsub.typing.Constraint
	(classConstraint.binders, resolvedConstraints);
  }

  /****************************************************************
   * Module Interface
   ****************************************************************/

  /**
     Children should call this implementation (super) then print
     their specific information. 
   */
  public void printInterface(java.io.PrintWriter s)
  {
    // print the constraint as a prefix constraint
    if (classConstraint == null || classConstraint.syntacticConstraint == null)
      return;

    s.print(classConstraint.syntacticConstraint);
  }

  String printTypeParameters()
  {
    if (classConstraint == null)
      return "";

    mlsub.typing.Monotype[] typeParameters = classConstraint.typeParameters;
    StringBuffer res = new StringBuffer("<");
    for (int n = 0; n < typeParameters.length; n++)
      {
	switch (variance.getVariance(n)) 
	  {
	  case Variance.CONTRAVARIANT: 
	    res.append("-");
	    break;
	  case Variance.COVARIANT: 
	    res.append("+");
	    break;
	  }

	if (resolvedConstraints != null)
	for (int i = 0; i < resolvedConstraints.length; i++)
	  if (resolvedConstraints[i] instanceof mlsub.typing.MonotypeLeqTcCst)
	    {
	      mlsub.typing.MonotypeLeqTcCst cst = (mlsub.typing.MonotypeLeqTcCst) resolvedConstraints[i];
	      if (cst.m == typeParameters[n]) {
		res.append('!');
		break;
	      }
	    }

	res.append(typeParameters[n].toString());
	if (n + 1 < typeParameters.length)
	  res.append(", ");
      }

    if (resolvedConstraints != null) {
      boolean first = true;
      for (int n = 0; n < resolvedConstraints.length; n++)
	{
	  if (resolvedConstraints[n] instanceof mlsub.typing.MonotypeLeqTcCst)
	    break;
	  if (first) {
	    res.append(" | ");
	    first = false;
	  }
	  res.append(resolvedConstraints[n]);
	  if (n + 1 < resolvedConstraints.length)
	    res.append(", ");
	}
    }

    return res.append(">").toString();
  }
}
