/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

/**
   A Nice module.

   @version $Date$
   @author Daniel Bonniot
 */

public interface Module extends mlsub.compilation.Module
{
  String[] listImplicitPackages();
  bossa.modules.Compilation compilation();
  boolean interfaceFile();

  /****************************************************************
   * Code generation
   ****************************************************************/

  gnu.bytecode.ClassType createClass(String name);
  void addGlobalVar(gnu.expr.Declaration declaration, boolean constant);
  gnu.bytecode.Method lookupDispatchClassMethod(String name, String attribute, String value);
  gnu.expr.ReferenceExp addMethod(gnu.expr.LambdaExp method, 
				  boolean packageMethod);
  gnu.expr.ClassExp getClassExp(Object def);
  void addUserClass(gnu.expr.ClassExp classe);
}
