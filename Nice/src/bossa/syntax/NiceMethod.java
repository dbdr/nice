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
import nice.tools.typing.Types;

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
     Contract contract,
     boolean isOverride)
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
	 new ArrayList((hasAlike ? 1 : 0) + 
                       (thisConstraint == null ? 0 : 
                        thisConstraint.getAtoms().size())));

    constraint.addBinders(thisBinders);
    if (thisConstraint != null)
      constraint.addAtoms(thisConstraint.getAtoms());

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
      return new NiceMethod(name, constraint, returnType, params, contract, 
                            isOverride);
    else
      return NiceMethod.WithDefault.create
        (name, constraint, returnType, params, body, contract, isOverride);
  }

  public NiceMethod(LocatedString name, 
		    Constraint constraint, Monotype returnType, 
		    FormalParameters parameters,
		    Contract contract, boolean isOverride)
  {
    super(name, constraint, returnType, parameters, contract);
    this.isOverride = isOverride;
    this.returnTypeLocation = returnType.location();
    bossa.link.Dispatch.register(this);
  }

  private boolean isOverride;
  private bossa.util.Location returnTypeLocation;

  public boolean isMain()
  {
    if (! (name.toString().equals("main") && arity == 1))
      return false;

    return getType().domain()[0].toString().equals("java.lang.String[]");
  }

  void resolve()
  {
    super.resolve();

    homonyms = Node.getGlobalScope().lookup(getName());
    homonyms.remove(getSymbol());
  }

  void typedResolve()
  {
    findSpecializedMethods();

    if (isOverride && specializedMethods == null)
      User.error(this, "This method does not override any other method");

    if (! inInterfaceFile() && ! isOverride && specializedMethods != null)
      {
        MethodDeclaration parent = 
          (MethodDeclaration) specializedMethods.get(0);
        boolean sameResult = 
          getReturnType().toString().equals(parent.getReturnType().toString());

        if (sameResult)
          User.warning(this, "This method overrides " + parent +
"\nYou should make this explicit, either by omitting the return type" +
                       "\nor by using the 'override' keyword");
        else
          User.warning(this, "This method overrides " + parent +
"\nYou should make this explicit by using the 'override' keyword");
      }

    super.typedResolve();
  }

  private List homonyms;

  private List specializedMethods = null;

  public Iterator listSpecializedMethods()
  {
    return specializedMethods == null ? null : specializedMethods.iterator();
  }

  public boolean specializesMethods()
  {
    //if (homonyms != null)
    //throw new Error(this.toString());

    return specializedMethods != null;
  }

  void findSpecializedMethods()
  {
    if (homonyms.isEmpty())
      {
        homonyms = null;
        return;
      }

    Domain ourDomain = Types.domain(getType());

    for (ListIterator i = homonyms.listIterator(); i.hasNext();)
      {
        VarSymbol s = (VarSymbol) i.next();
        MethodDeclaration d = s.getMethodDeclaration();

        // Ignore non-methods.
        if (d == null || d.isIgnored())
          continue;

        // Check that we have the same number of arguments
        if (d.getArity() != this.getArity())
          continue;

        Domain itsDomain = Types.domain(s.getType());

        // Do we have a smaller domain?
        if (! (Typing.smaller(ourDomain, itsDomain, true))
            ||  Types.typeParameterDispatch(getType(), s.getType()))
          continue;

        if (! Types.covariantSpecialization(getType(), s.getType()))
          {
            User.error
              (returnTypeLocation != null ? returnTypeLocation : location(), 
"The return type is less precise than the original return type of method\n" +
d + "\ndefined in:\n" +
               d.location());
          }

        // Check if we are a proper specialization, or if we actually have
        // the same domain.
        if (Typing.smaller(itsDomain, ourDomain))
          {
            if (module == d.module)
              User.error(this, "This method has a domain identical to " +
                         d + ", which is defined at " + d.location());
            else
              // Methods with identical domains in different packages are
              // accepted, but they do not specialize each other.
              // They can be refered to unambiguously by using their
              // fully qualified name.
              continue;
          }

        if (specializedMethods == null)
          specializedMethods = new ArrayList
            // Heuristic: the maximum number that might be needed
            (homonyms.size() - i.previousIndex());
        specializedMethods.add(d);
      }

    homonyms = null;
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
       Contract contract, boolean isOverride)
    {
      if (body == null)
        return new NiceMethod
          (name, constraint, returnType, parameters, contract, isOverride);

      return new DefaultMethodImplementation
        (name, constraint, returnType, parameters, contract, isOverride, body);
    }

    public WithDefault
      (LocatedString name, 
       Constraint constraint, Monotype returnType, 
       FormalParameters parameters,
       Contract contract, boolean isOverride,
       DefaultMethodImplementation impl)
    {
      super(name, constraint, returnType, parameters, contract, isOverride);
      this.implementation = impl;
    }

    DefaultMethodImplementation implementation;

    void innerTypecheck() throws TypingEx
    {
      super.innerTypecheck();
      try {
        implementation.innerTypecheck();
      } finally {
        mlsub.typing.lowlevel.Engine.existentialLevel = 0;
      }
    }

    protected gnu.expr.Expression computeCode()
    {
      // We need to store the result before compiling the implementation.
      code = super.computeCode();

      // Compile the implementation before returning. This is used to detect
      // dependencies between global variables through default method 
      // implementations, so that the global variables can be initialized in
      // the proper order.
      implementation.getRefExp();

      return code;
    }
  }
}
