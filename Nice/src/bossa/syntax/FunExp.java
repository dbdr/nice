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

// File    : FunExp.java
// Created : Mon Jul 12 15:09:50 1999 by bonniot
//$Modified: Tue Jul 27 10:20:26 1999 by bonniot $
// Description : A functional expression

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class FunExp extends Expression
{
  public FunExp(Collection typeParameters, 
		Constraint cst, Collection formals, List body)
  {
    this.typeParameters=typeParameters;
    this.formals=formals;
    this.constraint=cst;
    this.body=new Block(body);
  }

  Expression resolve(VarScope s,TypeScope ts)
  {
    ts=TypeScope.makeScope(ts,constraint.binders);
    Node.buildScope(s,ts,formals);
    VarSymbol.resolveScope(formals);
    try{
      body.buildScope(VarScope.makeScope(s,formals),ts);
    }
    catch(DuplicateIdentEx e){
      User.error(this,e.ident+" was defined twice in function");
    }
    
    body.resolveScope();
    return this;
  }

  Type getType()
  {
    Type returnType=body.getType();
    
    User.error(returnType==null,"The last statement of "+this+
	       "must be a return statement");
 
    Collection tp=returnType.getTypeParameters();
    if(tp==null)
      tp=typeParameters;
    else
      tp.addAll(typeParameters);

    return Type.newType
      (tp,
       new Polytype(Constraint.and(constraint,returnType.getConstraint()),
		    new FunType(MonoSymbol.getMonotype(formals),
				returnType.getMonotype())));
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/
  
  public String toString()
  {
    return 
      constraint
      + "fun ("
      + Util.map("",", ","",formals)
      + ") => "
      + body
      ;
  }
  
  Collection /* of TypeSymbol*/ typeParameters;
  Collection /* of FieldSymbol */ formals;
  Constraint constraint;
  Block body;
}
