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
    getProcedure();
  }

  private gnu.mapping.Procedure getProcedure()
  {
    if (this.procedure != null)
      // Already done.
      return this.procedure;

    Class refClass = null;
    try{
      refClass = findClass(inlineProcedure.toString());
    }
    catch(ClassNotFoundException e){
      User.error(inlineProcedure, 
		 "Inlined method " + inlineProcedure + " was not found");
    }

    Method m = null;

    try{
      m = refClass.getMethod("create", string1);
      if (! Modifier.isStatic(m.getModifiers()))
	throw new NoSuchMethodException();
    }
    catch(NoSuchMethodException e){
      User.error(inlineProcedure,
		 "Inlined method " + inlineProcedure +
		 " has no static create(String)");
    }

    Object o = null;
    try{
      o = m.invoke(null, new Object[]{ parameter });
    }
    catch(InvocationTargetException e){
      User.error(inlineProcedure,
		 "Inlined method " + inlineProcedure +
		 ": " + e.getMessage());
    }
    catch(IllegalAccessException e){
      User.error(inlineProcedure,
		 "Inlined method " + inlineProcedure +
		 ": could not call create method",
		 e.getMessage());
    }
    
    if (!(o instanceof gnu.mapping.Procedure))
      User.error(inlineProcedure,
		 "Inlined method " + inlineProcedure + 
		 " should be a subclass of gnu.mapping.Procedure");

    if (!(o instanceof gnu.expr.Inlineable))
      User.warning(inlineProcedure,
		   "Inlined method " + inlineProcedure + 
		   " cannot be inlined, but will be called anyway");

    this.procedure = (gnu.mapping.Procedure) o;
    return this.procedure;
  }

  private Class findClass(String name) throws ClassNotFoundException
  {
    if (loader == null)
      return Class.forName(name);
    else
      return loader.loadClass(name);
  }

  static ClassLoader loader;

  static
  {
    String inlinedMethodsRepository = System.getProperty("nice.inlined");
    if (inlinedMethodsRepository != null)
      try {
        inlinedMethodsRepository = "file://" + 
          new java.io.File(inlinedMethodsRepository).getAbsolutePath() + '/';
        loader = new java.net.URLClassLoader
          (new java.net.URL[]{ new java.net.URL(inlinedMethodsRepository) })
          {
            protected Class loadClass(String name, boolean resolve)
              throws ClassNotFoundException
            {
              /* Change the default behviour, which is to look up the 
                 parent classloader first. Instead, look it up after this one,
                 so that the inlined methods are found here, but the
                 interfaces they implement are found in the system classloader,
                 so that the casts for using them succeed.
              */
              Class res = findLoadedClass(name);

              if (res == null)
                try {
                  res = this.findClass(name);
                } 
                catch (ClassNotFoundException ex) {}

              if (res == null)
                {
                  ClassLoader parent = getParent();
                  // A JVM may represent the system classloader by null.
                  if (parent == null)
                    parent = ClassLoader.getSystemClassLoader();
                  res = parent.loadClass(name);
                }

              if (resolve && res != null)
                resolveClass(res);

              return res;
            }
          };
      }
      catch (java.net.MalformedURLException ex) {
        bossa.util.Internal.warning
          ("Incorrect location for inlined methods: " + 
           inlinedMethodsRepository);
      }
  }

  protected gnu.expr.Expression computeCode()
  {
    return new gnu.expr.QuoteExp(getProcedure());
  }

  gnu.expr.Expression getCode() 
  {
    return nice.tools.code.Gen.wrapInLambda(getProcedure());
  }

  private static Class[] string1 = new Class[]{ "".getClass() };
  private LocatedString inlineProcedure;
  private String parameter;
  private gnu.mapping.Procedure procedure;

  void checkSpecialRequirements(Expression[] arguments)
  {
    if (getProcedure() instanceof bossa.syntax.Macro)
      ((Macro) procedure).checkSpecialRequirements(arguments);
  }

  public void printInterface(java.io.PrintWriter s)
  {
    s.print(super.toString() +
	    " = inline " + 
	    inlineProcedure + 
	    (parameter!=null ? "(\""+parameter+"\");\n" : ";\n"));
  }
}
