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

package bossa.syntax;

import bossa.util.*;
import java.util.*;

import mlsub.typing.*;
import gnu.bytecode.ClassType;

/**
   A class definition which reflects an existing native java class.
   
   This is usefull when you want to add type parameters
   to an existing java class.
   
   @version $Date$
   @author Daniel Bonniot
*/

public class JavaClass extends ClassDefinition.ClassImplementation
{
  public JavaClass(ClassDefinition definition, LocatedString javaName)
  {
    this.definition = definition;
    this.javaName = javaName;

    lookup();
  }

  ClassDefinition definition;

  /**
     This must be called in a first pass, before bytecode types are implicitely
     loaded, so that the retyping is tyken into account.
  */
  private void lookup()
  {
    ClassType classType;
    try {
      classType = (ClassType) nice.tools.code.TypeImport.lookup(javaName);
    }
    catch(ClassCastException ex) {
      throw User.error(definition, javaName + " is a primitive type");
    }
    
    if (classType == null)
      User.error(javaName, javaName + " was not found in classpath");

    
    // Check that we don't give a wrong arity to a generic class.
    int nativeArity = classType.getArity();
    if (nativeArity != -1 && nativeArity != definition.tc.arity())
      User.error(definition, javaName + " has " + nativeArity +
		 " type parameters");
    
    TypeConstructor old = 
      JavaClasses.setTypeConstructorForJavaClass
      (definition.compilation(), definition.tc, classType);

    if (old != null)
      User.error(definition, javaName + 
		 " was already associated with the Nice class " + old);
    definition.setJavaType(classType);
  }

  void resolveClass()
  {
    JavaClasses.fetchMethods(definition.tc, 
			     (gnu.bytecode.ClassType) definition.getJavaType());
  }

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(" = native " + javaName.toString() + ";\n");
  }
  
  /** The qualified name of the existing java type. */
  private LocatedString javaName;
}
