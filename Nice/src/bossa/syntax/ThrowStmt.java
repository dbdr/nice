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
import mlsub.typing.MonotypeConstructor;

/**
   Throw statement.
 
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class ThrowStmt extends Statement
{
  public ThrowStmt(Expression e)
  {
    this.exn = expChild(e);
  }

  public gnu.expr.Expression generateCode()
  {
    return new gnu.expr.ThrowExp(exn.generateCode());
  }
  
  public String toString()
  {
    return "throw " + exn;
  }

  Expression exn;
}
