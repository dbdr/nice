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
    public Constraint(MonotypeVar[] typeParameters, List atoms)
    {
      this.typeParameters = typeParameters;
      this.atoms = atoms;
    }

    MonotypeVar[] typeParameters;
    List atoms;
  }

  final Constraint classConstraint;
  mlsub.typing.AtomicConstraint[] resolvedConstraints;

  void resolve()
  {
    if (classConstraint != null)
      {
	TypeScope scope = new TypeScope(this.typeScope);
	try { scope.addSymbols(classConstraint.typeParameters); }
	catch(TypeScope.DuplicateName ex) {}
	resolvedConstraints = 
	  AtomicConstraint.resolve(scope, classConstraint.atoms);
      }
  }

  mlsub.typing.MonotypeVar[] getTypeParameters ()
  {
    if (classConstraint == null)
      return null;
    else
      return classConstraint.typeParameters;
  }

  mlsub.typing.Constraint getResolvedConstraint()
  {
    if (classConstraint == null)
      return mlsub.typing.Constraint.True;
    else
      return new  mlsub.typing.Constraint
	(classConstraint.typeParameters, resolvedConstraints);
  }

  /****************************************************************
   * Module Interface
   ****************************************************************/

  String printTypeParameters()
  {
    if (classConstraint == null)
      return "";

    MonotypeVar[] typeParameters = classConstraint.typeParameters;
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
	res.append(typeParameters[n].toString());
	if (n + 1 < typeParameters.length)
	  res.append(", ");
      }
    return res.append(">").toString();
  }
}
