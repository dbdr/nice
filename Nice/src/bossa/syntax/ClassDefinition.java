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
//$Modified: Fri Jul 23 17:42:07 1999 by bonniot $
// Description : Abstract syntax for a class definition

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class ClassDefinition extends Node //extends TypeSymbol 
  implements Definition
{
  public ClassDefinition(LocatedString name, Collection typeParameters)
  {
    this.name=name;
    if(typeParameters==null)
      this.typeParameters=new ArrayList(0);
    else
      this.typeParameters=typeParameters;
    extensions=new ArrayList();
    implementations=new ArrayList();
    abstractions=new ArrayList();
    methods=new ArrayList();
    fields=new ArrayList();
    this.tc=new TypeConstructor(this);
  }
  
  Constraint getConstraint()
  {
    return new Constraint(typeParameters,null);
  }

  boolean isAssignable()
  {
    return false;
  }

  Type getType()
  {
    return Type.newType
      (typeParameters,
       new Polytype(new MonotypeConstructor
		    (this.tc,
		     TypeParameters.fromSymbols(typeParameters))));
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
    extensions=TypeConstructor.resolve(typeScope,extensions);
    implementations=TypeConstructor.resolve(typeScope,implementations);
    abstractions=TypeConstructor.resolve(typeScope,abstractions);
    resolveScope(fields);
  }

  VarScope memberScope()
  {
    VarScope res=null;

    try{
      res=VarScope.makeScope(null,fields);
    }
    catch(DuplicateIdentEx e){
      User.error(this.name,"Identifier "+e.ident+" defined twice in this class");
    }

    return res;
  }

  public void typecheck()
  {
  }

  public void addExtension(TypeConstructor name)
  {
    extensions.add(name);
  }

  public void addImplementation(TypeConstructor name)
  {
    implementations.add(name);
  }

  public void addAbstraction(TypeConstructor name)
  {
    abstractions.add(name);
  }

  public void addField(LocatedString name, Monotype type)
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

  LocatedString name;
  TypeConstructor tc;
  Collection /* of TypeSymbol */ typeParameters;
  private Collection /* of TypeConstructor */ extensions;
  private Collection /* of TypeConstructor */ implementations;
  private Collection /* of TypeConstructor */ abstractions;
  private Collection /* of VarSymbol */ fields;
  private Collection methods;
}
