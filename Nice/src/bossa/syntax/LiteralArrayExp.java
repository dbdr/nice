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

import bossa.util.*;
import java.util.*;

import gnu.bytecode.*;

import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.Polytype;
import mlsub.typing.TypeConstructor;
import nice.tools.code.Types;

/**
   Creates an array containing the given elements.

   The array type is infered as the minimal possible type.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class LiteralArrayExp extends Expression
{
  public LiteralArrayExp(List /* of Expression */ elements)
  {
    this.elements = toArray(elements);
  }

  void computeType()
  {
    Polytype elementType = Polytype.union(getType(elements));

    type = array(elementType);

    // If the type cannot be simplified, it must be because elements
    // have incomparable types. In this case, we give the array the type 
    // Object[].
    if (! type.trySimplify())
      type = array(PrimitiveType.objectPolytype());
  }

  private Polytype array(Polytype elementType)
  {
    Polytype res = new Polytype
      (elementType.getConstraint(), 
       bossa.syntax.Monotype.sure(new MonotypeConstructor
	 (PrimitiveType.arrayTC, new Monotype[]{elementType.getMonotype()})));
    res.setNotSimplified();
    return res;
  }

  /**
     Adjust the array type according to the context.

     This is usefull because arrays are non-variant.
     For instance, different code must be generated 
     for [ 1, 2 ] in the contexts:
     List<int[]> i = [[ 1, 2 ]]
     and 
     List<List<byte[]>> b = [[ 1, 2 ]]
  */
  bossa.syntax.Expression resolveOverloading(Polytype expectedType)
  {
    Monotype elementType = Types.getTypeParameter(expectedType, 0);

    if (elementType != null)
      for (int i = 0; i < elements.length; i++)
        elements[i].adjustToExpectedType(elementType);

    return this;
  }

  void adjustToExpectedType(Monotype expectedType)
  {
    TypeConstructor tc = Types.equivalent(expectedType).head();

    // Remember that we will need to wrap the array to make it a collection.
    // This cannot be found easily during code generation for nested arrays
    // since the bytecode type of both List<List<T>> and List<T[]> is
    // simply List.
    if (tc != PrimitiveType.arrayTC &&
        tc != null && tc.isRigid() &&
        mlsub.typing.Typing.testRigidLeq(tc, PrimitiveType.collectionTC))
      {
        wrapAsCollection = true;
      }

    // Adjust nested elements.
    Monotype elementType = Types.getTypeParameter(expectedType, 0);
    if (elementType != null)
      for (int i = 0; i < elements.length; i++)
        elements[i].adjustToExpectedType(elementType);    
  }

  private boolean wrapAsCollection;

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    gnu.expr.Expression[] args = Expression.compile(elements);
    ArrayType t = (ArrayType) nice.tools.code.SpecialTypes.array(Types.lowestUpperBound(args));

    return new gnu.expr.ApplyExp
      (new nice.tools.code.LiteralArrayProc
         (t, elements.length, wrapAsCollection),
       args);
  }
  
  public String toString()
  {
    return "[" + Util.map("", ", ", "", elements) + "]";
  }

  Expression[] elements;
}
