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
import mlsub.typing.Monotype;
import mlsub.typing.Constraint;

/**
   <tt>return</tt> in a function or method.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class ReturnStmt extends Statement
{
  public ReturnStmt(Expression value)
  {
    this.value = value;
  }

  Polytype returnType()
  {
    if (value == null)
      return ConstantExp.voidPolytype;
    else
      return value.getType();
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression generateCode()
  {
    if (value == null)
      return new gnu.expr.ExitExp(includingFunction.getBlock());
    else
      return new gnu.expr.ExitExp(value.generateCode(),
				  includingFunction.getBlock());
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    return "return" + (value!=null ? " " + value : "");
  }
  
  Expression value;
  Function includingFunction;
}
