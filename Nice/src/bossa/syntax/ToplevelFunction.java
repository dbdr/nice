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
import mlsub.typing.*;
import nice.tools.code.Gen;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

import bossa.util.Debug;

/**
   Definition of a toplevel function.

   Functions, as opposed to methods, do not dispatch on their arguments' types,
   but have a single implementation.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class ToplevelFunction extends UserOperator
implements Function
{
  /**
     @param name the name of the method
     @param constraint the constraint
     @param returnType the return type
     @param parameters the formal parameters
     @param body the body of the function
   */
  public ToplevelFunction(LocatedString name, 
			  Constraint constraint,
			  Monotype returnType,
			  FormalParameters parameters,
			  Statement body,
			  Contract contract)
  {
    super(name, constraint, returnType, parameters, contract);

    this.body = body;
    this.voidReturn = returnType.toString().equals("void");
  }

  /** Can be null if this funtion is taken from an interface file. */
  private Statement body;
  private boolean voidReturn;

  void resolve()
  {
    super.resolve();

    mlsub.typing.Constraint cst = getType().getConstraint();
    if (mlsub.typing.Constraint.hasBinders(cst))
      try{
	typeScope.addSymbols(cst.binders());
      }
      catch(TypeScope.DuplicateName e){
	User.error(this, e);
      }
    
    // Save the scopes, since we need them later, but they get null'ed.
    thisScope = scope;
    thisTypeScope = typeScope;
  }

  private VarScope thisScope;
  private TypeScope thisTypeScope;

  void resolveBody()
  {
    if (body != null)
      body = dispatch.analyse(body, thisScope, thisTypeScope, !voidReturn);
  }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  public mlsub.typing.Monotype getExpectedType()
  {
    return getReturnType();
  }

  public void checkReturnedType(mlsub.typing.Polytype returned)
    throws Function.WrongReturnType
  {
    try {
      Typing.leq(returned, getReturnType());
    }
    catch (mlsub.typing.TypingEx e) {
      throw new Function.WrongReturnType(e, getReturnType());
    }
  }

  void innerTypecheck() throws TypingEx
  {
    super.innerTypecheck();

    Node.currentFunction = this;
    if (parameters.hasThis())
      Node.thisExp = new SymbolExp(getSymbols()[0], location());

    try{ 
      bossa.syntax.dispatch.typecheck(body); 
    }
    finally{ 
      Node.currentFunction = null; 
      Node.thisExp = null;
    }
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.expr.Expression computeCode()
  {
    if (body == null)
      {
	// An alternative design would be to leave toplevel functions 
	// out of .nicei files, and use the type information in the bytecode
	// attributes (optim: use java type if monomorphic).
	// Then there would be no need to bother linking declaration and code.

	// Just get the handle to the already compiled function.
	gnu.expr.Expression res = module.lookupPackageMethod
	  (name.toString(), getType().toString());

	if (res != null)
	  return res;

	Internal.error(this, "Toplevel function " + name +
		       " was not found in compiled package " + module);
      }

    LambdaExp code = Gen.createMethod
      (name.toString(), javaArgTypes(), javaReturnType(), getSymbols());
    code.addBytecodeAttribute
      (new MiscAttr("type", getType().toString().getBytes()));

    return module.addMethod(code, true);
  }
  
  public void compile()
  {
    if (body == null)
      return;

    if(Debug.codeGeneration)
      Debug.println("Compiling toplevel function " + this);

    LambdaExp lexp = Gen.dereference(getCode());

    Gen.setMethodBody(lexp, getContract().compile(body.generateCode()));
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter w)
  {
    w.print(super.toString());
    w.print(" = ...\n");
  }
}
