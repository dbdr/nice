/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : JavaMethod.java
// Created : Tue Nov 09 11:49:47 1999 by bonniot

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import bossa.util.Location;
import mlsub.typing.TypeConstructor;

import java.util.*;

/**
   A Native Java Method.

   @version $Date$
   @author Daniel Bonniot
 */
public class JavaMethod extends MethodDeclaration
{
  /**
   * @param name the name of the method
   * @param typeParameters the type parameters
   * @param constraint the constraint
   * @param returnType the return type
   * @param parameters the MonoTypes of the parameters
   */
  public JavaMethod
    (
     // Informations about the java method
     // These 3 args are null if we are in an interface file
     LocatedString className,
     String methodName,
     List /* of LocatedString */ javaTypes,
     // Nice information
     LocatedString name, 
     Constraint constraint,
     Monotype returnType,
     List parameters
    )
  {
    super(name,constraint,returnType,parameters);
    
    this.className = className;
    this.methodName = methodName;
    this.javaTypes = javaTypes;
  }

  private JavaMethod
    (
     // Informations about the java method
     // These 3 args are null if we are in an interface file
     LocatedString className,
     String methodName,
     List /* of LocatedString */ javaTypes,
     // Nice information
     LocatedString name, 
     mlsub.typing.Constraint constraint,
     mlsub.typing.Monotype returnType,
     mlsub.typing.Monotype[] parameters
    )
  {
    super(name, null, null , null);
    
    this.className = className;
    this.methodName = methodName;
    this.javaTypes = javaTypes;

    setLowlevelTypes(constraint, parameters, returnType);
  }

  private static JavaMethod make(Method m, boolean constructor)
  {
    try
      {
	JavaMethod res;

	Type[] paramTypes = m.getParameterTypes();
	mlsub.typing.Monotype[] params;
	int n = 0; // index in params
	
	if(m.getStaticFlag() || constructor)
	  params = new mlsub.typing.Monotype[paramTypes.length];
	else
	  {
	    params = new mlsub.typing.Monotype[paramTypes.length + 1];
	    params[n++] = nice.tools.code.Types.getMonotype(m.getDeclaringClass());
	  }
    
	for(int i = 0; i<paramTypes.length; i++)
	  params[n++] = nice.tools.code.Types.getMonotype(paramTypes[i]);
    
	mlsub.typing.Monotype retType;
	if(constructor)
	  retType = nice.tools.code.Types.getMonotype(m.getDeclaringClass());
	else
	  retType = nice.tools.code.Types.getMonotype(m.getReturnType());
    
	res = new JavaMethod(null, m.getName(), null,
			     new LocatedString(m.getName(),Location.nowhere()),
			     null, retType, params);
	res.reflectMethod = m;
	return res;
      }
    catch(nice.tools.code.Types.ParametricClassException e){
      // The fetched method involves parametric java classes.
      // Ignore.
      return null;
    }
    catch(nice.tools.code.Types.NotIntroducedClassException e){
      // The fetched method involves invalid types.
      // Ignore.
      return null;
    }
  }
  
  /****************************************************************
   * Store automatically fetched java methods.
   * Do not take them into account if an explicit java method
   * was declared for the same reflect method.
   ****************************************************************/

  private static Map declaredMethods = new HashMap();
  
  /**
   * Creates a bossa method for the fiven native method
   * and puts it in global scope.
   *
   * @param m the native method.
   * @return the created JavaMethod.
   */
  public static JavaMethod addFetchedMethod(Method m)
  {
    if(declaredMethods.get(m) != null)
      return null;

    JavaMethod md = JavaMethod.make(m, false);

    if(md != null)
      Node.getGlobalScope().addSymbol(md.symbol);

    return md;
  }

  public static JavaMethod addFetchedConstructor
    (Method m,
     TypeConstructor declaringClass)
  {
    if(declaredMethods.get(m) != null)
      return null;
    
    JavaMethod md = JavaMethod.make(m, true);
    
    if(md != null)
      TypeConstructors.addConstructor(declaringClass, md);

    return md;
  }

