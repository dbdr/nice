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

// File    : ClassExp.java
// Created : Wed Mar 29 14:30:13 2000 by Daniel Bonniot
//$Modified: Wed Jul 26 14:20:34 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * A constant expression representing a class.
 *
 * For instance, in the expression 'java.lang.System.arraycopy(...)',
 * java.lang.System is a ClassExp, and is considered temporarilly
 * as an additional argument.
 * 
 * @author Daniel Bonniot
 */

public final class ClassExp extends Expression
{
  ClassExp(gnu.bytecode.ClassType javaClass)
  {
    this.javaClass = javaClass;
  }

  void computeType()
  {
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
  static Expression create(PackageExp p, String ident)
  {
    return create(p.getName() + "." + ident, p.location());
  }
  
  static Expression create(LocatedString ident)
  {
    return create(ident.toString(), ident.location());
  }
  
  static Expression create(String ident, Location loc)
  {
    Expression res = null;

    mlsub.typing.TypeConstructor tc = (mlsub.typing.TypeConstructor)
      Node.getGlobalTypeScope().lookup(ident);

    if(tc != null)
      {
	gnu.bytecode.Type type = bossa.CodeGen.javaType(tc);
	// type might not be a class
	// for instance if the ident was "int"
	if (type instanceof gnu.bytecode.ClassType)
	  res = new ClassExp((gnu.bytecode.ClassType) type);
      }

    if(res == null)
      res = new PackageExp(ident);

    res.setLocation(loc);
    
    return res;
  }
  
  /****************************************************************
   * Misc.
   ****************************************************************/

  public String toString()
  {
    return "Constant class "+javaClass.getName();
  }

  gnu.bytecode.ClassType staticClass()
  {
    return javaClass;
  }
  
  private gnu.bytecode.ClassType javaClass;
}
