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

import gnu.bytecode.*;
import gnu.expr.*;

import bossa.util.Location;

import java.util.*;

/**
   A Native Java Method.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
class JavaMethod extends MethodDeclaration
{
  JavaMethod(LocatedString name, Constraint constraint,
	     Monotype returnType, FormalParameters parameters)
  {
    super(name,constraint,returnType,parameters);
  }

  JavaMethod
    (
     LocatedString name, 
     mlsub.typing.Polytype type,
     Method reflectMethod
    )
  {
    super(name, null, type);
    
    this.reflectMethod = reflectMethod;
  }

  static JavaMethod make(Method m, boolean constructor)
  {
    JavaMethod res;
    mlsub.typing.Polytype type = nice.tools.code.Import.type(m);

    // We could not turn the bytecode type into a Nice type.
    if (type == null)
      return null;
    
    if (constructor)
      res = new JavaConstructor
	(new LocatedString
	 ("new " + m.getDeclaringClass().getName(), Location.nowhere()),
	 type, m);
    else
      res = new JavaMethod(new LocatedString(m.getName(), Location.nowhere()),
			   type, m);
    return res;
  }
  
  boolean hasThis()
  {
    return true;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  Method reflectMethod;

  public gnu.bytecode.Type javaReturnType() 
  { return reflectMethod.getReturnType(); }

  public gnu.bytecode.Type[] javaArgTypes()
  { return reflectMethod.getParameterTypes(); }

  protected gnu.expr.Expression computeCode()
  {
    return new QuoteExp(new PrimProcedure(reflectMethod));
  }
  
  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter w)
  {
    Internal.error("Automatic java methods should not be exported");
  }

  /****************************************************************
   * List of implementations
   ****************************************************************/

  Iterator getImplementations()
  {
    return implementations.iterator();
  }

  private LinkedList implementations = new LinkedList();

  void addImplementation(MethodBodyDefinition impl)
  {
    implementations.add(impl);
  }
}
