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

  /****************************************************************
   * Code generation
   ****************************************************************/

  gnu.bytecode.ClassType createClass(String name);
  gnu.expr.Declaration addGlobalVar(String name, gnu.bytecode.Type type);
  gnu.expr.Expression getDispatchMethod(NiceMethod def);
  gnu.expr.Expression lookupPackageMethod(String methodName, String type);
  gnu.expr.ReferenceExp addMethod(gnu.expr.LambdaExp method, boolean packageMethod);
  void addImplementationClass(gnu.expr.ClassExp classe);
}
