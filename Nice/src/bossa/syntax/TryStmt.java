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
import java.util.*;

import mlsub.typing.Typing;
import mlsub.typing.TypingEx;
import mlsub.typing.TypeConstructor;

import gnu.expr.TryExp;
import gnu.expr.CatchClause;

/**
   A try/catch/finally statement  

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public class TryStmt extends Statement
{
  public TryStmt(Statement body)
  {
    this.body = child(body);
  }

  public void setFinally(Statement body)
  {
    finallyBody = child(body);
  }
  
  public void addCatch(TypeIdent tc, LocatedString var, Statement body)
  {
    catches.add(child(new Catch(tc, var, body)));
  }
  
  public gnu.expr.Expression generateCode()
  {
    TryExp res = new TryExp
      (body.generateCode(), 
       (finallyBody==null ? null : finallyBody.generateCode()));

    CatchClause oldc = null;
    for(Iterator i = catches.iterator(); i.hasNext();)
      {
	Catch c = (Catch) i.next();
	
	CatchClause newc = c.clause();
	if (oldc!=null)
	  oldc.setNext(newc);
	else
	  res.setCatchClauses(newc);
	
	oldc = newc;
      }
    
    return res;
  }
  
  public String toString()
  {
    return "try ...";
  }

  Statement body;
  Statement finallyBody;
  List catches = new LinkedList();
  
  public class Catch extends Node
  {
    Catch(TypeIdent tc, LocatedString var, Statement body)
    {
      super(Node.down);

      this.exnVar = new MonoSymbol
	(var, 
	 new MonotypeConstructor(tc, null, tc.location()));
      this.addChild(exnVar);
      
      this.tc = tc;
      this.typeLocation = tc.location();
      this.var = var;
      this.body = this.child(body);
    }

    CatchClause clause()
    {
      try{
	Typing.leq(t, ConstantExp.throwableTC());
      }
      catch(TypingEx e){
	User.error(typeLocation, tc + " is not catchable");
      }

      CatchClause res = new CatchClause
	(var.toString(), 
	 (gnu.bytecode.ClassType) nice.tools.code.Types.javaType(t));
      res.outer = Statement.currentScopeExp;
      
      exnVar.setDeclaration(res.getDeclaration());
      res.setBody(body.generateCode());
      
      return res;
    }
    
    MonoSymbol exnVar;
    TypeIdent tc;
    TypeConstructor t;
    private LocatedString var;
    Statement body;
    private Location typeLocation;
  }
}
