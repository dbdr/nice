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

// File    : JavaClass.java
// Created : Wed Feb 02 16:20:12 2000 by Daniel Bonniot
//$Modified: Thu Feb 17 21:36:05 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

/**
 * A class definition which reflects an existing native java class.
 *
 * This is usefull when you want to add type parameters
 * to an existing java class.
 * 
 * @author Daniel Bonniot
 */

public class JavaClass extends ClassDefinition
{
  /**
   * Creates a java class definition.
   *
   * @param name the name of the class
   * @param isFinal 
   * @param isAbstract 
   * @param isInterface true iff the class is an "interface" 
       (which is not an "abstract interface").
       isInterface implies isAbstract.
   * @param typeParameters a list of type symbols
   * @param extensions a list of TypeConstructors
   * @param implementations a list of Interfaces
   * @param abstractions a list of Interfaces
   * @param javaName the name of the corresponding java class.
   it must be fully qualified, or the package must have been imported.
   */
  public JavaClass(LocatedString name, 
		   boolean isFinal, boolean isAbstract, 
		   boolean isInterface,
		   List typeParameters,
		   List extensions, List implementations, List abstractions,
		   LocatedString javaName)
  {
    super(name, isFinal, isAbstract, isInterface,
	  typeParameters, extensions, implementations, abstractions);
    this.javaName = javaName;

    JavaTypeConstructor.registerTypeConstructorForJavaClass
      (this.tc, javaName.toString());
  }

  public boolean isConcrete()
  {
    return true;
  }

  void resolve()
  {
    super.resolve();

    if(javaName.toString().equals("_Array"))
      javaType = new bossa.SpecialArray(gnu.bytecode.Type.pointer_type);
    else
      {
	java.lang.Class refClass 
	  = JavaTypeConstructor.lookupJavaClass(javaName.toString());
    
	if(refClass==null)
	  User.error(javaName,
		     javaName+" was not found");
    
	javaType = (gnu.bytecode.ClassType) gnu.bytecode.Type.make(refClass);
      }
  }
  
  private gnu.bytecode.Type javaType;
  
  gnu.bytecode.Type javaClass()
  {
    return javaType;
  }
  
  public void compile()
  {
    // nothing to do.
  }
  
  protected void addFields(gnu.bytecode.ClassType c)
  {
    //??
  }

  public void printInterface(java.io.PrintWriter s)
  {
    super.printInterface(s);
    s.print(" = native "+javaName+";\n");
  }
  
  private LocatedString javaName;
}
