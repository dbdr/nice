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

import nice.tools.code.Types;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;
import bossa.util.Location;

/**
   A native java static field access.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class JavaFieldAccess extends FieldAccess
{
  public JavaFieldAccess
    (LocatedString className,String fieldName,
     LocatedString name,Constraint cst,
     Monotype returnType, FormalParameters parameters)
  {
    super(name, cst, parameters, returnType);
    this.className = className;
    this.fieldName = fieldName;
  }
  
  private JavaFieldAccess(LocatedString className,String fieldName,
			  LocatedString name,
			  mlsub.typing.Constraint cst,
			  mlsub.typing.Monotype returnType, 
			  mlsub.typing.Monotype[] parameters)
  {
    super(name, cst, parameters, returnType);
    this.className = className;
    this.fieldName = fieldName;
  }
  
  static MethodDeclaration make(Field f)
  {
    try{
      mlsub.typing.Monotype[] params;
      if(!f.getStaticFlag())
	{
	  params = new mlsub.typing.Monotype[]
	  { Types.monotype(f.getDeclaringClass(), true) };
	}
      else
	params = null;
    
      JavaFieldAccess res = new JavaFieldAccess
	(new LocatedString(f.getDeclaringClass().getName(),
			   Location.nowhere()),
	 f.getName(),
	 new LocatedString(f.getName(), Location.nowhere()),
	 null,
	 Types.monotype(f.getType()), 
	 params);

      res.field = f;
      res.fieldDecl = new Declaration(f.getName(), f);

      if (Debug.javaTypes)
	Debug.println("Loaded field " + res);
      
      return res;
    }
    catch(Types.ParametricClassException e){
      return null;
    }
    catch(Types.NotIntroducedClassException e){
      return null;
    }
  }
  
  void buildScope(VarScope outer, TypeScope typeOuter)
  {
    super.buildScope(outer, typeOuter);

    // We put this here, since we need 'module' to be computed
    // since it is used to open the imported packages.
    // The registration must be done before resolution.
    if (field == null)
      {
	field = getField(className,fieldName);

	if(field == null)
	  User.error(this,
		     "Field "+fieldName+" not found in class "+className);
    
	if (field.getStaticFlag())
	  {
	    if (arity != 0)
	      User.error(name, name + " should have no parameters");
	  }
	else
	  {
	    if (arity != 1)
	      User.error(name, name + " should have exactly one parameter");
	  }

	JavaClasses.registerNativeField(this, field);
      }
    fieldDecl = new Declaration(fieldName, field);
  }
  
  /****************************************************************
   * Reflection
   ****************************************************************/

  private Field field;

  Field getField(LocatedString javaClass, String name)
  {
    ClassType c = null;
    try { 
      c = (ClassType) Types.type(javaClass); 
      if (c == null)
	User.error(javaClass,"Class " + javaClass + " not found");
    }
    catch(ClassCastException e) { 
      User.error(javaClass, 
		 javaClass + " is a primitive type, it has no field");
    }
    
    // remembers the fully qualified name
    className.content = c.getName();
    
    for(Field f = c.getFields(); f != null; f = f.getNext())
      if(name.equals(f.getName()))
	return f;
    
    return null;
  }

  /** The java class this method is defined in */
  LocatedString className;
  
  private String fieldName;

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(interfaceString() + "\n");
  }
  
  private String interfaceString()
  {
    return
      super.toString() + " = native "
      + className + "." + (field==null ? fieldName : field.getName())
      + ";"
      ;
  }

  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    if (getType() == null)
      return "JavaFieldAccess " + name;
    else
      return interfaceString();
  }
}
