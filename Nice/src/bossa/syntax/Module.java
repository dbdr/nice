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

import java.util.Iterator;
import gnu.bytecode.ClassType;
import gnu.bytecode.Method;
import gnu.bytecode.Type;

/**
   A Nice module.

   @version $Date$
   @author Daniel Bonniot
 */

public interface Module extends mlsub.compilation.Module
{
  String[] listImplicitPackages();

  String mangleName(String name);

  boolean isRunnable();
  void isRunnable(boolean isRunnable);

  boolean generatingBytecode();
  ClassType getOutputBytecode();
  ClassType getReadBytecode();
  Method addDispatchMethod(NiceMethod def);
  Method addPackageMethod(String name, Type[] argTypes, Type retType);
  Method getMainAlternative();
  void setMainAlternative(Method main);
  void compileMethod(gnu.expr.LambdaExp meth);
  void addClassInitStatement(gnu.expr.Expression exp);
  ClassType createClass(String name);
  void addClass(ClassType c);
  gnu.expr.ScopeExp getPackageScopeExp();
}
