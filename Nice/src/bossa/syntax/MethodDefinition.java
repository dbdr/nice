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
//$Modified: Mon Aug 30 15:43:21 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Abstract syntax for a global method declaration.
 */
public class MethodDefinition extends PolySymbol implements Definition
{
  /**
   * The method is a class member if c!=null.
   *
   * @param c the class this method belongs to, or 'null'
   * @param name the name of the method
   * @param typeParameters the type parameters
   * @param constraint the constraint
   * @param returnType the return type
   * @param parameters the MonoTypes of the parameters
   */
  public MethodDefinition(ClassDefinition c,
			  LocatedString name, 
			  List typeParameters,
			  Constraint constraint,
			  Monotype returnType,
			  Collection parameters)
  {
    // hack, super must be the first call
    super(name,null);
    
    Collection params=new ArrayList();
    // if it is a class method, there is an implicit "this" argument
    //TODO    if(c!=null)
    //params.add(c.type);
    params.addAll(parameters);
    
    this.arity=params.size();
    this.type=Type.newType(typeParameters,
			   new Polytype(constraint,
					new FunType(params,returnType)));
    addChild(type);

    this.memberOf=c;
  }

  /** the Method is global */
  public MethodDefinition(LocatedString name,
			  List typeParameters,
			  Constraint constraint,
			  Monotype returnType,
			  Collection parameters)
  {
    this(null,name,typeParameters,constraint,returnType,parameters);
  }

  public Collection associatedDefinitions()
  {
    return null;
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

  int getArity()
  {
    return arity;
  }
  
  /************************************************************
   * Printing
   ************************************************************/

  public String toString()
  {
    return
      type.codomain().toString()
      + " "
      + name
      + type.getConstraint().toString()
      + "("
      + Util.map("",", ","",type.domain())
      + ");\n"
      ;
  }

  private ClassDefinition memberOf;
  private int arity;
  boolean isFieldAccess=false; //true if this method represent the access to the field of an object.
}
