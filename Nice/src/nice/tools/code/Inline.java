/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : Inline.java
// Created : Tue Jul 25 12:26:52 2000 by Daniel Bonniot
//$Modified: Tue Aug 29 10:24:25 2000 by Daniel Bonniot $

package nice.tools.code;

import gnu.expr.Expression;
import gnu.expr.ApplyExp;
import gnu.mapping.*;

/**
   Static class to inline code written in <code>Procedure</code>s.
   
   @author Daniel Bonniot
 */

public final class Inline
{
  public static Expression inline(Procedure1 proc, Expression arg1)
  {
    return new ApplyExp(proc, new Expression[]{ arg1 });
  }

  public static Expression inline(Procedure2 proc, 
				  Expression arg1, Expression arg2)
  {
    return new ApplyExp(proc, new Expression[]{ arg1, arg2 });
  }
}
