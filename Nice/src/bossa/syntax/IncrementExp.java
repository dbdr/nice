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
  public IncrementExp(Expression variable, boolean prefix, boolean increment)
  {
    this.variable = variable;
    this.returnOld = !prefix;
    this.increment = increment;
  }

  void computeType()
  {
    this.type = variable.getType();
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    variable = variable.noOverloading();
    if(!variable.isAssignable())
      User.error(this, variable + " cannot be incremented");

    gnu.expr.Declaration decl = variable.getDeclaration();
    if (decl != null)
      return new gnu.expr.IncrementExp
	(decl, (short) (increment ? 1 : -1), !returnOld);

    // variable is not a local variable, so it must be a field
    CallExp call = null;
    if (variable instanceof CallExp)
      call = (CallExp) variable;
    
    if (call == null)
      Internal.error(this, "\"var\" is assignable and not a local, " +
		     "so it should be a call to a FieldAccessMethod");

    FieldAccess access = call.function.getFieldAccessMethod();
    if (access == null)
      Internal.error(this, "\"var\" is assignable and not a local, " +
		     "so it should be a call to a FieldAccessMethod");

    return Inline.inline
      (new IncrementProc(access.field, returnOld, increment),
       call.arguments.getExp(0).generateCode());
  }

  /****************************************************************
   * Misc.
   ****************************************************************/
  
  public String toString()
  {
    if (returnOld)
      return variable.toString() + (increment ? "++" : "--");
    else
      return (increment ? "++" : "--") + variable.toString();
  }

  String description()
  {
    return increment ? "Incrementation" : "Decrementation";
  }

  Expression variable;
  private boolean returnOld, increment;
}
