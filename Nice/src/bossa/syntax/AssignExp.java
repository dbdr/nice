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
import mlsub.typing.*;
import mlsub.typing.Polytype;

import bossa.util.Debug;

/**
   Assignment.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
public class AssignExp extends Expression
{
  private AssignExp(Expression to, Expression value)
  {
    this.to = expChild(to);
    this.value = expChild(value);
  }

  /**
     Create the assignment expression, applying rewrite rules first. 
  */
  public static Expression create(Expression to, Expression value)
  {
    // Rewrite "get(e, i) = v" into "set(e, i, v)"
    if (to instanceof CallExp &&
	"get".equals(((CallExp) to).function.toString()))
      {
	CallExp call = (CallExp) to;
	((IdentExp) call.function).ident.content = "set";
	call.arguments.add(value);

	return call;
      }
    
    return new AssignExp(to, value);
  }

  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  /**
     Checks that right can be assigned to a variable of type left.
     Returns a new expression to be used instead of right,
     since overloading resolution is done on the expected type.
  */
  static Expression checkAssignment(Polytype left, Expression right)
    throws TypingEx
  {
    Expression val = right.resolveOverloading(left);
    Typing.leq(val.getType(), left);
    return val;
  }
  
  void computeType()
  {
    this.type = value.getType();
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    to = to.noOverloading();
    return to.compileAssign(value.generateCode());
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return to + " = " + value;
  }

  Expression to;
  Expression value;
}
