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

  void computeType()
  {
    Polytype elementType = Polytype.union(getType(elements));
    
    this.type = new Polytype
      (elementType.getConstraint(), 
       bossa.syntax.Monotype.sure(new MonotypeConstructor
	 (ConstantExp.arrayTC, new Monotype[]{elementType.getMonotype()})));

    nice.tools.code.Types.setBytecodeType(this.type);
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
