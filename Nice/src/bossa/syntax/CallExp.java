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

// File    : CallExp.java
// Created : Mon Jul 05 16:27:27 1999 by bonniot
//$Modified: Tue Jul 27 12:40:58 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.typing.*;

/**
 * A function call
 */
public class CallExp extends Expression
{
  /**
   *
   * @param fun the function to call
   * @param parameters a collection of Expressions
   */
  public CallExp(Expression fun, 
		 Collection parameters)
  {
    this.fun=fun;
    this.parameters=parameters;
  }

  Expression resolve(VarScope scope, TypeScope ts)
  {
    fun=fun.resolve(scope,ts);
    parameters=resolve(scope,ts,parameters);
    return this;
  }

  Type getType()
  {
    Type t=fun.getType();
    Collection dom=t.domain();
    Monotype codom=t.codomain();
    
    User.error(dom==null || codom==null,
	       this,fun+" is not a function");
    
    User.error(!(t instanceof Polytype),this,
	       "You have to specify the type parameters for function "+
	       fun);

    Typing.enter(t.getTypeParameters(),this.fun+"");

    Typing.implies();

    try{
      t.getConstraint().assert();
      Typing.in(Expression.getType(parameters),
		Domain.fromMonotypes(t.domain()));
    }
    catch(BadSizeEx e){
      User.error(this,e.expected+" parameters expected "+
		 ", not "+e.actual);
    }
    catch(TypingEx e){
      User.error(this,e.getMessage());
    }
    
    Typing.leave();

    Monotype res=t.codomain();
    User.error(res==null,this,fun+" is not a function");
    return new Polytype(res);
  }

  public String toString()
  {
    return fun
      + "(" + Util.map("",", ","",parameters) + ")"
      ;
  }

  protected Expression fun;
  protected Collection /* of Expression */ parameters;
}
