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

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
*/
public class JavaMethod extends MethodDeclaration
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
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public static final String fullNamePrefix = "JAVA:";

  /** @return a string that uniquely represents this method */
  public String getFullName()
  {
    return fullNamePrefix + name + ':' + getType();
  }

  public final LambdaExp getLambda()
  {
    return nice.tools.code.Gen.dereference(getCode());
  }

  Method reflectMethod;

  public gnu.bytecode.Type javaReturnType() 
  { return reflectMethod.getReturnType(); }

  public gnu.bytecode.Type[] javaArgTypes()
  { return reflectMethod.getParameterTypes(); }

  protected gnu.expr.Expression computeCode()
  {
    return new QuoteExp(new PrimProcedure(reflectMethod));
  }

  gnu.expr.Expression getCode() 
  {
    return nice.tools.code.Gen.wrapInLambda(new PrimProcedure(reflectMethod));
  }
  
  gnu.expr.Expression getConstructorInvocation()
  {
    return new QuoteExp(new InitializeProc(reflectMethod));
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

  private boolean registered;

  public void registerForDispatch()
  {
    if (registered)
      return;

    bossa.link.Dispatch.register(this);
    registered = true;
  }
}
