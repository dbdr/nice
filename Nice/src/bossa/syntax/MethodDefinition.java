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

// File    : MethodDefinition.java
// Created : Thu Jul 01 18:12:46 1999 by bonniot
//$Modified: Fri Jul 09 20:27:34 1999 by bonniot $
// Description : Abstract syntax for a global method declaration

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class MethodDefinition extends VarSymbol implements Definition
{
  /** the Method is a class member */
  public MethodDefinition(ClassDefinition c,
			  Ident name, 
			  Constraint constraint,
			  Type returnType,
			  Collection parameters)
  {
    // hack, super must be the first call
    super(name,null);

    Collection params=new ArrayList();
    // if it is a class method, there is an implicit "this" argument
    if(c!=null)
      params.add(c.type);
    params.addAll(parameters);

    this.type=new FunType(params,returnType);
    this.memberOf=c;
    this.constraint=constraint;
  }

  /** the Method is global */
  public MethodDefinition(Ident name, 
			  Constraint constraint,
			  Type returnType,
			  Collection parameters)
  {
    this(null,name,constraint,returnType,parameters);
  }

  /** true iff the method was declared inside a class */
  boolean isMember()
  {
    return memberOf!=null;
  }

  // from VarSymbol 
  boolean isAssignable()
  {
    return false;
  }

  void buildScope(VarScope outer, TypeScope ts)
  {
    scope=outer;
    typeScope=TypeScope.makeScope(ts,constraint.binders);
  }

  void resolveScope()
  {
    type=type.resolve(typeScope);
  }

  public String toString()
  {
    return
      type.codomain().toStringExtern()
      + " "
      + name.toString()
      + constraint.toString()
      + "("
      + Util.map("",", ","",type.domain())
      + ");\n"
      ;
  }

  private ClassDefinition memberOf;
  private Constraint constraint;
}
