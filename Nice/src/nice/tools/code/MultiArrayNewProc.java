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
   Create a multidimensional array.
   
   @author Daniel Bonniot
 */

public class MultiArrayNewProc extends gnu.mapping.ProcedureN
  implements Inlineable
{
  /**
     @param arrayType the type of the array
     @param nbDimensions the number of dimensions to be allocated.
       The corresponding number of integers are expected as arguments 
       of the procedure.
   */
  public MultiArrayNewProc(ArrayType arrayType, int nbDimensions)
  {
    this.arrayType = arrayType;
    this.nbDimensions = nbDimensions;
  }

  private ArrayType arrayType;
  private int nbDimensions;
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    
    for (int i=0; i<nbDimensions; i++)
      args[i].compile(comp, Type.int_type);

    comp.getCode().emitNewArray(arrayType.getComponentType(), nbDimensions);
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
