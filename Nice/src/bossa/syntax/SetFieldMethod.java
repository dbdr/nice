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

// File    : SetFieldMethod.java
// Created : Thu Jul 01 18:12:46 1999 by bonniot
//$Modified: Fri Jan 21 15:37:02 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

/**
 * A bossa field access.
 */
public class SetFieldMethod extends MethodDefinition
{
  public SetFieldMethod
    (ClassDefinition classDef, 
     LocatedString fieldName, Monotype fieldType,
     List classTypeParameters)
  {
    super(fieldName,new Constraint(classTypeParameters,null),
	  JavaTypeConstructor.voidType, makeList(classDef.getType().getMonotype(),fieldType));
    this.definition=classDef;
    this.classTC = classDef.tc;
    this.fieldName = fieldName.toString();
    this.fieldType = fieldType;
    
    MethodDefinition.addMethod(this);
  }
  
  private static List makeList(Monotype t1, Monotype t2)
  {
    LinkedList res=new LinkedList();
    res.addFirst(t2);
    res.addFirst(t1);
    return res;
  }
  
  public final TypeConstructor classTC;
  public final Monotype fieldType;
  
  public final String fieldName;
  
  /** The java class this method is defined in */
  final ClassDefinition definition;

  /****************************************************************
   * Code generation
   ****************************************************************/

  public String bytecodeName()
  {
    return "$set_" + fieldName;
  }
  
  gnu.expr.Expression compileAssign(List parameter, gnu.expr.Expression value)
  {
    if(parameter.size()!=1)
      Internal.error(this,"Field access methods must have just one parameter");
    
    Expression param = (Expression) parameter.get(0);
    
    gnu.expr.Expression[] callParams = new gnu.expr.Expression[2];
    callParams[0]=param.compile();
    callParams[1]=value;
    
    return new gnu.expr.ApplyExp(new gnu.expr.QuoteExp(getDispatchMethod()),
				 callParams);
  }
  
}
