/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import bossa.util.Location;
import mlsub.typing.TypeConstructor;
import nice.tools.code.Types;

import java.util.*;

/**
   A Native Java Method.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
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
     FormalParameters parameters
    )
  {
    super(name,constraint,returnType,parameters);
    
    this.className = className;
    this.methodName = methodName;
    this.javaTypes = javaTypes;
  }

  JavaMethod
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
    super(name, constraint, parameters, returnType);
    
    this.className = className;
    this.methodName = methodName;
    this.javaTypes = javaTypes;
  }

  static JavaMethod make(Method m, boolean constructor)
  {
    try {
      JavaMethod res;

      Type[] paramTypes = m.getParameterTypes();
      mlsub.typing.Monotype[] params;
      int n = 0; // index in params
	
      if(m.getStaticFlag() || constructor)
	params = new mlsub.typing.Monotype[paramTypes.length];
      else
	{
	  params = new mlsub.typing.Monotype[paramTypes.length + 1];
	  params[n++] = Types.monotype(m.getDeclaringClass(), true);
	}
    
      for(int i = 0; i<paramTypes.length; i++)
	params[n++] = Types.monotype(paramTypes[i]);
    
      mlsub.typing.Monotype retType;
      if(constructor)
	// the return type is surely not null
	retType = Types.monotype(m.getDeclaringClass(), true);
      else
	retType = Types.monotype(m.getReturnType());
    
      res = new JavaMethod(null, m.getName(), null,
			   new LocatedString(m.getName(),Location.nowhere()),
			   null, retType, params);
      res.reflectMethod = m;
      return res;
    }
    catch(Types.ParametricClassException e){
      // The fetched method involves parametric java classes.
      // Ignore.
      return null;
    }
    catch(Types.NotIntroducedClassException e){
      // The fetched method involves invalid types.
      // Ignore.
      Internal.warning("Java method " + m + " was ignored.\nReason: " + e);
      return null;
    }
  }
  
  void buildScope(VarScope outer, TypeScope typeOuter)
  {
    // We put this here, since we need 'module' to be computed
    // since it is used to open the imported packages.
    findReflectMethod();    
    
    super.buildScope(outer, typeOuter);
  }
  
  private gnu.bytecode.Type type(LocatedString s)
  {
    Type res = Types.type(s.toString());
    if(res == null)
      User.error(s, "Unknown java class "+s);
    return res;
  }
  
  boolean hasThis()
  {
    return true;
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
  Method reflectMethod;
  
  public gnu.bytecode.Type javaReturnType() 
  { return javaRetType; }

  public gnu.bytecode.Type[] javaArgTypes()
  { return javaArgType; }

  protected gnu.expr.Expression computeCode()
  {
    return new gnu.expr.QuoteExp(new gnu.expr.PrimProcedure(reflectMethod));
  }
  
  private void findReflectMethod()
  {
    if(reflectMethod != null)
      return;
    
    Type holder = Types.type(className.toString());
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
    
    LocatedString retTypeString = (LocatedString) javaTypes.get(0);
    javaRetType = type(retTypeString);
    
    // set the fully qualified name of the return type back
    javaTypes.set(0, new LocatedString(javaRetType.getName(), 
				       retTypeString.location()));
    
    reflectMethod = 
      ((ClassType) holder).getDeclaredMethod (methodName, javaArgType);
    
    if(reflectMethod == null)
      User.error(this, "Method " + methodName + 
		 " was not found in class " + holder.getName());

    // use the following, or the Type.flushTypeChanges() in SpecialTypes
    //reflectMethod.arg_types = javaArgType;
    //if(!methodName.equals("<init>"))
    //reflectMethod.return_type = javaRetType;

    if (! reflectMethod.isConstructor() &&
	! reflectMethod.return_type.getName().equals(javaRetType.getName()))
      User.error(retTypeString,
		 "Method " + methodName +
		 " has not the indicated return type." +
		 "\nIndicated: " + javaRetType.getName() +
		 "\nFound    : " + reflectMethod.return_type.getName());
    
    JavaClasses.registerNativeMethod(this, reflectMethod);    

    //if (reflectMethod.isConstructor())
    //.addConstructor(this);
    
    int javaArity;
    if(reflectMethod.getStaticFlag() || reflectMethod.isConstructor())
      javaArity = javaTypes.size()-1;
    else
      javaArity = javaTypes.size();
    
    if(javaTypes != null && javaArity != arity)
      User.error(this,
		 "Native method " + this.getSymbol().name + 
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

  /****************************************************************
   * List of implementations
   ****************************************************************/

  Iterator getImplementations()
  {
    return implementations.iterator();
  }

  private LinkedList implementations = new LinkedList();

  void addImplementation(MethodBodyDefinition impl)
  {
    implementations.add(impl);
  }
}
