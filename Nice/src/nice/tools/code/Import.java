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

	boolean strictImport = 
          bossa.modules.Package.currentCompilation.strictJavaTypes;

        boolean nonNullArgs = strictPackages.contains
          (m.getDeclaringClass().getPackageName());

        if (strictImport || nonNullArgs)
	{
	  for (int i = 0; i < paramTypes.length; i++)
	  //arguments of a method are considered not null 
	    params[n++] = Types.monotype(paramTypes[i], true, typeParameters,
				       niceTP, true);
	} else {
	  for (int i = 0; i < paramTypes.length; i++)
	  //arguments maybe null
	    params[n++] = Types.monotype(paramTypes[i], false, typeParameters,
					niceTP, true);
	}

	mlsub.typing.Monotype retType;
	if (constructor)
	    // the return type is surely not null
	    retType = Types.monotype(declaringClass.thisType(), true,
				     typeParameters, niceTP);
	else if (strictImport)
	{		
	  Type returnType = m.getFullReturnType();
	  if (m.getStaticFlag())
	    //no exception found yet to this assumption 
	    retType = Types.monotype(returnType, true, 
				     typeParameters, niceTP, true);
	  else
	    {
	      Type classType = declaringClass.thisType();
	    
	      if ((returnType.getName() != null) && returnType.getName().
						 equals(classType.getName()))
	        //if returntype equals declaringclass than it's almost sure
	        //that returntype isn't null, this sort of methods are common
	        //in immutable classes such as String	
	        retType = Types.monotype(returnType, true, typeParameters,
				         niceTP);
	      else if ((returnType instanceof ParameterizedType) &&
		       (classType instanceof ParameterizedType) &&
		     (((ParameterizedType)returnType).main.getName() != null) &&
		     (((ParameterizedType)returnType).main.getName().equals(
		      ((ParameterizedType)classType).main.getName())))
	        //same as above for parameterized classes a bit more difficult
	        retType = Types.monotype(returnType, true, typeParameters,
				         niceTP);
	      else if (returnType instanceof ArrayType)
	        //an array can be empty so it should never be null
	        retType = Types.monotype(returnType, true, typeParameters,
				       niceTP, true);
	      else
	        //the rest where no good guess is possible about nullness
	        retType = Types.monotype(returnType, typeParameters, niceTP);
	    }
	} else {
	   //if not strict then returntype not null.
	   retType = Types.monotype(m.getFullReturnType(), true,
				    typeParameters, niceTP, true);
	}	  
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
}
