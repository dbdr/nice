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
    this.to = to;
    this.value = value;
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
	
	return CallExp.create
	  (new IdentExp(new LocatedString("set", call.function.location())),
	   call.arguments.getExp(0),
	   call.arguments.getExp(1),
	   value);
      }
    
    return new AssignExp(to, value);
  }

  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  void computeType()
  {
    this.type = value.getType();
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
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
