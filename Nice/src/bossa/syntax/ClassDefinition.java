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

// File    : ClassDefinition.java
// Created : Thu Jul 01 11:25:14 1999 by bonniot
//$Modified: Fri Jul 16 19:20:44 1999 by bonniot $
// Description : Abstract syntax for a class definition

package bossa.syntax;

import java.util.*;
import bossa.util.Util;

public class ClassDefinition extends TypeSymbol implements Definition
{
  public ClassDefinition(Ident name, Collection typeParameters)
  {
    super(name);
    this.tc=new TypeConstructor(this);
    this.typeParameters=typeParameters;
    extensions=new ArrayList();
    implementations=new ArrayList();
    abstractions=new ArrayList();
    methods=new ArrayList();
    fields=new ArrayList();
  }

  Constraint getConstraint()
  {
    return new Constraint(typeParameters);
  }

  boolean isAssignable()
  {
    return false;
  }

  void buildScope(VarScope scope, TypeScope ts)
  {
    this.scope=scope;
    this.typeScope=TypeScope.makeScope(ts,typeParameters);
    buildScope(this.scope,this.typeScope,fields);
    buildScope(this.scope,this.typeScope,methods);
  }

  void resolveScope()
  {
    extensions=Monotype.resolve(typeScope,extensions);
    implementations=Monotype.resolve(typeScope,implementations);
    abstractions=Monotype.resolve(typeScope,abstractions);
    resolveScope(fields);
  }

  VarScope memberScope()
  {
    return VarScope.makeScope(null,fields);
  }

  public void addExtension(IdentType name)
  {
    extensions.add(name);
  }

  public void addImplementation(IdentType name)
  {
    implementations.add(name);
  }

  public void addAbstraction(IdentType name)
  {
    abstractions.add(name);
  }

  public void addField(Ident name, Monotype type)
  {
    fields.add(new FieldSymb(name,type,this));
  }

  public void addMethod(MethodDefinition m)
  {
    methods.add(m);
  }

  public String toString()
  {
    return
      "class "
      + name.toString()
      + Util.map("<",", ",">",typeParameters)
      + Util.map(" extends ",", ","",extensions)
      + Util.map(" implements ",", ","",implementations)
      + Util.map(" abstract ",", ","",abstractions)
      + " {\n"
      + Util.map("",";\n",";\n",fields)
      + Util.map(methods)
      + "}\n\n"
      ;
  }

  Collection methodDefinitions()
  {
    return methods;
  }

  TypeConstructor tc;
  private Collection /* of TypeSymbol */ typeParameters;
  private Collection /* of IdentType */ extensions;
  private Collection /* of IdentType */ implementations;
  private Collection /* of IdentType */ abstractions;
  private Collection /* of VarSymbol */ fields;
  private Collection methods;
}
