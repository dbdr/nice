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
import nice.tools.code.*;

/**
   Postfix or infix incrementation or decrementation of a numeric variable.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class IncrementExp extends Expression
{
  public IncrementExp(Expression var, boolean prefix, boolean increment)
  {
    this.var = expChild(var);
    this.returnOld = !prefix;
    this.increment = increment;
  }

  void computeType()
  {
    this.type = var.getType();
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    var = var.noOverloading();
    if(!var.isAssignable())
      User.error(this, var + " cannot be incremented");

    gnu.expr.Declaration decl = var.getDeclaration();
    if (decl != null)
      return new gnu.expr.IncrementExp
	(decl, (short) (increment ? 1 : -1), !returnOld);

    // var is not a local variable, so it must be a field
    CallExp call = null;
    if (var instanceof CallExp)
      call = (CallExp) var;
    else if (var instanceof ExpressionRef)
      {
	Expression e = ((ExpressionRef) var).content();
	if (e instanceof CallExp)
	  call = (CallExp) e;
      }
    
    if (call == null)
      Internal.error(this, "\"var\" is assignable and not a local, " +
		     "so it should be a call to a FieldAccessMethod");

    FieldAccessMethod access = call.fun.getFieldAccessMethod();
    if (access == null)
      Internal.error(this, "\"var\" is assignable and not a local, " +
		     "so it should be a call to a FieldAccessMethod");

    return Inline.inline
      (new IncrementProc(access.field(), returnOld, increment),
       call.arguments.getExp(0).generateCode());
  }

  /****************************************************************
   * Misc.
   ****************************************************************/
  
  public String toString()
  {
    if (returnOld)
      return var.toString() + (increment ? "++" : "--");
    else
      return (increment ? "++" : "--") + var.toString();
  }

  private Expression var;
  private boolean returnOld, increment;
}
