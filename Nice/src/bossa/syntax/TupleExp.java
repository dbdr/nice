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
import gnu.bytecode.ArrayType;
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

  boolean isAssignable()
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
    m = ((MonotypeConstructor) m).getTP()[0];

    // Get the expected component types
    if (m instanceof TupleType)
      expectedComponents = ((TupleType) m).getComponents();

    return this;
  }

  private Monotype[] expectedComponents;

  void computeType()
  {
    Polytype[] types = bossa.syntax.Expression.getType(expressions);
    // should create a new <tt>and</tt> method without the last dummy parameters
    Constraint cst = Constraint.and(Polytype.getConstraint(types), 
				    null, null);
    Monotype[] components = Polytype.getMonotype(types);
    TupleType tupleType = new TupleType(components);
    Types.setBytecodeType(components);
    this.componentType = Types.lowestCommonSupertype
      (expectedComponents != null ? expectedComponents : components);

    type = new Polytype(cst, bossa.syntax.Monotype.sure(tupleType));
  }

  private Type componentType;
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.expr.Expression compile()
  {
    int len = expressions.length;
    
    // The array is not a special array, since it has nothing to
    // do with collections.
    
    Expression arrayVal = new gnu.expr.ApplyExp
      (new nice.tools.code.MultiArrayNewProc(new ArrayType(componentType), 1),
       new Expression[]{intExp(len)});

    LetExp let = new LetExp(new Expression[]{arrayVal});
    //let.outer = Statement.currentScopeExp;
    Declaration arrayDecl = let.addDeclaration("tuple", 
					       ArrayType.make(componentType));

    //FIXME: CanRead should be set automatically if it is true
    arrayDecl.setCanRead(true);

    Expression array = new ReferenceExp(arrayDecl);
    
    Expression[] stmts = new Expression[1 + len];
    for (int i=0; i<len; i++)
      stmts[i] = 
	new ApplyExp(new nice.lang.inline.ArraySetOp(componentType),
		     new Expression[]{
		       array, intExp(i), expressions[i].generateCode()
		     });
    
    stmts[len] = array;
    
    let.setBody(new BeginExp(stmts));
    
    return let;
  }

  gnu.expr.Expression compileAssign(gnu.expr.Expression array)
  {
    int len = expressions.length;

    LetExp let = null;
    Expression tupleExp;
    
    // if array is a complex expression, 
    // we have to evaluate it and store the result 
    // to avoid evaluating it several times.
    if (!(array instanceof ReferenceExp))
      {
	let = new LetExp(new Expression[]{array});
	//let.outer = Statement.currentScopeExp;
	Declaration tupleDecl = let.addDeclaration
	  ("tupleRef", ArrayType.make(componentType));

	//FIXME: Idem, see above
	tupleDecl.setCanRead(true);
	tupleExp = new ReferenceExp(tupleDecl);
      }
    else
      tupleExp = array;
    
    Expression[] stmts = new Expression[len];
    for (int i=0; i<len; i++)
      stmts[i] = expressions[i].compileAssign
	(new ApplyExp(new nice.lang.inline.ArrayGetOp(componentType),
		      new Expression[]{
			tupleExp, 
			intExp(i)}));
    
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
