/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
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
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */
public class NiceFieldAccess extends FieldAccess
{
  public NiceFieldAccess
    (NiceClass classDef, 
     LocatedString fieldName, Monotype fieldType)
  {
    super(fieldName, new Constraint(classDef.typeParameters,null),
	  makeList(Monotype.create(Monotype.sure(classDef.lowlevelMonotype()))),
	  fieldType);
    this.definition = classDef;
    this.fieldName = fieldName.toString();
  }
  
  private static FormalParameters makeList(Monotype t)
  {
    List res = new LinkedList();
    res.add(new FormalParameters.Parameter(t));
    return new FormalParameters(res);
  }
  
  public final String fieldName;
  
  /** The class this field belongs to. */
  NiceClass definition;

  public void createContext()
  {
    super.createContext();
    createField();
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

  private Type fieldType()
  {
    return javaReturnType();
  }  

  private void createField()
  {
    fieldDecl = definition.classe.addField(fieldName, fieldType());
  }
}
