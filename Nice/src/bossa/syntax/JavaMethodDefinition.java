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
//$Modified: Mon Nov 15 12:00:02 1999 by bonniot $

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
   * @param c the class this method belongs to, or 'null'
   * @param name the name of the method
   * @param typeParameters the type parameters
   * @param constraint the constraint
   * @param returnType the return type
   * @param parameters the MonoTypes of the parameters
   */
  public JavaMethodDefinition(
			      // Informations about the java method
			      String className,
			      String methodName,
			      List /* of String */ javaTypes,
			      // Bossa information
			      LocatedString name, 
			      Constraint constraint,
			      Monotype returnType,
			      List parameters,
			      boolean isStatic
			      )
  {
    super(name,constraint,returnType,parameters);
    this.className=className;
    this.methodName=methodName;
    this.javaTypes=javaTypes;
    
    flags=0;
    if(isStatic)
      {
	flags|=Access.STATIC;
	javaArity=arity;
      }
    else
      javaArity=arity-1;

    User.error(javaTypes.size()-1!=javaArity,
	       this.name,
	       "Native method "+this.name+" has not the same number of parameters in Java and Bossa !");
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
      else
	return ClassType.make(s);
  }
  
  protected gnu.bytecode.Type returnJavaType() 
  { return type((String)javaTypes.get(0)); }

  protected gnu.bytecode.Type[] argumentsJavaTypes()
  {
    gnu.bytecode.Type[] res=new ClassType[javaArity];
    for(int i=0;i<javaArity;i++)
      res[i]=type((String)javaTypes.get(i+1));
    
    return res;
  }

  public void compile(bossa.modules.Module module)
  {
    ClassType c=ClassType.make(className);
    dispatchMethod=new gnu.expr.PrimProcedure
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
  
  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    return
      "native "
      + type.codomain().toString()
      + " "
      + name
      + Util.map("<",", ",">",type.getTypeParameters())
      + type.getConstraint().toString()
      + "("
      + Util.map("",", ","",type.domain())
      + ");\n"
      ;
  }
}
