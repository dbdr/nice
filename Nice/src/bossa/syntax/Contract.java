/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

/**
   The contract of a method.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

import bossa.util.*;
import java.util.*;

public class Contract
{
  public void addElement(Expression condition, Expression name, boolean precond)
  {
    if (precond)
      {
        if (name == null) addRequire(condition);
        else addRequire(condition, name);
      }
    else
      {
        if (name == null) addEnsure(condition);
        else addEnsure(condition, name);
      }
  }

  public void addRequire(Expression condition)
  {
    pre.add(bossa.syntax.dispatch.createCallExp(symbol(assertName, condition), condition));
    requireRepr.append(condition).append(',');
  }

  public void addRequire(Expression condition, Expression name)
  {
    pre.add(bossa.syntax.dispatch.createCallExp(symbol(assertName, condition), condition, name));
    requireRepr.append(condition).append(':').append(name).append(',');
  }

  public void addEnsure(Expression condition)
  {
    post.add(bossa.syntax.dispatch.createCallExp(symbol(assertName, condition), condition));
    ensureRepr.append(condition);
  }

  public void addEnsure(Expression condition, Expression name)
  {
    post.add(bossa.syntax.dispatch.createCallExp(symbol(assertName, condition), condition, name));
    ensureRepr.append(condition).append(':').append(name).append(',');
  }

  private static final String assertName = "alwaysAssert";

  private List pre  = new LinkedList();
  private List post = new LinkedList();

  private StringBuffer requireRepr = new StringBuffer("requires ");
  private StringBuffer ensureRepr = new StringBuffer("ensures ");

  private Expression symbol(String name, Located loc)
  {
    return dispatch.createIdentExp(new LocatedString(name, loc.location()));
  }

  void resolve(VarScope scope, TypeScope typeScope, 
	       mlsub.typing.Monotype resultType, 
               Location location)
  {
    preExp = new Expression[pre.size()];
    int n = 0;
    for (Iterator i = pre.iterator(); i.hasNext();)
      preExp[n++] = dispatch.analyse((Expression) i.next(),
				     scope, typeScope);

    if (post.size() == 0)
      {
        postExp = Expression.noExpressions;
        return;
      }

    if (! nice.tools.typing.Types.isVoid(resultType))
      result = new MonoSymbol(new LocatedString("result", location), 
                              resultType) {
	  boolean isAssignable()
	  { return false; }

	  gnu.expr.Expression compile()
	  { return gnu.expr.CheckContract.result; }
	};

    try {
      if (result != null)
	scope.addSymbol(result);
      postExp = new Expression[post.size()];
      n = 0;
      for (Iterator i = post.iterator(); i.hasNext();)
	postExp[n++] = dispatch.analyse((Expression) i.next(), 
					scope, typeScope);
    }
    finally {
      if (result != null)
	scope.removeSymbol(result);
    }
  }

  private Expression[] preExp, postExp;
  private MonoSymbol result;

  void typecheck()
  {
    for (int i = 0; i < preExp.length; i++)
      dispatch.typecheck(preExp[i]);

    for (int i = 0; i < postExp.length; i++)
      dispatch.typecheck(postExp[i]);
  }

  public gnu.expr.Expression compile(gnu.expr.Expression body)
  {
    return new gnu.expr.CheckContract(Expression.compile(preExp), 
				      Expression.compile(postExp), 
				      body);
  }

  public String toString()
  {
    StringBuffer res = new StringBuffer();
    if (preExp != null && preExp.length > 0)
      res.append(requireRepr.toString());
    if (postExp != null && postExp.length > 0)
      res.append(ensureRepr.toString());
    return res.toString();
  }

  public static final Contract noContract = new Contract() {
      void resolve(VarScope scope, TypeScope typeScope) {}
      void typecheck() {}

      public gnu.expr.Expression compile(gnu.expr.Expression body)
      {
	return body;
      }

      public String toString() { return ""; }
    };
}
