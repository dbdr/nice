/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 2000                             */
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
   A method defined in Nice.
   
   @version $Date$
   @author Daniel Bonniot
 */
public class NiceMethod extends MethodDeclaration
{
  /**
   * The method is a class member.
   *
   * @param c the class this method belongs to.
   * @param name the name of the method
   * @param typeParameters the type parameters
   * @param constraint the constraint
   * @param returnType the return type
   * @param parameters the MonoTypes of the parameters
   */
  public static NiceMethod create
    (ClassDefinition c,
     LocatedString name, 
     Constraint constraint,
     Monotype returnType,
     List parameters)
  {
    //it is a class method, there is an implicit "this" argument

    boolean hasAlike = returnType.containsAlike() 
      || Monotype.containsAlike(parameters);
    
    // create type parameters with the same names as in the class
    mlsub.typing.MonotypeVar[] thisTypeParams =
      c.createSameTypeParameters();
    
    int thisTypeParamsLen = (thisTypeParams == null ? 0 
			     : thisTypeParams.length);
    
    // if the constraint is True
    // we must create a new one, otherwise we would
    // modify other methods!
    if(constraint == Constraint.True)
      constraint = new Constraint
	(new ArrayList(thisTypeParamsLen + (hasAlike ? 1 : 0)),
	 new ArrayList((hasAlike ? 1 : 0)));
	
    constraint.addBinders(thisTypeParams);
	
    mlsub.typing.Monotype thisType;
    if(hasAlike)
      {
	TypeConstructor alikeTC = 
	  new TypeConstructor("Alike", c.variance(), false, false);
	
	constraint.addBinder(alikeTC);
	// added in front. Important for rebinding in method alternatives
	
	mlsub.typing.AtomicConstraint atom;
	if(c.getAssociatedInterface()!=null)
	  atom = new mlsub.typing.ImplementsCst
	    (alikeTC, c.getAssociatedInterface());
	else
	  atom = new mlsub.typing.TypeConstructorLeqCst(alikeTC, c.tc);
	constraint.addAtom(AtomicConstraint.create(atom));
	
	thisType = new mlsub.typing.MonotypeConstructor(alikeTC, thisTypeParams);
	
	Map map = new HashMap();
	map.put(Alike.id, alikeTC);
	returnType = returnType.substitute(map);
	parameters = Monotype.substitute(map, parameters);
      }
    else
      thisType = 
	new mlsub.typing.MonotypeConstructor(c.tc, thisTypeParams);
    
    parameters.add(0, Monotype.create(thisType));
    
    NiceMethod res = new NiceMethod(name, constraint, returnType, parameters);
    res.memberOf = c;
    
    return res;
  }

  public NiceMethod(LocatedString name, 
		    Constraint constraint,
		    Monotype returnType,
		    List parameters)
  {
    super(name, constraint, returnType, parameters);
  }
  
  /****************************************************************
   * Typechecking
   ****************************************************************/

  void innerTypecheck()
  {
    // set bytecode types for type variables
    mlsub.typing.FunType ft = 
      (mlsub.typing.FunType) getType().getMonotype();
    
    Types.setBytecodeType(ft.domain());
    Types.setBytecodeType(ft.codomain());
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    dispatchPrimMethod = module.addDispatchMethod(this);
    return new gnu.expr.PrimProcedure(dispatchPrimMethod);
  }
  
  public gnu.bytecode.Type javaReturnType()
  {
    return Types.javaType(getType().codomain());
  }
  
  public gnu.bytecode.Type[] javaArgTypes()
  {
    return Types.javaType(getType().domain());
  }
  
  public void compile()
  {
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(super.toString() + ";\n");
  }
}
