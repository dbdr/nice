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
//$Modified: Fri Jul 09 20:40:33 1999 by bonniot $
// Description : Abstract syntax for a class definition

package bossa.syntax;

import java.util.*;
import bossa.util.Util;

public class ClassDefinition extends TypeSymbol implements Definition
{
  public ClassDefinition(Ident name, Collection typeParameters)
  {
    super(name);
    this.type=new ClassType(this);
    this.typeParameters=typeParameters;
    implementations=new ArrayList();
    abstractions=new ArrayList();
    methods=new ArrayList();
    fields=new ArrayList();
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
    resolveScope(fields);
  }

  public void addImplementation(Ident name)
  {
    implementations.add(name);
  }

  public void addAbstraction(Ident name)
  {
    abstractions.add(name);
  }

  public void addField(Ident name, Type type)
  {
    fields.add(new VarSymbol(name,type));
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

  Type type;
  private Collection /* of TypeSymbol */ typeParameters;
  private Collection implementations;
  private Collection abstractions;
  private Collection /* of VarSymbol */ fields;
  private Collection methods;
}
