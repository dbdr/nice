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
//$Modified: Thu Jul 29 12:21:19 1999 by bonniot $

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
    Type funt=fun.getType();
    Collection parametersTypes=null;

    Collection dom=funt.domain();
    Monotype codom=funt.codomain();
    
    User.error(dom==null || codom==null,
	       this,fun+" is not a function");
    
    User.error(!(funt instanceof Polytype),this,
	       "You have to specify the type parameters for function "+
	       fun);

    Typing.enter(funt.getTypeParameters(),"call "+this.fun);


    try{
      Typing.implies();
      
      try{ funt.getConstraint().assert(); }
      catch(TypingEx e) { User.error(this,"The conditions for using this function are not fullfiled"); }

      parametersTypes=Expression.getPolytype(parameters);
      User.error(parametersTypes==null,this,
		 "Arguments of functions must not be imperative-parametric");
      
      Typing.in(parametersTypes,
		Domain.fromMonotypes(funt.domain()));

      Typing.leave();

    }
    catch(BadSizeEx e){
      User.error(this,e.expected+" parameters expected "+
		 ", not "+e.actual);
    }
    catch(TypingEx e){
      if(parameters.size()>=2)
	User.error(this,"The parameters "+
		   Util.map("(",", ",")",parameters) +
		 " are not within the domain of the function");
      else
	User.error(this,"The parameter "+
		   Util.map("",", ","",parameters) +
		 " is not within the domain of the function");
    }
    
    Constraint cst=funt.getConstraint().and(Type.getConstraint(parametersTypes));
    cst.and(MonotypeLeqCst.constraint(Type.getMonotype(parametersTypes),dom));
    
    Polytype res = new Polytype(cst,codom);
    return res;
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
