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

// File    : IncrementExp.java
// Created : Fri Jul 21 13:58:26 2000 by Daniel Bonniot
//$Modified: Mon Jul 24 12:07:06 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * Postfix or infix incrementation or decrementation of a numeric variable.
 * 
 * @author Daniel Bonniot
 */

public class IncrementExp extends Expression
{
  public IncrementExp(Expression var, boolean prefix, boolean increment)
  {
    this.var = expChild(var);
    this.prefix = prefix;
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

    gnu.expr.Declaration decl = var.getDeclaration();
    if (decl != null)
      return new gnu.expr.IncrementExp
	(decl, (short) (increment ? 1 : -1), prefix);
    else
      {
	return var.compileAssign(new gnu.expr.ApplyExp
	  (plusProc(), new gnu.expr.Expression[]{ var.compile(), oneExp }));
      }
  }

  private static gnu.expr.Expression oneExp =
    ConstantExp.makeNumber(1).compile();
  
  private static gnu.expr.Expression plusProc;
  private static gnu.expr.Expression plusProc()
  {
    if (plusProc == null)
      plusProc = new gnu.expr.QuoteExp(((MethodDefinition.Symbol) Node.getGlobalScope().lookup(new LocatedString("+", Location.nowhere())).get(0)).definition.getDispatchMethod());

    return plusProc;
  }
  
  /****************************************************************
   * Misc.
   ****************************************************************/
  
  public String toString()
  {
    if (prefix)
      return (increment ? "++" : "--") + var.toString();
    else
      return var.toString() + (increment ? "++" : "--");
  }

  private Expression var;
  private boolean prefix, increment;
}
