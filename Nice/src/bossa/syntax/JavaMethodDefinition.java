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
//$Modified: Sat Dec 04 12:45:54 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

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
			      String className,
			      String methodName,
			      List /* of String */ javaTypes,
			      // Bossa information
			      LocatedString name, 
			      Constraint constraint,
			      Monotype returnType,
			      List parameters
			      )
  {
    super(name,constraint,returnType,parameters);
    this.className=className;
    this.methodName=methodName;
    this.javaTypes=javaTypes;

    boolean isStatic = java.lang.reflect.Modifier.isStatic
      (findReflectMethod(className,methodName,javaTypes).getModifiers());
    
    flags=0;
    if(isStatic)
      {
	flags|=Access.STATIC;
	javaArity=arity;
      }
    else
      javaArity=arity-1;

    if(className==null)
      MethodDefinition.addMethod(this);

    if(javaTypes!=null && javaTypes.size()-1!=javaArity)
      User.error(this.name,
		 "Native method "+this.name+
		 " has not the same number of parameters in Java and Bossa !");
  }

  // Class.forName doesn't report errors nicely, I do it myself !
  private Class getClass(String name)
  {
    try{
      if(name.equals("void"))
	return Void.TYPE;
      else if(name.equals("int"))
	return Integer.TYPE;
      else if(name.equals("boolean"))
	return Boolean.TYPE;
      else
	return Class.forName(name);
    }
    catch(ClassNotFoundException e){
      User.error(this,
		 "Class \""+name+"\" does not exists");
      return null;
    }
  }
  
  private java.lang.reflect.Method
    findReflectMethod(String className, String methodName, List javaTypes)
  {
    try
      {
	Class[] classes = new Class[javaTypes.size()-1];
	for(int i=1;i<javaTypes.size();i++)
	  classes[i-1]=getClass((String) javaTypes.get(i));
	
	return getClass(className).
	  getDeclaredMethod(methodName,classes);
      }
    catch(NoSuchMethodException e){
      User.error(this,
		 "Method "+className+"."+methodName+" does not exists");
    }
    
    return null;
  }
  
  /** The java class this method is defined in */
  String className;

  /** Its name in the java class */
  String methodName;
  
  List /* of String */ javaTypes;
  
  /** Access flags */
  private int flags;
  
  private int javaArity;
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  private gnu.bytecode.Type type(String s)
  {
    // FIXME
    if(s.equals("void"))
      return gnu.bytecode.Type.void_type;
    else if(s.equals("int"))
      return gnu.bytecode.Type.int_type;
    else if(s.equals("boolean"))
      return gnu.bytecode.Type.boolean_type;
    else
      return ClassType.make(s);
  }
  
  protected gnu.bytecode.Type returnJavaType() 
  { return type((String)javaTypes.get(0)); }

  protected gnu.bytecode.Type[] argumentsJavaTypes()
  {
    gnu.bytecode.Type[] res=new gnu.bytecode.Type[javaArity];
    for(int i=0;i<javaArity;i++)
      res[i]=type((String)javaTypes.get(i+1));
    
    return res;
  }

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    ClassType c=ClassType.make(className);
    return new gnu.expr.PrimProcedure
      (
       c.addMethod
       (methodName,
	argumentsJavaTypes(),returnJavaType(),
	flags)
	);
  }
  
  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(toString());
  }

  private String interfaceString()
  {
    return
      type.codomain().toString()
      + " "
      + name
      + Util.map("<",", ",">",type.getTypeParameters())
      + type.getConstraint().toString()
      + "("
      + Util.map("",", ","",type.domain())
      + ")"
      + " = "
      + returnJavaType().getName()
      + " " + className
      + "." + methodName
      + mapGetName(argumentsJavaTypes())
      + ";\n";
  }
  
  private static String mapGetName(gnu.bytecode.Type[] types)
  {
    String res="(";
    for(int n=0;n<types.length;n++)
      {
	if(n!=0)
	  res+=", ";
	res+=types[n].getName();
      }
    return res+")";
  }
    
  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    return "native " + interfaceString();
  }
}
