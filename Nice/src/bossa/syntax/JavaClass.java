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

    TypeConstructor old = 
      JavaClasses.setTypeConstructorForJavaClass
      (definition.compilation(), definition.tc, javaName.toString());

    if (old != null)
      User.error(definition, javaName + 
		 " was already associated with the Nice class " + old);
  }

  ClassDefinition definition;

  void resolveClass()
  {
    java.lang.Class refClass 
      = nice.tools.code.Types.lookupJavaClass(javaName.toString());
    
    if (refClass == null)
      User.error(javaName, javaName + " was not found in classpath");
    
    definition.setJavaType(gnu.bytecode.Type.make(refClass));
    JavaClasses.fetchMethods(definition.tc, (gnu.bytecode.ClassType) definition.getJavaType());
  }

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(" = native " + javaName.toString() + ";\n");
  }
  
  /** The qualified name of the existing java type. */
  private LocatedString javaName;
}
