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
   Arguments of a function call.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */

public class Arguments
{
  public static class Argument
  {
    public Argument(Expression value)
    {
      this.value = value;
    }
    
    public Argument(Expression value, LocatedString name)
    {
      this.value = value;
      this.name = name;
    }

    public String toString()
    {
      return (name == null ? "" : name + ":" ) + value;
    }
    
    Expression value;
    LocatedString name;
  }
  
  public Arguments(java.util.List args)
  {
    this.arguments = (Argument[]) args.toArray(new Argument[args.size()]);
  }

  public Arguments(Argument[] arguments)
  {
    this.arguments = arguments;
  }

  public static Arguments noArguments = new Arguments(new Argument[0]);

  public void addReceiver(Expression value)
  {
    if (arguments[0] != null)
      Internal.error("No room for \"receiver\"");
    
    arguments[0] = new Argument(value, null);
  }

  void add(Expression arg)
  {
    Argument[] newArgs = new Argument[arguments.length + 1];
    System.arraycopy(arguments, 0, newArgs, 0, arguments.length);
    newArgs[arguments.length] = new Argument(arg);
    arguments = newArgs;
  }

  int size()
  {
    return arguments.length;
  }
  
  Argument get(int num)
  {
    return arguments[num];
  }
  
  Expression getExp(int num)
  {
    return arguments[num].value;
  }

  void setExp(int num, Expression value)
  {
    arguments[num].value = value;
  }

  void noOverloading()
  {
    for (int i = arguments.length; --i>=0; )
      arguments[i].value = arguments[i].value.noOverloading();
  }
  
  void computeTypes()
  {
    for (int i = arguments.length; --i>=0; )
      arguments[i].value.getType();
  }
  
  Expression[] inOrder()
  {
    if (arguments.length == 0)
      return Expression.noExpressions;

    Expression[] res = new Expression[arguments.length];
    
    for (int i = arguments.length; --i>=0; )
      res[i] = arguments[i].value;

    return res;
  }
  
  PackageExp packageExp()
  {
    noOverloading();
    // case where the parameters is a package, or a package prefix
    if(arguments.length == 1)
      {
	Expression param0 = getExp(0);
	if(param0 instanceof PackageExp)
	  return (PackageExp) param0;
      }
    return null;
  }
  
  gnu.bytecode.ClassType staticClass()
  {
    gnu.bytecode.ClassType res = getExp(0).staticClass();
    
    if (res != null)
      // the first argument was fake, remove it
      {
	Argument[] newArgs = new Argument[arguments.length - 1];
	System.arraycopy(arguments, 1, newArgs, 0, newArgs.length);
	arguments = newArgs;
      }
    
    return res;
  }

  java.util.List applicationExpressions = new java.util.ArrayList();
  
  Expression[] getExpressions(int num)
  {
    return (Expression[]) applicationExpressions.get(num);
  }
  
  /**
     return true if there are arity non-tagged arguments.
  */
  boolean plainApplication(int arity)
  {
    if (arguments.length != arity)
      return false;
    for (int i = 0; i<arguments.length; i++)
      if (arguments[i].name != null)
	return false;
    applicationExpressions.add(inOrder());
    return true;
  }
  
  public gnu.expr.Expression[] compile()
  {
    return null; //Expression.compile(arguments);
  }

  public String toString()
  {
    return "(" + Util.map("", ", ", "", arguments) + ")";
  }

  public String toStringInfix()
  {
    StringBuffer res = new StringBuffer("(");
    for (int i = 1; i<arguments.length; i++)
      res.append(arguments[i].toString() + 
		 (i + 1 < arguments.length ? ", " : ""));
    return res.append(")").toString();
  }

  public String printTypes()
  {
    StringBuffer res = new StringBuffer("(");
    for (int i = 0; i<arguments.length; i++)
      res.append((arguments[i].name == null ? "" : arguments[i].name + ":") + 
		 arguments[i].value.getType() + 
		 (i + 1 < arguments.length ? ", " : ""));
    return res.append(")").toString();
  }

  String explainNoMatch()
  {
    for (int i = 0; i < arguments.length; i++)
      if (arguments[i].name != null)
	return " has compatible named arguments";
    
    return " has " + arguments.length + " arguments";
  }
  
  Argument[] arguments;
}
