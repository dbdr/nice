/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : TryExp.java
// Created : Thu May 25 12:34:19 2000 by Daniel Bonniot
//$Modified: Thu Jun 08 17:13:43 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import java.util.*;

import mlsub.typing.Typing;
import mlsub.typing.TypingEx;
import mlsub.typing.TypeConstructor;

import gnu.expr.TryExp;
import gnu.expr.CatchClause;

/**
 * A try/catch/finally statement
 * 
 * @author Daniel Bonniot
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
    catchs.add(child(new Catch(tc, var, body)));
  }
  
  public gnu.expr.Expression generateCode()
  {
    TryExp res = new TryExp
      (body.generateCode(), 
       (finallyBody==null ? null : finallyBody.generateCode()));

    CatchClause oldc = null;
    for(Iterator i=catchs.iterator(); i.hasNext();)
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

  private Statement body;
  private Statement finallyBody;
  private List catchs = new LinkedList();
  
  public class Catch extends Node
  {
    Catch(TypeIdent tc, LocatedString var, Statement body)
    {
      super(Node.down);

      this.exnVar = new MonoSymbol
	(var, 
	 new MonotypeConstructor(tc, null, tc.location()));
      addChild(exnVar);
      
      this.tc = tc;
      this.typeLocation = tc.location();
      this.var = var;
      this.body = child(body);
    }

    void findJavaClasses()
    {
      t = tc.resolveToTC(typeScope);
      tc = null;
    }
    
    void resolve()
    {
      // done in findJavaClasses
      //tc = tc.resolve(typeScope);
    }
    
    CatchClause clause()
    {
      try{
	Typing.leq(t, ThrowStmt.throwableTC());
      }
      catch(TypingEx e){
	User.error(typeLocation, tc + " is not catchable");
      }

      CatchClause res = new CatchClause
	(var.toString(), 
	 (gnu.bytecode.ClassType) bossa.CodeGen.javaType(t));
      res.outer = Statement.currentScopeExp;
      
      exnVar.setDeclaration(res.getDeclaration());
      res.setBody(body.generateCode());
      
      return res;
    }
    
    private MonoSymbol exnVar;
    private TypeIdent tc;
    private TypeConstructor t;
    private LocatedString var;
    private Statement body;
    private Location typeLocation;
  }
}
