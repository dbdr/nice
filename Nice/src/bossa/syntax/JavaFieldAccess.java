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
    super(name, cst, returnType, parameters);
    this.className = className;
    this.fieldName = fieldName;
    
    if(arity != 0)
      User.error(name, name + " should have no parameters");    
  }
  
  private JavaFieldAccess(LocatedString className,String fieldName,
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
  
  static MethodDeclaration make(Field f)
  {
    try{
      f.getReflectField();
    }
    catch(java.lang.NoSuchFieldException e){
      User.error("Field " + f + " does not exist");
    }
    
    try{
      mlsub.typing.Monotype[] params;
      if(!f.getStaticFlag())
	{
	  params = new mlsub.typing.Monotype[]
	  { nice.tools.code.Types.getMonotype(f.getDeclaringClass()) };
	}
      else
	params = null;
    
      JavaFieldAccess res = new JavaFieldAccess
	(new LocatedString(f.getDeclaringClass().getName(),
			   Location.nowhere()),
	 f.getName(),
	 new LocatedString(f.getName(),
			   Location.nowhere()),
	 null,
	 nice.tools.code.Types.getMonotype(f.getType()), 
	 params);

      res.field = f;
      
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
  }
  
  /****************************************************************
   * Reflection
   ****************************************************************/

  Field getField(LocatedString javaClass, String name)
  {
    ClassType c = null;
    try { 
      c = (ClassType) nice.tools.code.Types.type(javaClass.toString()); 
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
      return "JavaFieldAccess " + symbol.name;
    else
      return interfaceString();
  }
}
