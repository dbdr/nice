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
	"get".equals(((CallExp) to).fun.toString()))
      {
	CallExp call = (CallExp) to;
	((IdentExp) ((ExpressionRef) call.fun).content()).ident.content = "set";
	call.arguments.add(value);

	return call;
      }
    
    return new AssignExp(to, value);
  }

  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  static void checkAssignment(Polytype left, ExpressionRef right)
    throws TypingEx
  {
    right.resolveOverloading(left);
    Typing.leq(right.getType(), left);
  }
  
  void typecheck()
  {
    to = to.noOverloading();

    if(!to.isAssignable())
      User.error(this, to + " cannot be assigned a value");
    
    try{
      checkAssignment(to.getType(), value);
    }
    catch(TypingEx t){
      User.error(this,
		 "Typing error : " + to + 
		 " cannot be assigned value " + value + 
		 " of type " + value.getType(),
		 "\n" + t.getMessage());
    }
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

  private Expression to;
  private ExpressionRef value;
}
