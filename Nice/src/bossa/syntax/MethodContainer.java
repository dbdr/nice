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
		   java.util.List typeParameters, java.util.List variance)
  {
    super(name, propagate);

    this.name.prepend(module.getName() + ".");
    
    this.variance = makeVariance(variance);

    if (typeParameters==null || typeParameters.size() == 0)
      this.typeParameters = null;
    else
      this.typeParameters = (MonotypeVar[]) 
	typeParameters.toArray(new MonotypeVar[typeParameters.size()]);
  }

  /** The name of this method container without package qualification. */
  String getSimpleName()
  {
    String name = getName().toString();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  final MonotypeVar[] typeParameters;
  mlsub.typing.Variance variance;

  abstract mlsub.typing.TypeSymbol getTypeSymbol();

  /** Create type parameters with the same names as in this entity. */
  mlsub.typing.MonotypeVar[] createSameTypeParameters ()
  {
    if (typeParameters == null)
      return null;
    
    mlsub.typing.MonotypeVar[] thisTypeParams = 
      new mlsub.typing.MonotypeVar[typeParameters.length];
    for(int i=0; i<thisTypeParams.length; i++)
      thisTypeParams[i] = new MonotypeVar(typeParameters[i].toString());
    return thisTypeParams;
  }

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
}
