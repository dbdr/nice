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
  public void addRequire(Expression condition)
  {
    pre.add(CallExp.create(symbol("assert", condition), condition));
  }

  public void addRequire(Expression condition, Expression name)
  {
    pre.add(CallExp.create(symbol("assert", condition), condition, name));
  }

  public void addEnsure(Expression condition)
  {
    post.add(CallExp.create(symbol("assert", condition), condition));
  }

  public void addEnsure(Expression condition, Expression name)
  {
    post.add(CallExp.create(symbol("assert", condition), condition, name));
  }

  private LinkedList pre  = new LinkedList();
  private LinkedList post = new LinkedList();

  private Expression symbol(String name, Located loc)
  {
    return new IdentExp(new LocatedString(name, loc.location()));
  }

  void resolve(VarScope scope, TypeScope typeScope, 
	       mlsub.typing.Monotype resultType)
  {
    preExp = new Expression[pre.size()];
    int n = 0;
    for (Iterator i = pre.iterator(); i.hasNext();)
      preExp[n++] = dispatch.analyse((Expression) i.next(),
				     scope, typeScope);

    if (! nice.tools.code.Types.isVoid(resultType))
      result = new MonoSymbol(resultName, resultType) {
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

  private static final LocatedString resultName = 
    new LocatedString("result", Location.nowhereAtAll());

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

  public static final Contract noContract = new Contract() {
      void resolve(VarScope scope, TypeScope typeScope) {}
      void typecheck() {}

      public gnu.expr.Expression compile(gnu.expr.Expression body)
      {
	return body;
      }
    };
}