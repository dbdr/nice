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
//$Modified: Mon Jul 24 18:20:10 2000 by Daniel Bonniot $

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
    if(!var.isAssignable())
      User.error(this, var + " cannot be incremented");

    gnu.expr.Declaration decl = var.getDeclaration();
    if (decl != null)
      return new gnu.expr.IncrementExp
	(decl, (short) (increment ? 1 : -1), prefix);

    if (prefix)
      return var.compileAssign(new gnu.expr.ApplyExp
	(plusProc(), 
	 new gnu.expr.Expression[]{ var.generateCode(), oneExp }));
    
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

    return new gnu.expr.ApplyExp
      (new nice.tools.code.IncrementProc(access.field(), increment),
       new gnu.expr.Expression[]{ ((Expression) call.parameters.get(0)).generateCode() });
  }

  private static gnu.expr.Expression oneExp =
    ConstantExp.makeNumber(1).generateCode();
  
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
