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

/**
   Abstract ancestor for all statements.
   Descendants have "Stmt" suffix.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public abstract class Statement
  implements Located
{
  /****************************************************************
   * Code generation
   ****************************************************************/

  abstract gnu.expr.Expression generateCode();

  static gnu.expr.Expression[] compile(Statement[] statements)
  {
    gnu.expr.Expression[] res = new gnu.expr.Expression[statements.length];
    for (int i = 0; i < statements.length; i++)
      {
	Statement s = statements[i];

	if (s == null)
	  {
	    res[i] = gnu.expr.QuoteExp.voidExp;
	    continue;
	  }

	try {
	  res[i] = s.generateCode();
	}
	catch(UserError e) {
	  // Make sure that the error is attached to a location.
	  // If not, it's better than nothing to located the error in
	  // the containing statement.
	  if (e.responsible == null)
	    e.responsible = s;
	  // Rethrow.
	  throw e;
	}

	if (s.location() != null)
	  s.location().write(res[i]);
      }
    return res;
  }

  static gnu.expr.Expression sequence(gnu.expr.Expression e1, gnu.expr.Expression e2)
  {
    gnu.expr.Expression[] array=new gnu.expr.Expression[2];
    array[0]=e1;
    array[1]=e2;
    return new gnu.expr.BeginExp(array);
  }
  
  /****************************************************************
   * Location
   ****************************************************************/

  public Location location()
  {
    return loc;
  }

  public void setLocation(Location l)
  {
    loc=l;
  }

  Location loc;
}
