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

/**
   A Nice module.

   @version $Date$
   @author Daniel Bonniot
 */

public interface Module extends mlsub.compilation.Module
{
  abstract String[] listImplicitPackages();

  abstract String mangleName(String name);

  boolean isRunnable();
  void isRunnable(boolean isRunnable);

  abstract boolean generatingBytecode();
  abstract ClassType getOutputBytecode();
  abstract ClassType getReadBytecode();
  abstract Method addDispatchMethod(NiceMethod def);
  abstract Method getMainAlternative();
  void setMainAlternative(Method main);
  abstract void compileMethod(gnu.expr.LambdaExp meth);
  abstract void addClassInitStatement(gnu.expr.Expression exp);
  abstract ClassType createClass(String name);
  abstract void addClass(ClassType c);
  gnu.expr.ScopeExp getPackageScopeExp();
}
