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

    arrayType = creationType(arrayType, target, false);

    comp.getCode().emitNewArray(arrayType.getComponentType(), nbDimensions);
    target.compileFromStack(comp, arrayType);
  }

  /** Decide the bytecode type to create a new array with,
      given its computed type and the target.

      @param promote whether promotion of component types is desired.
   */
  static ArrayType creationType(ArrayType computedType, Target target,
                                boolean promote)
  {
    if (target.getType() instanceof ArrayType)
      {
	ArrayType targetType = (ArrayType) target.getType();
	/*
	   By well-typing, we know the target type is a super-type of
	   the computed type.
	   If the target has primitive components, we might as well
	   use that type to produce better code, since subsumption would need
	   copying otherwise.
	   On the other hand, it would be incorrect (and useless) for
	   reference types, in case the arrays comes back in the value of
	   a function.
	*/
	if (hasPrimitiveComponents(targetType))
	  return targetType;
      }

    if (promote)
      // We don't have information about the context. The sensible thing to do
      // is to promote primitive types (those smaller than int).
      return promoteComponent(computedType);
    else
      return computedType;
  }

  private static boolean hasPrimitiveComponents(ArrayType array)
  {
    Type componentType = array.getComponentType();

    return componentType instanceof ArrayType 
        || componentType instanceof PrimType;
  }

  /** Recursively promote the component of the given array. */
  private static ArrayType promoteComponent(ArrayType array)
  {
    Type type = array.getComponentType();

    // Is the type subject to promotion?
    Type promoted;

    if (type == Type.byte_type || type == Type.short_type)
      promoted = Type.int_type;

    // If not directly, is it an array whose component type is?
    else if (type.isArray())
      promoted = promoteComponent((ArrayType) type);

    else
      promoted = type;

    // If the component is changed, return a new array.
    if (promoted != type)
      return SpecialArray.create(promoted);

    return array;
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
