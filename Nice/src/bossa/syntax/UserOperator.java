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

package bossa.syntax;

import nice.tools.code.Types;

/**
   An operator whose semantics is defined by the user (i.e. not built-in).

   A contract can be attached to a user operator.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

abstract class UserOperator extends MethodDeclaration
{
  UserOperator(LocatedString name, 
	       Constraint constraint, Monotype returnType, 
	       FormalParameters parameters,
	       Contract contract)
  {
    super(name, constraint, returnType, parameters);
    this.contract = contract;
  }

  private Contract contract;

  public Contract getContract() { return contract; }

  /****************************************************************
   * Resolution
   ****************************************************************/

  void doResolve()
  {
    // the type must be found before
    removeChild(getSymbol());
    getSymbol().doResolve();

    symbols = parameters.getMonoSymbols();
    if (symbols != null)
      {
	mlsub.typing.Monotype[] paramTypes = getArgTypes();
	for (int i = 0; i < symbols.length; i++) {
	  if (Types.isVoid(paramTypes[i]))
	    throw bossa.util.User.error(symbols[i].syntacticType, 
					"A parameter cannot have a void type");
	  if (symbols[i].name != null)
	    {
	      symbols[i].type = paramTypes[i];
	      scope.addSymbol(symbols[i]);
	    }
	}
      }

    VarScope scope = this.scope;
    TypeScope typeScope = this.typeScope;

    super.doResolve();

    // The contract must be resolved after the formal parameters since they
    // can refer to them.
    contract.resolve(scope, typeScope, getReturnType(), location());
  }

  public String toString()
  {
    return super.toString() + contract.toString();
  }

  private MonoSymbol[] symbols;

  public MonoSymbol[] getSymbols() { return symbols; }

  /****************************************************************
   * Typechecking
   ****************************************************************/

  void innerTypecheck() throws mlsub.typing.TypingEx
  {
    /* 
       The body must be type-checked in a rigid context
       This is not done in MethodDeclaration, 
       because it is not usefull for all subclasses.
       
       XXX: Note that this is a waste if this is a method declaration
            and there is no contract. The performance loss should be mesured,
	    to see if optimisation is necessary.
    */
    mlsub.typing.Typing.implies();

    contract.typecheck();

    // Set bytecode types for type variables.
    mlsub.typing.FunType ft = (mlsub.typing.FunType) getType().getMonotype();

    Types.setBytecodeType(ft.domain());
    Types.setBytecodeType(ft.codomain());
  }

  void typecheckCompiled()
  {
    /* We only need typechecking if there is a contract, to resolve
       overloading.
       We will probably be able to remove this if contracts cen be reloaded
       from bytecode.
    */
    if (contract != Contract.noContract)
      typecheck();
  }
}
