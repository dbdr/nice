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
public class NiceMethod extends UserOperator
{
  /**
     The method is a class or interface member.
    
     @param c the class this method belongs to.
     @param name the name of the method
     @param typeParams the type parameters
     @param constraint the constraint
     @param returnType the return type
     @param params the MonoTypes of the parameters
     @param body the body of the function, or null if this is a real method
   */
  public static MethodDeclaration create
    (MethodContainer c,
     LocatedString name, 
     Constraint constraint,
     Monotype returnType,
     FormalParameters params,
     Statement body, 
     Contract contract)
  {
    // it is a class method, there is an implicit "this" argument

    boolean hasAlike = returnType.containsAlike() 
      || params.containsAlike();

    MethodContainer.Constraint thisConstraint = c.classConstraint;
    mlsub.typing.MonotypeVar[] thisTypeParams = c.getTypeParameters();
    
    int thisTypeParamsLen = (thisTypeParams == null ? 0 
			     : thisTypeParams.length);

    TypeSymbol container = c.getTypeSymbol();
    // contains the interface if container is one
    Interface itf = null;
    // contains the associated tc if possible
    TypeConstructor tc;
    if (container instanceof TypeConstructor)
      tc = (TypeConstructor) container;
    else
      {
	itf = (Interface) container;
	tc = itf.associatedTC();
      }
    
    // if the constraint is True
    // we must create a new one, otherwise we would
    // modify other methods!
    if(constraint == Constraint.True)
      constraint = new Constraint
	(new ArrayList(thisTypeParamsLen + (hasAlike ? 1 : 0)),
	 new ArrayList((hasAlike ? 1 : 0) + (thisConstraint == null ? 0 
					     : thisConstraint.atoms.size())));
	
    constraint.addBinders(thisTypeParams);
    if (thisConstraint != null)
      constraint.addAtoms(thisConstraint.atoms);

    mlsub.typing.Monotype thisType;

    // "alike" is not created for a non-abstract interface
    // if alike is not present in the type, since it saves
    // a type parameter (more intuituve for rebinding)
    // and it does not change typing to do so.
    if(hasAlike || tc == null)
      {
	TypeConstructor alikeTC = 
	  new TypeConstructor("alike", c.variance, false, false);
	
	constraint.addBinder(alikeTC);
	// added in front. Important for rebinding in method alternatives
	
	mlsub.typing.AtomicConstraint atom;
	if (itf != null)
	  atom = new mlsub.typing.ImplementsCst(alikeTC, itf);
	else
	  atom = new mlsub.typing.TypeConstructorLeqCst(alikeTC, tc);
	constraint.addAtom(AtomicConstraint.create(atom));
	
	thisType = new mlsub.typing.MonotypeConstructor(alikeTC, thisTypeParams);
	
	if (hasAlike)
	  {
	    Map map = new HashMap();
	    map.put(Alike.id, alikeTC);
	    returnType = returnType.substitute(map);
	    params.substitute(map);
	  }
      }
    else
      thisType = 
	new mlsub.typing.MonotypeConstructor(tc, thisTypeParams);
    
    params.addThis(Monotype.create(Monotype.sure(thisType)));
    
    if (body == null)
      return new NiceMethod(name, constraint, returnType, params, contract);
    else
      return new ToplevelFunction(name, constraint, returnType, params, 
				  body, contract);
  }

  public NiceMethod(LocatedString name, 
		    Constraint constraint, Monotype returnType, 
		    FormalParameters parameters,
		    Contract contract)
  {
    super(name, constraint, returnType, parameters, contract);
    bossa.link.Dispatch.register(this);
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  /** @return a string that uniquely represents this method */
  public String getFullName()
  {
    return module.getName() + '.' + name + ':' + getType();
  }

  protected gnu.expr.Expression computeCode()
  {
    return module.getDispatchMethod(this);
  }

  public final LambdaExp getLambda()
  {
    return nice.tools.code.Gen.dereference(getCode());
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
