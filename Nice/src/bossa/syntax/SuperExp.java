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

import bossa.link.*;
import bossa.util.*;
import gnu.expr.*;
import gnu.bytecode.*;
import mlsub.typing.*;

import mlsub.typing.Polytype;
import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.FunType;
import mlsub.typing.Constraint;
import mlsub.typing.AtomicConstraint;

/**
   A call to the next most specific implementation of a metohd.   

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
*/

public class SuperExp extends Expression
{
  void setCurrentMethod(MethodBodyDefinition m)
  {
    currentMethod = m;

    MethodDeclaration decl = currentMethod.getDeclaration();
    gnu.expr.Expression code;
    if (decl instanceof NiceMethod)
      superAlternative = getSuper((NiceMethod) decl);
    else
      getSuper((JavaMethod) decl);
  }

  MethodBodyDefinition currentMethod;
  Alternative superAlternative = null;
  MethodBodyDefinition superImplementation = null;
  Method superMethod = null;

  private Alternative getSuper(NiceMethod decl)
  {
    Alternative superAlt = null;
    Alternative current = currentMethod.getAlternative();

    java.util.Iterator alternatives = Alternative.sortedAlternatives(decl).iterator();

    // Look for the first alternative more general than the current one.
    for (; alternatives.hasNext();)
      {
	Alternative a = (Alternative) alternatives.next();
	if (a == current) continue;

	if (Alternative.leq(current, a))
	  if (superAlt == null)
	    superAlt = a;
	  else
	    throw User.error(this, "This call to super is ambiguous");
      }

    if (superAlt == null)
      throw User.error(this, "There is no super implementation to call");

    return superAlt;
  }

  private void getSuper(JavaMethod decl)
  {
    for (java.util.Iterator i = decl.getImplementations();
	 i.hasNext();)
      {
	MethodBodyDefinition impl = (MethodBodyDefinition) i.next();
	if (leq(impl, currentMethod))
	  continue;
	
	if (superImplementation == null || leq(impl, superImplementation))
	  superImplementation = impl;
      }

    Method thisMethod = decl.reflectMethod;
    Type firstArg = nice.tools.code.Types.get(currentMethod.firstArgument());
    if (! (firstArg instanceof ClassType))
      throw User.error(this, "The first argument of this method is not a class");
    ClassType superClass = ((ClassType) firstArg).getSuperclass();
    if (superClass != null)
      {
	superMethod = superClass.getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
	if (superMethod != null)
	  return;
      }

    throw User.error(this, "There is no super implementation to call");
  }

  private boolean leq(MethodBodyDefinition a, MethodBodyDefinition b)
  {
    return mlsub.typing.Typing.testRigidLeq(a.firstArgument(), b.firstArgument());
  }

  /****************************************************************
   * Typing
   ****************************************************************/

  void computeType()
  {
    Polytype type = currentMethod.getDeclaration().getType();
    FunType monotype;
    Constraint constraint;
    if (! type.isMonomorphic())
      {
	// The type of super is computed by restricting the method type
	// to the patterns of the super implementation.

	type = type.cloneType();
	monotype = (FunType) type.getMonotype();
	if (superAlternative != null)
	  constraint = addLeq(type.getConstraint(), superAlternative.getPatterns(), monotype.domain());
	else
	  {
	    TypeConstructor superTC;
	    if (superImplementation != null)
	      superTC = superImplementation.firstArgument();
	    else
	      {
		superTC = findTCforClass
		  (currentMethod.firstArgument(), superMethod.getDeclaringClass());
		if (superTC == null)
		  {
		    // This probably means that super comes from a special
		    // class that is not in the Nice hierarchy, like
		    // java.lang.Object (because of variances).
		    
		    // Our safe bet is to assert that the argument is 
		    // above Object
		    superTC = JavaClasses.object
		      (GlobalTypeScope.compilation, 
		       currentMethod.firstArgument().variance.arity());
		  }
	      }

	    constraint = addLeq
	      (type.getConstraint(), 
	       new Pattern[]{new Pattern(superTC, null, false)}, 
	       monotype.domain());
	  }
      }
    else
      {
	monotype = (FunType) type.getMonotype();
	constraint = Constraint.True;
      }
    this.type = new Polytype(constraint, monotype.codomain());
  }
  
  private static Constraint addLeq(Constraint c, Pattern[] p, Monotype[] m)
  {
    AtomicConstraint[] oldAtoms = c.atoms();
    AtomicConstraint[] newAtoms = new AtomicConstraint
      [(oldAtoms == null ? 0 : oldAtoms.length) + p.length];
    if (oldAtoms != null)
      System.arraycopy(oldAtoms, 0, newAtoms, 0, oldAtoms.length);
    for (int i = 0; i < p.length; i++)
      newAtoms[newAtoms.length - 1 - i] = new TypeConstructorLeqMonotypeCst(p[i].tc, m[i]);
    return new Constraint(c.binders(), newAtoms);
  }

  private static TypeConstructor findTCforClass(TypeConstructor tc, 
						ClassType t)
  {
    if (nice.tools.code.Types.get(tc) == t)
      return tc;

    TypeConstructor[] supers = ClassDefinition.get(tc).superClass;
    if (supers != null)
      for (int i = 0; i < supers.length; i++)
	{
	  TypeConstructor res = findTCforClass(supers[i], t);
	  if (res != null)
	    return res;
	}

    return null;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.expr.Expression compile()
  {
    gnu.expr.Expression code;
    if (superAlternative != null)
      code = superAlternative.methodExp();
    else
      // It does not matter which method is called (the super method or
      // the base method), a call to super is emited.
      code = new QuoteExp(PrimProcedure.specialCall(superMethod));

    return new gnu.expr.ApplyExp(code, currentMethod.compiledArguments());
  }
  
  public String toString()
  {
    return "super";
  }
}