  public static MethodDeclaration addFetchedMethod(Field m)
  {
    MethodDeclaration md = StaticFieldAccess.make(m);
    Node.getGlobalScope().addSymbol(md.symbol);
    return md;
  }

  Scopes buildScope(VarScope outer, TypeScope typeOuter)
  {
    // We put this here, since we need 'module' to be computed
    // since it is used to open the imported packages.
    findReflectMethod();    
    
    return super.buildScope(outer, typeOuter);
  }
  
  private gnu.bytecode.Type type(LocatedString s)
  {
    Type res = nice.tools.code.Types.type(s.toString());
    if(res == null)
      User.error(s, "Unknown java class "+s);
    return res;
  }
  
  /** The java class this method is defined in */
  LocatedString className;

  /** Its name in the java class */
  String methodName;
  
  private List /* of LocatedString */ javaTypes;
  
  /** Access flags */
  private int flags;
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  private Type javaRetType;
  private Type[] javaArgType;
  private Method reflectMethod;
  
  public gnu.bytecode.Type javaReturnType() 
  { return javaRetType; }

  public gnu.bytecode.Type[] javaArgTypes()
  { return javaArgType; }

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    return new gnu.expr.PrimProcedure(reflectMethod);
  }
  
  private void findReflectMethod()
  {
    if(reflectMethod != null)
      return;
    
    Type holder = nice.tools.code.Types.type(className.toString());
    if(holder == null)
      User.error(this,
		 "Class " + className + " was not found");
    
    if(!(holder instanceof ClassType))
      User.error(this, className + " is a primitive type");
    
    className = new LocatedString(holder.getName(), className.location());
    
    javaArgType = new Type[javaTypes.size()-1];
    
    for(int i = 1; i<javaTypes.size(); i++)
      {
	LocatedString t = (LocatedString) javaTypes.get(i);
	
	javaArgType[i-1] = type(t);
	
	// set the fully qualified name back
	javaTypes.set(i,new LocatedString(javaArgType[i-1].getName(),
					  t.location()));
      }
    
    LocatedString t = (LocatedString) javaTypes.get(0);
    javaRetType = type(t);
    
    // set the fully qualified name of the return type back
    javaTypes.set(0, new LocatedString(javaRetType.getName(), t.location()));
    
    reflectMethod = ((ClassType) holder).getDeclaredMethod
      (methodName, javaArgType);
    
    if(reflectMethod == null)
      User.error(this, "method " + methodName + 
		 " was not found in class " + holder.getName());

    // use the following, or the Type.flushTypeChanges() in SpecialTypes
    //reflectMethod.arg_types = javaArgType;
    //if(!methodName.equals("<init>"))
    //reflectMethod.return_type = javaRetType;
    
    declaredMethods.put(reflectMethod, Boolean.TRUE);
    
    //if (reflectMethod.isConstructor())
    //.addConstructor(this);
    
    int javaArity;
    if(reflectMethod.getStaticFlag() || reflectMethod.isConstructor())
      javaArity = javaTypes.size()-1;
    else
      javaArity = javaTypes.size();
    
    if(javaTypes != null && javaArity != arity)
      User.error(this,
		 "Native method " + this.symbol.name + 
		 " has not the same number of parameters " +
		 "in Java (" + javaArity +
		 ") and in Nice (" + arity + ")");    
  }
  
  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(super.toString() +
	    " = native " +
	    (methodName.equals("<init>") 
	     ?  "new " + className
	     : javaTypes.get(0) 
	     + " " + className 
	     + "." + methodName) +
	    mapGetName(javaArgTypes()) + ";\n");
  }
  
  private static String mapGetName(gnu.bytecode.Type[] types)
  {
    if(types == null)
      return "((NULL))";
    
    String res = "(";
    for(int n = 0; n<types.length; n++)
      {
	if(n != 0)
	  res += ", ";
	if(types[n] == null)
	  res += "[NULL]";
	else
	  res += types[n].getName();
      }
    return res + ")";
  }    
}
