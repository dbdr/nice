/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 2000                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import java.util.*;
import bossa.util.User;

import java.lang.reflect.*;

/**
   A method that is compiled by inlining code.

   The inlining class can be defined by the user.
   It keeps the core compiler small, while having very good efficiency.

   It has to be a subclass of <code>gnu.mapping.Procedure</code>, 
   and should implement interface <code>gnu.expr.Inlineable</code>
   to be actually inlined.
   
   @version $Date$
   @author Daniel Bonniot
*/
public class InlinedMethod extends MethodDeclaration
{
  public InlinedMethod(LocatedString name, 
		       Constraint constraint,
		       Monotype returnType,
		       FormalParameters parameters,
		       LocatedString inlineProcedure,
		       String parameter)
  {
    super(name, constraint, returnType, parameters);

    this.inlineProcedure = inlineProcedure;
    this.parameter = parameter;
  }

  void typecheck()
  {
    super.typecheck();
    // forces the reflection search
    getDispatchMethod();
  }

  protected gnu.mapping.Procedure computeDispatchMethod()
  {
    Class refClass;

    String name = inlineProcedure.toString();
    refClass = nice.tools.code.Types.lookupQualifiedJavaClass(name);

    if (refClass == null)
      User.error(inlineProcedure, 
		 "Inlined method " + inlineProcedure + " was not found");

    Method m = null;
    Object[] params = new Object[]{ parameter };

    try{
      m = refClass.getMethod("create", string1);
    }
    catch(NoSuchMethodException e){
      User.error(inlineProcedure,
		 "Inlined method " + inlineProcedure +
		 " has no static create(String)");
    }

    Object o = null;
    try{
      o = m.invoke(null, params);
    }
    catch(InvocationTargetException e){
      User.error(inlineProcedure,
		 "Inlined emthod " + inlineProcedure +
		 ": could not call create method");
    }
    catch(IllegalAccessException e){
      User.error(inlineProcedure,
		 "Inlined emthod " + inlineProcedure +
		 ": could not call create method");
    }
    
    if (!(o instanceof gnu.mapping.Procedure))
      User.error(inlineProcedure,
		 "Inlined method " + inlineProcedure + 
		 " should be a subclass of gnu.mapping.Procedure");

    if (!(o instanceof gnu.expr.Inlineable))
      User.warning(inlineProcedure,
		   "Inlined method " + inlineProcedure + 
		   " cannot be inlined, but will be called anyway");

    return (gnu.mapping.Procedure) o;
  }

  private static Class[] string1 = new Class[]{ "".getClass() };
  private LocatedString inlineProcedure;
  private String parameter;

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(super.toString() +
	    " = inline " + 
	    inlineProcedure + 
	    (parameter!=null ? "(\""+parameter+"\");\n" : ";\n"));
  }
}
