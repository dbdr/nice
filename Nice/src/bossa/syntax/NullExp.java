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

import mlsub.typing.Polytype;
import mlsub.typing.MonotypeVar;
import mlsub.typing.TypeSymbol;
import mlsub.typing.Constraint;

/**
   The 'null' expression.   

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class NullExp extends Expression
{
  /** There is only one instance of NullExp,
      since it has no state.
  */
  public static final NullExp instance = new NullExp();
  
  private NullExp() {}

  void computeType()
  {
    //this.type = mlsub.typing.Polytype.bottom();
    MonotypeVar alpha = new MonotypeVar();
    this.type = new Polytype(new Constraint(new TypeSymbol[]{alpha}, null), 
			     bossa.syntax.Monotype.maybe(alpha));
  }
  
  protected gnu.expr.Expression compile()
  {
    return gnu.expr.QuoteExp.nullExp;
  }
  
  public String toString()
  {
    return "null";
  }
}
