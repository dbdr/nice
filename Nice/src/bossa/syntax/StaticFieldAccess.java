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

// File    : StaticFieldAccess.java
// Created : Thu Jul 01 18:12:46 1999 by bonniot
//$Modified: Mon Aug 07 15:31:40 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;
import bossa.util.Location;

/**
 * A native java static field access.
 */
public class StaticFieldAccess extends MethodDefinition
{
  public StaticFieldAccess(LocatedString className,String fieldName,
			   LocatedString name,Constraint cst,
			   Monotype returnType, List parameters)
  {
    super(name,cst,returnType,new ArrayList(0));
    this.className = className;
    this.fieldName = fieldName;
    
    if(parameters.size()!=0)
      User.error(name,
		 name+" should have no parameters");    
  }
  
  private StaticFieldAccess(LocatedString className,String fieldName,
			    LocatedString name,
			    mlsub.typing.Constraint cst,
			    mlsub.typing.Monotype returnType, 
			    mlsub.typing.Monotype[] parameters)
  {
    super(name, null, null, null);
    this.className = className;
    this.fieldName = fieldName;
    
    setLowlevelTypes(cst, parameters, returnType);
  }
  
  static MethodDefinition make(Field f)
  {
    try{
      mlsub.typing.Monotype[] params;
      if(!f.getStaticFlag())
	{
	  params = new mlsub.typing.Monotype[]
	  { nice.tools.code.Types.getMonotype(f.getDeclaringClass()) };
	}
      else
	params = null;
    
      StaticFieldAccess res = new StaticFieldAccess
	(new LocatedString(f.getDeclaringClass().getName(),
			   Location.nowhere()),
	 f.getName(),
	 new LocatedString(f.getName(),
			   Location.nowhere()),
	 null,
	 nice.tools.code.Types.getMonotype(f.getType()), 
	 params);
      try{
	res.field = f.getReflectField();
      }
      catch(java.lang.NoSuchFieldException e){
	User.error("Field "+f+" does not exist");
      }
    
      return res;
    }
    catch(nice.tools.code.Types.ParametricClassException e){
      return null;
    }
    catch(nice.tools.code.Types.NotIntroducedClassException e){
      return null;
    }
  }
  
  public void createContext()
  {
    super.createContext();

    // We put this here, since we need 'module' to be computed
    // since it is used to open the imported packages.
    this.field = getField(className,fieldName);

    if(field==null)
      User.error(this,
		 "Field "+fieldName+" not found in class "+className);

    if(!java.lang.reflect.Modifier.isStatic(field.getModifiers()))
      User.error(this,
		 fieldName+" should be a static field");
  }
  
  /****************************************************************
   * Reflection
   ****************************************************************/

  java.lang.reflect.Field getField(LocatedString javaClass, String name)
  {
    Class c = nice.tools.code.Types.lookupJavaClass(javaClass.toString());

    if(c==null)
      User.error(javaClass,"Class "+javaClass+" not found");
    
    // remembers the fully qualified name
    className.content = c.getName();
    
    java.lang.reflect.Field[] fields = c.getFields();
    
    for(int i = 0;i<fields.length;i++)
      if(name.equals(fields[i].getName()))
	return fields[i];
    
    return null;
  }

  /** The java class this method is defined in */
  LocatedString className;
  
  private String fieldName;
  
  java.lang.reflect.Field field;

  /****************************************************************
   * Code generation
   ****************************************************************/

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    return new gnu.kawa.reflect.StaticGet
      ((ClassType) gnu.bytecode.Type.make(field.getDeclaringClass()),
       field.getName(),
       Type.make(field.getType()),field.getModifiers());
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
    return
      mlsub.typing.Constraint.toString(getType().getConstraint())
      + getType().codomain().toString()
      + " "
      + symbol.name
      + "("
      + Util.map("",", ","",getType().domain())
      + ") = native "
      + className + "." + (field==null ? fieldName : field.getName())
      + ";\n"
      ;
  }

  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    if (getType() == null)
      return "StaticFieldAccess " + symbol.name;
    else
      return interfaceString();
  }
}
