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
import nice.tools.code.Types;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

import bossa.util.Location;
import bossa.util.Debug;

/**
   Definition of a toplevel function.

   Functions, as opposed to methods, do not dispatch on their arguments' types,
   but have a single implementation.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class ToplevelFunction extends MethodDeclaration 
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
			  Statement body)
  {
    super(name, constraint, returnType, parameters);

    this.body = body;
    this.voidReturn = returnType.toString().equals("void");
  }

  /** Can be null if this funtion is taken from an interface file. */
  private Statement body;
  private MonoSymbol[] symbols;
  private boolean voidReturn ;

  void resolve()
  {
    buildParameterSymbols();
    
    mlsub.typing.Constraint cst = getType().getConstraint();
    if (mlsub.typing.Constraint.hasBinders(cst))
      try{
	typeScope.addSymbols(cst.binders());
      }
      catch(TypeScope.DuplicateName e){
	User.error(this, e);
      }
    
    if(body != null)
      body = dispatch.analyse$0(body, scope, typeScope, !voidReturn);
  }

  private void buildParameterSymbols()
  {
    // the type must be found before
    removeChild(symbol);
    symbol.doResolve();

    symbols = parameters.getMonoSymbols();
    mlsub.typing.Monotype[] paramTypes = getArgTypes();
    for (int i = 0; i < paramTypes.length; i++)
      if (symbols[i].name != null)
	{
	  symbols[i].type = paramTypes[i];
	  scope.addSymbol(symbols[i]);
	}
  }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  void innerTypecheck() throws TypingEx
  {
    // The body must be type-checked in a rigid context
    // This is not done in MethodDeclaration, 
    // because it is not usefull for all subclasses
    Typing.implies();

    Node.currentFunction = this;
    try{ bossa.syntax.dispatch.typecheck$0(body); }
    finally{ Node.currentFunction = null; }
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  private gnu.expr.BlockExp blockExp;
  public gnu.expr.BlockExp getBlock() { return blockExp; }

  private Method primMethod;

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    String bytecodeName = module.mangleName(name.toString());  
    primMethod = module.addPackageMethod
      (bytecodeName, javaArgTypes(), javaReturnType());
    return new gnu.expr.PrimProcedure(primMethod);
  }
  
  public void compile()
  {
    if (body == null)
      return;

    // forces computation of primMethod
    // it already exists if this function is called 
    // by code anterior to its definition
    getDispatchMethod();

    gnu.expr.LambdaExp lexp = 
      nice.tools.code.Gen.createMethod(primMethod, symbols);
    Statement.currentScopeExp = lexp;
    blockExp = (gnu.expr.BlockExp) lexp.body;
    blockExp.setBody(body.generateCode());
    module.compileMethod(lexp);
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
