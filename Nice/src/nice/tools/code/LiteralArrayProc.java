/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

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
    // Try to get the precise bytecode type to use from the context.
    if (target.getType() instanceof ArrayType)
      arrayType = (ArrayType) target.getType();

    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    code.emitPushInt(nbElements);
    code.emitNewArray(arrayType.getComponentType());
    
    /*
      Optimization:
      We need to keep the reference to the array.

      Instead of `dup'ing it before each use,
      we `dup2' it every second iteration.

      This is better than producing all the references in advance,
      which would make the stack grow unboundedly.

      This saves nbElements/2 bytecodes.
    */

    if (nbElements > 0)
      code.emitDup();

    for (int i = 0; i < nbElements; i++)
      {
	if (i < nbElements - 2)
	  {
	    code.emitDup(2);

	    code.emitPushInt(i);
	    args[i].compile(comp, arrayType.elements);
	    code.emitArrayStore(arrayType.elements);
	    i++;
	  }
	else if (i == nbElements - 2)
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
