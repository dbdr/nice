/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import java.util.*;

import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.TupleType;
import mlsub.typing.Polytype;
import mlsub.typing.Constraint;

import gnu.bytecode.Type;
import gnu.expr.*;
import gnu.expr.Expression;

import nice.tools.code.Types;

/**
   Creation of a tuple.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class TupleExp extends bossa.syntax.Expression
{
  public TupleExp(List expressions)
  {
    this.expressions = toArray(expressions);
  }

  /****************************************************************
   * Typing
   ****************************************************************/

  public boolean isAssignable()
  {
    for (int i = 0; i < expressions.length; i++)
      if (! expressions[i].isAssignable())
	return false;
    
    return true;
  }
  
  /**
     Adjust the array type according to the context.

     This is usefull because arrays are non-variant.
     For instance, different code must be generated 
     for [ 1, 2 ] in the contexts:
     int[] i = [ 1, 2 ]
     and 
     byte[] b = [ 1, 2 ]
  */
  bossa.syntax.Expression resolveOverloading(Polytype expectedType)
  {
    // This can only help
    expectedType.simplify();

    Monotype m = expectedType.getMonotype();
    // get rid of the nullness part
    m = Types.rawType((MonotypeConstructor) m);

    // Get the expected component types
    if (m instanceof TupleType)
      expectedComponents = ((TupleType) m).getComponents();

    return this;
  }

  bossa.syntax.Expression noOverloading()
  {
    for(int i = expressions.length; i-->0;)
      expressions[i] = expressions[i].noOverloading();

    return this;
  }

  private Monotype[] components;
  private Monotype[] expectedComponents;

  void computeType()
  {
    Polytype[] types = bossa.syntax.Expression.getType(expressions);
    // should create a new <tt>and</tt> method without the last dummy parameters
    Constraint cst = Constraint.and(Polytype.getConstraint(types), 
				    null, null);
    components = Polytype.getMonotype(types);
    TupleType tupleType = new TupleType(components);
    Types.setBytecodeType(components);
    if (expectedComponents == null)
      expectedComponents = components;

    type = new Polytype(cst, bossa.syntax.Monotype.sure(tupleType));
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.expr.Expression compile()
  {
    // Force computation of the component types.
    getType();

    /*
      We base the array type on the expected type, but we record the
      real type of the components.
    */
    return nice.tools.code.TupleType.createExp
      (Types.lowestCommonSupertype(expectedComponents),
       Types.javaType(components), 
       bossa.syntax.Expression.compile(expressions));
  }

  gnu.expr.Expression compileAssign(gnu.expr.Expression array)
  {
    int len = expressions.length;

    LetExp let = null;
    Expression tupleExp;
    
    Type arrayType = array.getType();
    nice.tools.code.TupleType tupleType = null;
    if (arrayType instanceof nice.tools.code.TupleType)
      tupleType = (nice.tools.code.TupleType) arrayType;

    // if array is a complex expression, 
    // we have to evaluate it and store the result 
    // to avoid evaluating it several times.
    if (!(array instanceof ReferenceExp))
      {
	let = new LetExp(new Expression[]{array});
	Declaration tupleDecl = let.addDeclaration
	  ("tupleRef", arrayType);

	//FIXME: CanRead should be set automatically.
	tupleDecl.setCanRead(true);
	tupleExp = new ReferenceExp(tupleDecl);
      }
    else
      tupleExp = array;
    
    Expression[] stmts = new Expression[len];
    for (int i=0; i<len; i++)
      {
	Expression value = new ApplyExp
	  (new nice.lang.inline.ArrayGetOp(null),
	   new Expression[]{ tupleExp, intExp(i)});
	if (tupleType != null)
	  value = nice.tools.code.EnsureTypeProc.ensure(value, tupleType.componentTypes[i]);

	stmts[i] = expressions[i].compileAssign(value);
      }
    
    if (let != null)
      {
	let.setBody(new BeginExp(stmts));
	return let;
      }
    else
      return new BeginExp(stmts);
  }

  private Expression intExp(int i)
  {
    return new QuoteExp(new Integer(i));
  }
  
  /****************************************************************
   * Misc.
   ****************************************************************/
  
  public String toString()
  {
    return Util.map("(", ", ", ")", expressions);
  }

  bossa.syntax.Expression[] expressions;
}
