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
import mlsub.typing.MonotypeVar;
import mlsub.typing.Polytype;
import mlsub.typing.Constraint;
import mlsub.typing.TypeConstructor;
import mlsub.typing.TypeSymbol;
import mlsub.typing.Typing;
import mlsub.typing.TypingEx;

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

  /**
     Adjust the array type according to the context.

     This is usefull because arrays are non-variant.
     For instance, different code must be generated 
     for [ 1, 2 ] in the contexts:
     int[] i = [ 1, 2 ]
     and 
     byte[] b = [ 1, 2 ]
  */
  Expression resolveOverloading(Polytype expectedType)
  {
    // This can only help
    expectedType.simplify();

    Monotype m = expectedType.getMonotype();
    // get rid of the nullness part
    m = ((MonotypeConstructor) m).getTP()[0];

    // Look for the element type

    if (!(m instanceof MonotypeConstructor))
      return this;

    MonotypeConstructor mc = (MonotypeConstructor) m;
    if (!(mc.getTC() == ConstantExp.arrayTC))
      // there must be an error, but it shall be discovered elsewhere
      return this;
    
    // Remember the required element type, to take it as ours if possible.
    // This is done in computeType
    Monotype elementMonotype = mc.getTP()[0];
    expectedElementType = new Polytype(expectedType.getConstraint(), 
				       elementMonotype);

    return this;
  }

  private Polytype expectedElementType;

  void computeType()
  {
    Polytype elementType = Polytype.union(getType(elements));
    
    if (expectedElementType == null)
      elementType.simplify();
    else
      /* The context requires a certain element type.
	 First check that it is compatible with the actual elements,
	 then take it as the element type.
      */
      {
	try{
	  Typing.leq(elementType, expectedElementType);
	  elementType = expectedElementType;
	} catch(TypingEx ex) {
	  // this is an error, but it shall be reported elsewhere
	  // so keep the computed type
	}
      }
    this.type = new Polytype
      (elementType.getConstraint(), 
       bossa.syntax.Monotype.sure(new MonotypeConstructor
	 (ConstantExp.arrayTC, new Monotype[]{elementType.getMonotype()})));
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    Type t = nice.tools.code.Types.javaType(type);
    
    return new gnu.expr.ApplyExp
      (new nice.tools.code.LiteralArrayProc((ArrayType) t, 
					    elements.length),
       Expression.compile(elements));
  }
  
  public String toString()
  {
    return "[" + Util.map("", ", ", "", elements) + "]";
  }

  Expression[] elements;
}
