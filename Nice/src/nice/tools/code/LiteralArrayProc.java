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
  public LiteralArrayProc(ArrayType arrayType, int nbElements,
                          boolean wrapAsCollection)
  {
    this.arrayType = arrayType;
    this.nbElements = nbElements;
    this.wrapAsCollection = wrapAsCollection;
  }

  private ArrayType arrayType;
  private int nbElements;
  private boolean wrapAsCollection;
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    arrayType = MultiArrayNewProc.creationType(arrayType, target);

    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    Type componentType = arrayType.getComponentType().getImplementationType();

    code.emitPushInt(nbElements);
    code.emitNewArray(componentType);

    // Set a special type, not the legacy array type.
    code.popType();
    code.pushType(arrayType);

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
        // Duplicate the reference to the array, according to our future needs.
	if (i % 2 == 0)
          if (i < nbElements - 2)
	    code.emitDup(2);
          else if (i == nbElements - 2)
            code.emitDup();

        // Get the specific type for this rank of the array (useful for tuples)
        Type specificType = Types.componentType(arrayType, i);
        // Only use it if it is more specific than the expected type.
        // For instance don't use int if we store it in an Object[] anyway.
        if (! specificType.isAssignableTo(componentType))
          specificType = componentType;

	code.emitPushInt(i);
        args[i].compile(comp, specificType);
	code.emitArrayStore(componentType);
      }

    if (wrapAsCollection)
      SpecialArray.emitCoerceToCollection(code);
    else
      target.compileFromStack(comp, code.topType());
  }

  public Type getReturnType(Expression[] args)
  {
    if (wrapAsCollection)
      return ClassType.make("java.util.List");

    return arrayType;
  }

  public Object applyN(Object[] args)
  {
    throw new Error("Not implemented");
  }
}
