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
import nice.tools.code.*;

import mlsub.typing.TypeConstructor;

import gnu.bytecode.*;
import gnu.expr.*;

import java.util.*;

/**
   A field access in a Nice class.
   
   In terms of scoping, this is the symbol
   that is returned when the access to a field is done,
   either a 'get' or a 'set'.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
public class NiceFieldAccess extends FieldAccess
{
  public NiceFieldAccess
    (ClassDefinition classDef, 
     LocatedString fieldName, Monotype fieldType,
     List classTypeParameters)
  {
    super(fieldName, new Constraint(classTypeParameters,null),
	  fieldType, makeList(Monotype.create(classDef.lowlevelMonotype())));
    this.definition = classDef;
    this.classTC = classDef.tc;
    this.fieldName = fieldName.toString();
  }
  
  private static FormalParameters makeList(Monotype t)
  {
    List res = new LinkedList();
    res.add(new FormalParameters.Parameter(t));
    return new FormalParameters(res);
  }
  
  public final TypeConstructor classTC;

  public final String fieldName;
  
  /** The java class this method is defined in */
  ClassDefinition definition;

  public void createContext()
  {
    super.createContext();
    findField();
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    Internal.error("Should not be part of the module interface");
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  private void findField()
  {
    ClassType owner = (ClassType) nice.tools.code.Types.javaType(classTC);
    field = owner.getDeclaredField(fieldName);
    if (field == null)
      field = owner.addField(fieldName, fieldType(), Access.PUBLIC);
  }

  private Type fieldType()
  {
    return javaReturnType();
  }  
}
