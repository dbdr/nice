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
import java.util.*;

import mlsub.typing.*;

/**
   A class definition which reflects an existing native java class.
   
   This is usefull when you want to add type parameters
   to an existing java class.
   
   @version $Date$
   @author Daniel Bonniot
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
		   List typeParametersVariances,
		   List extensions, List implementations, List abstractions,
		   LocatedString javaName)
  {
    super(name, isFinal, isAbstract, isInterface,
	  typeParameters, typeParametersVariances,
	  extensions, implementations, abstractions);

    this.javaName = javaName;
    if(javaName == null) // primitive type
      {
	isPrimitive = true;
	gnu.bytecode.Type t = ConstantExp.registerPrimType(name.toString(),tc);
	if (t == null)
	  User.error(this, name+" is not a known primitive type");
	setJavaType(t);
      }
    else // wrapper for a java class
      {
	TypeConstructor old = 
	  JavaClasses.setTypeConstructorForJavaClass
	  (this.tc, javaName.toString());

	if(old!=null)
	  User.error(this, javaName + 
		     " was already associated with the Nice class " + old);
      }
  }

  void resolve()
  {
    super.resolve();

    if(isPrimitive) return;
    
    java.lang.Class refClass 
      = nice.tools.code.Types.lookupJavaClass(javaName.toString());
    
    if (refClass == null)
      User.error(javaName, javaName + " was not found in classpath");
    
    setJavaType(gnu.bytecode.Type.make(refClass));
  }

  private boolean isPrimitive;
  
  public void compile()
  {
    // nothing to do.
  }
  
  public void printInterface(java.io.PrintWriter s)
  {
    super.printInterface(s);
    s.print(" = native "+(javaName == null ? "" : javaName.toString())+";\n");
  }
  
  /** Is null if this class represents a primitive type. */
  private LocatedString javaName;
}
