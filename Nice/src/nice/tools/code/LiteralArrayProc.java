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

// File    : MultiArrayNewProc.java
// Created : Mon Aug 28 14:42:14 2000 by Daniel Bonniot
//$Modified: Tue Aug 29 12:11:19 2000 by Daniel Bonniot $

package nice.tools.code;

import gnu.expr.*;
import gnu.bytecode.*;

/**
   Creates a literal array constant.
   
   @author Daniel Bonniot
 */

public class LiteralArrayProc extends gnu.mapping.ProcedureN
  implements Inlineable
{
  /**
     @param arrayType the type of the array
     @param nbElements the number of elements of the array
       The corresponding number of elements are expected as arguments 
       of the procedure.
   */
  public LiteralArrayProc(ArrayType arrayType, int nbElements)
  {
    this.arrayType = arrayType;
    this.nbElements = nbElements;
  }

  private ArrayType arrayType;
  private int nbElements;
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    code.emitPushInt(nbElements);
    code.emitNewArray(arrayType.getComponentType());
    
    for (int i=0; i<nbElements; i++)
      {
	code.emitDup();
	code.emitPushInt(i);
	args[i].compile(comp, arrayType.elements);
	code.emitArrayStore(arrayType.elements);
      }

    target.compileFromStack(comp, arrayType);
  }
  
  public Type getReturnType(Expression[] args)
  {
    return arrayType;
  }

  public Object applyN(Object[] args)
  {
    throw new Error("Not implemented");
  }
}
