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
  public static Definition create
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
    mlsub.typing.TypeSymbol[] thisBinders = c.getBinders();
    
    int thisBindersLen = (thisBinders == null ? 0 
			     : thisBinders.length);

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
	(new ArrayList(thisBindersLen + (hasAlike ? 1 : 0)),
	 new ArrayList((hasAlike ? 1 : 0) + (thisConstraint == null ? 0 
					     : thisConstraint.atoms.size())));
	
    constraint.addBinders(thisBinders);
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
	  new TypeConstructor("Alike", c.variance, false, false);
	
	// Add in front. Important for rebinding in method alternatives
	constraint.addFirstBinder(alikeTC);
	
	mlsub.typing.AtomicConstraint atom;
	if (itf != null)
	  atom = new mlsub.typing.ImplementsCst(alikeTC, itf);
	else
	  atom = new mlsub.typing.TypeConstructorLeqCst(alikeTC, tc);
	constraint.addAtom(AtomicConstraint.create(atom));
	
	thisType = new mlsub.typing.MonotypeConstructor(alikeTC, c.getTypeParameters());
	
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
	new mlsub.typing.MonotypeConstructor(tc, c.getTypeParameters());
    
    params.addThis(Monotype.create(Monotype.sure(thisType)));
    
    if (body == null)
      return new NiceMethod(name, constraint, returnType, params, contract);
    else
      return NiceMethod.WithDefault.create
        (name, constraint, returnType, params, body, contract);
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

  /****************************************************************
   * Method with default implementation
   ****************************************************************/

  public static class WithDefault extends NiceMethod
  {
    /**
       @param name the name of the method
       @param constraint the constraint
       @param returnType the return type
       @param parameters the formal parameters
       @param body the body of the function
    */
    public static Definition create
      (LocatedString name, 
       Constraint constraint,
       Monotype returnType,
       FormalParameters parameters,
       Statement body,
       Contract contract)
    {
      if (body == null)
        return new NiceMethod
          (name, constraint, returnType, parameters, contract);

      return new DefaultMethodImplementation
        (name, constraint, returnType, parameters, contract, body);
    }

    public WithDefault
      (LocatedString name, 
       Constraint constraint, Monotype returnType, 
       FormalParameters parameters,
       Contract contract,
       DefaultMethodImplementation impl)
    {
      super(name, constraint, returnType, parameters, contract);
      this.implementation = impl;
    }

    DefaultMethodImplementation implementation;

    void resolve()
    {
      super.resolve();

      mlsub.typing.Constraint cst = getType().getConstraint();
      if (mlsub.typing.Constraint.hasBinders(cst))
        try {
          typeScope.addSymbols(cst.binders());
        } catch (TypeScope.DuplicateName ex) {
          User.error(this, "Double declaration of the same type parameter");
        }
    }

    void innerTypecheck() throws TypingEx
    {
      super.innerTypecheck();
      implementation.innerTypecheck();
    }
  }
}
