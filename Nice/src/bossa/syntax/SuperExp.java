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
import nice.tools.code.Types;

import mlsub.typing.Polytype;
import mlsub.typing.Monotype;
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
    superAlternative = getSuper(decl);
  }

  MethodBodyDefinition currentMethod;
  Alternative superAlternative = null;
  Method superMethod = null;

  private Alternative getSuper(MethodDeclaration decl)
  {
    Alternative superAlt = null;
    Alternative current = currentMethod.getAlternative();

    java.util.Iterator alternatives = Alternative.sortedAlternatives(decl).iterator();

    // Look for the first alternative more general than the current one.
    while (alternatives.hasNext())
      {
	Alternative a = (Alternative) alternatives.next();
	if (a == current) continue;

	if (Alternative.leq(current, a))
	  if (superAlt == null || Alternative.leq(a, superAlt))
	    superAlt = a;
	  else if (Alternative.leq(superAlt, a))
	    ; // superAlt is a more direct parent than a, so ignore a.
	  else
	    {
	      String message = "This call to super is ambiguous. " + 
		"Possible parents are:\n" + superAlt + "\nand\n" + a;
	      throw User.error(this, message);
	    }
      }

    if (superAlt != null)
      return superAlt;

    if (decl instanceof JavaMethod)
      {
	getSuper((JavaMethod) decl);
	return null;
      }
    else
      throw User.error(this, "There is no super implementation to call");
  }

  private void getSuper(JavaMethod decl)
  {
    Type firstArg = nice.tools.code.Types.get(currentMethod.firstArgument());
    if (! (firstArg instanceof ClassType))
      throw User.error(this, "The first argument of this method is not a class");
    superMethod = getImplementationAbove(decl, (ClassType) firstArg);
    if (superMethod != null)
      return;

    throw User.error(this, "There is no super implementation to call");
  }

  public static Method getImplementationAbove(JavaMethod decl, ClassType firstArg)
  {
    Method thisMethod = decl.reflectMethod;
    ClassType superClass = firstArg.getSuperclass();
    if (superClass == null)
      return null;
    
    return superClass.getMethod(thisMethod.getName(), 
				thisMethod.getParameterTypes());
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
	    TypeConstructor superTC = null;
            try {
              superTC = Types.typeConstructor(superMethod.getDeclaringClass());
            }
            catch(Types.NotIntroducedClassException ex ) {}

	    if (superTC == null)
	      {
		// Our safe bet is to assert that the argument is 
		// above Object
		superTC = JavaClasses.object
		  (currentMethod.firstArgument().variance.arity());
	      }

	    constraint = addLeq
	      (type.getConstraint(), 
	       new Pattern[]{new Pattern(superTC, false)}, 
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
    AtomicConstraint[] newAtoms;
    if (oldAtoms != null) {
      newAtoms = new AtomicConstraint[oldAtoms.length + p.length];
      System.arraycopy(oldAtoms, 0, newAtoms, 0, oldAtoms.length);
    }
    else
      newAtoms = new AtomicConstraint[p.length];

    for (int i = 0; i < p.length; i++)
      newAtoms[newAtoms.length - 1 - i] = new TypeConstructorLeqMonotypeCst(p[i].tc, m[i]);

    return new Constraint(c.binders(), newAtoms);
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
      code = ((NiceClass) ClassDefinition.get(currentMethod.firstArgument()).implementation).
	callSuperMethod(superMethod);

    return new gnu.expr.ApplyExp(code, currentMethod.compiledArguments());
  }
  
  public String toString()
  {
    return "super";
  }
}
