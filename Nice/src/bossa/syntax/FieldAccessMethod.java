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

// File    : FieldAccessMethod.java
// Created : Thu Jul 01 18:12:46 1999 by bonniot
//$Modified: Sat Dec 04 12:29:25 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

/**
 * A bossa field access.
 *
 * In terms of scoping, this is the symbol
 * that is returned when the access to a field is done,
 * either a 'get' or a 'set'.
 * In terms of code generation, it represents only the 'get' operation.
 * It holds a reference to a <code>SetFieldMethod</code>
 * to compile an access occuring on the left of an assignment.
 */
public class FieldAccessMethod extends MethodDefinition
{
  public FieldAccessMethod
    (ClassDefinition classDef, 
     LocatedString fieldName, Monotype fieldType,
     List classTypeParameters)
  {
    super(fieldName,new Constraint(classTypeParameters,null),
	  fieldType,makeList(classDef.getType().getMonotype()));
    this.definition=classDef;
    this.fieldTC = classDef.tc;
    this.fieldName = fieldName.toString();
    
    MethodDefinition.addMethod(this);

    setMethod = new SetFieldMethod(classDef,fieldName,fieldType,classTypeParameters);
  }
  
  private static List makeList(Monotype t)
  {
    List res=new LinkedList();
    res.add(t);
    return res;
  }
  
  /** 
   * true if this method represent the access to the field of an object.
   */
  public boolean isFieldAccess() { return true; }

  public final TypeConstructor fieldTC;

  public final String fieldName;
  
  /** The java class this method is defined in */
  ClassDefinition definition;

  private final SetFieldMethod setMethod;
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  // Name for the "get" method
  public String bytecodeName()
  {
    return "$get_" + fieldName;
  }
  
  gnu.expr.Expression compileAssign(List parameter, Expression value)
  {
    return setMethod.compileAssign(parameter,value);
  }

  /****************************************************************
   * Module
   ****************************************************************/
  
  public void setModule(bossa.modules.Module module)
  {
    super.setModule(module);
    setMethod.setModule(module);
  }
  
}
