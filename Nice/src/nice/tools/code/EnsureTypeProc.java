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

// File    : EnsureTypeProc.java
// Created : Tue Aug 29 10:24:58 2000 by Daniel Bonniot
//$Modified: Tue Aug 29 12:12:26 2000 by Daniel Bonniot $

package nice.tools.code;

import gnu.expr.*;
import gnu.bytecode.*;

import nice.lang.inline.OptionOr;
/**
   Ensures that the expression has the given bytecode type.
   
   That is, add a cast in the bytecode if necessary.
   
   @author Daniel Bonniot
 */

public class EnsureTypeProc extends gnu.mapping.Procedure1 
implements Inlineable
{
  public static Expression ensure(Expression exp, Type expectedType)
  {
    Type type = exp.getType();

    if (! type.isAssignableTo(expectedType))
      return Inline.inline(new EnsureTypeProc(expectedType), exp);
    else
      return exp;
  }
  
  private EnsureTypeProc(Type type)
  {
    this.type = type;
  }

  private Type type;
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    
    Target oldTarget = null;
    if (target instanceof StackTarget)
      {
	oldTarget = target;
	target = new CheckedTarget(type, "nice.tools.code.EnsureTypeProc", 
				   gnu.mapping.WrongType.ARG_DESCRIPTION);
      }
    
    exp.getArgs()[0].compile(comp, target);

    /*
      If we changed the target, we also have to use the old one.
      It can happen that both produce code. For instance with
      int[][] a;
      a[1][2] = 3; // i.e. set(get(a,1), 2, 3)
    */
    if (oldTarget != null)
      oldTarget.compileFromStack(comp, type);
  }
  
  public gnu.bytecode.Type getReturnType (Expression[] args)
  {
    return type;
  }

  public Object apply1 (Object arg1)
  {
    throw new RuntimeException ("not implemented");
  }
}
