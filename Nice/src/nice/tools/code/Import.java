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

package nice.tools.code;

import mlsub.typing.*;
import gnu.bytecode.*;
import bossa.util.Internal;

/**
   Import native methods.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class Import
{
  /**
     Computes the Nice type of a native method.

     Uses its full signature (JDK 1.5) if available.
  */
  public static Polytype type(Method m)
  {
    boolean constructor = m.isConstructor();

    ClassType declaringClass = m.getDeclaringClass();
    Type[] paramTypes = m.getFullParameterTypes();
    TypeVariable[] methodTypeParameters = m.getTypeParameters();

    TypeVariable[] typeParameters = null;
    TypeSymbol[] niceTP = null;

    if (m.getStaticFlag())
      {
	if (methodTypeParameters != null)
	  {
	    typeParameters = new TypeVariable[methodTypeParameters.length];

	    for (int i = methodTypeParameters.length; --i >= 0;)
	      typeParameters[i] = methodTypeParameters[i];

	    niceTP = makeTP(typeParameters);
	  }
      }
    else
      {
	TypeVariable[] classTypeParameters = declaringClass.getParameters();
	int nParams = classTypeParameters == null ? 
	  0 : classTypeParameters.length;
	nParams += methodTypeParameters == null ? 
	  0 : methodTypeParameters.length;
	if (nParams > 0)
	  {
	    typeParameters = new TypeVariable[nParams];

	    if (methodTypeParameters != null)
	      for (int i = methodTypeParameters.length; --i >= 0;)
		typeParameters[--nParams] = methodTypeParameters[i];

	    if (classTypeParameters != null)
	      for (int i = classTypeParameters.length; --i >= 0;)
		typeParameters[--nParams] = classTypeParameters[i];

	    niceTP = makeTP(typeParameters);
	  }
      }

    mlsub.typing.Monotype[] params;
    int n = 0; // index in params

    try 
      {
	if(m.getStaticFlag() || constructor)
	  params = new mlsub.typing.Monotype[paramTypes.length];
	else
	  {
	    params = new mlsub.typing.Monotype[paramTypes.length + 1];
	    params[n++] = Types.monotype(declaringClass.thisType(), true,
					 typeParameters, niceTP);
	  }

        boolean nonNullArgs = strictPackages.contains
          (m.getDeclaringClass().getPackageName());

	for (int i = 0; i < paramTypes.length; i++)
	  params[n++] = Types.monotype(paramTypes[i], /*sure:*/nonNullArgs,
			typeParameters, niceTP);

        gnu.bytecode.Type javaRetType = 
		constructor ? declaringClass.thisType()	: m.getFullReturnType();
	mlsub.typing.Monotype retType = 
		Types.monotype(javaRetType, true, typeParameters, niceTP);

	Constraint cst = niceTP == null ? null : new Constraint(niceTP, null);
	return new Polytype(cst, new FunType(params, retType));
      }
    catch(Types.ParametricClassException e){
      // The fetched method involves parametric java classes.
      // Ignore.
      Internal.warning("Java method " + m + " was ignored.\nReason: " + e);
      return null;
    }
    catch(Types.NotIntroducedClassException e){
      // The fetched method involves invalid types.
      // Ignore.
      Internal.warning("Java method " + m + " was ignored.\nReason: " + e);
      return null;
    }
  }

  private static TypeSymbol[] makeTP(TypeVariable[] vars)
  {
    TypeSymbol[] res = new TypeSymbol[vars.length];
    for (int i = 0; i < vars.length; i++)
      res[i] = new MonotypeVar(vars[i].getName());
    return res;
  }

  /****************************************************************
   * Retyping policies
   ****************************************************************/

  private static java.util.HashSet strictPackages = new java.util.HashSet();

  static void reset() { strictPackages.clear(); }

  public static void addStrictPackage(String name)
  {
    strictPackages.add(name);
  }

  public static boolean isStrictPackage(String name)
  {
    return strictPackages.contains(name);
  }
}
