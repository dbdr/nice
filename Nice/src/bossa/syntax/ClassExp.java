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

/**
   A constant expression representing a class.
   
   For instance, in the expression 'java.lang.System.arraycopy(...)',
   java.lang.System is a ClassExp, and is considered temporarilly
   as an additional argument.   

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */

public final class ClassExp extends Expression
{
  private ClassExp(gnu.bytecode.ClassType javaClass)
  {
    this.javaClass = javaClass;
  }

  void computeType()
  {
    throw User.error(this, "Expression expected");
  }

  protected gnu.expr.Expression compile()
  {
    Internal.error("Constant class expression should not be compiled");
    return null;
  }
  
  /****************************************************************
   * Creation of ClassExp or PackageExp
   ****************************************************************/

  /**
     Creates a ClassExp, or a PackageExp.
  */
  static Expression create(LocatedString ident)
  {
    return create(null, ident);
  }
  
  /**
     @return the ClassExp or PackageExp representing [root].[name]
  */
  static Expression create(PackageExp root, LocatedString name)
  {
    String fullName = name.toString();
    if (root != null)
      fullName = root.name.append(".").append(fullName).toString();
    
    mlsub.typing.TypeConstructor tc = (mlsub.typing.TypeConstructor)
      Node.getGlobalTypeScope().lookup(fullName, name.location());

    if(tc != null)
      {
	gnu.bytecode.Type type = nice.tools.code.Types.javaType(tc);
	// type might not be a class
	// for instance if the ident was "int"
	if (type instanceof gnu.bytecode.ClassType)
	  {
	    Expression res = new ClassExp((gnu.bytecode.ClassType) type);
	    Location loc = name.location();
	    if (root != null && root.location() != null)
	      loc = root.location().englobe(loc);
	    res.setLocation(loc);
	    return res;
	  }
      }

    if (root != null)
      // name has been appended to root's name
      return root;

    root = new PackageExp(fullName);
    root.setLocation(name.location());
    return root;
  }
  
  /****************************************************************
   * Misc.
   ****************************************************************/

  public String toString()
  {
    return "Constant class " + javaClass.getName();
  }

  gnu.bytecode.ClassType staticClass()
  {
    return javaClass;
  }
  
  private gnu.bytecode.ClassType javaClass;
}
