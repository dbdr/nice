/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

/**
   The empty statement.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

import bossa.util.*;

public class EmptyStmt extends Statement
{
  public EmptyStmt (Location location)
  {
    super(location);
  }

  gnu.expr.Expression generateCode()
  {
    return gnu.expr.QuoteExp.voidExp;
  }
}
