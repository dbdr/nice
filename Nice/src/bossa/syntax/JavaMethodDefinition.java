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

// File    : JavaMethodDefinition.java
// Created : Tue Nov 09 11:49:47 1999 by bonniot
//$Modified: Fri Jul 28 21:27:43 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import bossa.util.Location;
import mlsub.typing.TypeConstructor;

import java.util.*;

/**
 * A Native Java Method.
 */
public class JavaMethodDefinition extends MethodDefinition
{
  /**
   * @param name the name of the method
   * @param typeParameters the type parameters
   * @param constraint the constraint
   * @param returnType the return type
   * @param parameters the MonoTypes of the parameters
   */
  public JavaMethodDefinition(
			      // Informations about the java method
			      // These 3 args are null if we are in an interface file
			      LocatedString className,
			      String methodName,
			      List /* of LocatedString */ javaTypes,
			      // Bossa information
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

  private JavaMethodDefinition(
			       // Informations about the java method
			       // These 3 args are null if we are in an interface file
			       LocatedString className,
			       String methodName,
			       List /* of LocatedString */ javaTypes,
			       // Bossa information
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

  private static JavaMethodDefinition make(Method m, boolean constructor)
  {
    try
      {
	JavaMethodDefinition res;

	Type[] paramTypes = m.getParameterTypes();
	mlsub.typing.Monotype[] params;
	int n = 0; // index in params
	
	if(m.getStaticFlag() || constructor)
	  params = new mlsub.typing.Monotype[paramTypes.length];
	else
	  {
	    params = new mlsub.typing.Monotype[paramTypes.length + 1];
	    params[n++] = bossa.CodeGen.getMonotype(m.getDeclaringClass());
	  }
    
	for(int i = 0; i<paramTypes.length; i++)
	  params[n++] = bossa.CodeGen.getMonotype(paramTypes[i]);
    
	mlsub.typing.Monotype retType;
	if(constructor)
	  retType = bossa.CodeGen.getMonotype(m.getDeclaringClass());
	else
	  retType = bossa.CodeGen.getMonotype(m.getReturnType());
    
	res = new JavaMethodDefinition(null, m.getName(), null,
				       new LocatedString(m.getName(), 
							 Location.nowhere()),
				       null,
				       retType, params);
	res.reflectMethod = m;
	return res;
      }
    catch(bossa.CodeGen.ParametricClassException e){
      // The fetched method involves parametric java classes.
      // Ignore.
      return null;
    }
    catch(bossa.CodeGen.NotIntroducedClassException e){
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
   * @return the created JavaMethodDefinition.
   */
  public static JavaMethodDefinition addFetchedMethod(Method m)
  {
    if(declaredMethods.get(m) != null)
      return null;

    JavaMethodDefinition md = JavaMethodDefinition.make(m, false);

    if(md != null)
      Node.getGlobalScope().addSymbol(md.symbol);

    return md;
  }

  public static JavaMethodDefinition addFetchedConstructor
    (Method m,
     TypeConstructor declaringClass)
  {
    if(declaredMethods.get(m) != null)
      return null;
    
    JavaMethodDefinition md = JavaMethodDefinition.make(m, true);
    
    if(md != null)
      TypeConstructors.addConstructor(declaringClass, md);

    return md;
  }

  public static MethodDefinition addFetchedMethod(Field m)
  {
    MethodDefinition md = StaticFieldAccess.make(m);
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
    Type res = bossa.CodeGen.type(s.toString());
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
  
  private int javaArity;
  
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
    
    Type holder = bossa.CodeGen.type(className.toString());
    if(holder == null)
      User.error(this,
		 "Class " + className + " was not found");
    
    if(!(holder instanceof ClassType))
      {
	if (className.toString().equals("ObjectArray"))
	  holder = bossa.SpecialArray.objectArrayType;
	else
	  User.error(this, className + " is a primitive type");
      }
    
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
    
    if(reflectMethod==null)
      reflectMethod = ((ClassType) holder).getDeclaredMethod
      (methodName, javaArgType);
    
    if(reflectMethod == null)
      User.error(this, this + " was not found in " + holder);

    // use the following, or the Type.flushTypeChanges() in SpecialTypes
    //reflectMethod.arg_types = javaArgType;
    //if(!methodName.equals("<init>"))
    //reflectMethod.return_type = javaRetType;
    
    declaredMethods.put(reflectMethod, Boolean.TRUE);
    
    //if (reflectMethod.isConstructor())
    //.addConstructor(this);
    
    if(reflectMethod.getStaticFlag() || reflectMethod.isConstructor())
      javaArity = arity;
    else
      javaArity = arity-1;
    
    if(javaTypes != null && javaTypes.size()-1 != javaArity)
      User.error(this,
		 "Native method "+this.symbol.name+
		 " has not the same number of parameters "+
		 "in Java ("+javaArity+
		 ") and in Bossa ("+arity+")");    
  }
  
  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(interfaceString());
  }

  private String interfaceString()
  {
    String res =
      syntacticConstraint
      + symbol.type.codomain()
      + " "
      + symbol.name.toQuotedString()
      + "("
      + Util.map("",", ","",symbol.type.domain())
      + ")"
      + " = native ";
    if(methodName.equals("<init>"))
       res += "new " + className;
    else
      res +=  javaTypes.get(0) 
	+ " " + className 
	+ "." + methodName;
    
    return res
      + mapGetName(javaArgTypes())
      + ";\n";
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
