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
//$Modified: Tue Aug 24 15:06:28 1999 by bonniot $
// Description : Abstract syntax for a class definition

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.typing.*;

public class ClassDefinition extends Node
  implements Definition
{
  public ClassDefinition(LocatedString name, 
			 List typeParameters,
			 List extensions, List implementations, List abstractions,
			 List fields,
			 List methods)
  {
    super(Node.global);
    
    this.name=name;
    if(typeParameters==null)
      this.typeParameters=new ArrayList(0);
    else
      this.typeParameters=typeParameters;
    this.extensions=extensions;
    this.implementations=implementations;
    this.abstractions=abstractions;
    this.fields=fields;
    this.methods=methods;
    this.tc=new TypeConstructor(this);

    addTypeSymbol(this.tc);
    addChildren(computeAccessMethods(fields));
    addChildren(methods);
    addChildren(implementations);
    addChildren(abstractions);
  }
  
  private List computeAccessMethods(List fields)
  {
    List res=new ArrayList(fields.size());
    for(Iterator i=fields.iterator();i.hasNext();)
      {
	MonoSymbol s=(MonoSymbol)i.next();
	List params=new LinkedList();
	params.add(getMonotype());
	MethodDefinition m=new MethodDefinition(this,s.name,typeParameters,Constraint.True(),s.type,params);
	m.isFieldAccess=true;
	res.add(m);
      }
    return res;
  }
  
  Constraint getConstraint()
  {
    return new Constraint(typeParameters,null);
  }

  Type getType()
  {
    return Type.newType
      (typeParameters,
       new Polytype(getMonotype()));
  }

  /**
   * Returns the 'Monotype' part of the type.
   * Private use only (to compute the type of the field access methods).
   */
  private Monotype getMonotype()
  {
    return new MonotypeConstructor
      (this.tc,
       TypeParameters.fromSymbols(typeParameters),
       name.location());
  }
  
  void resolve()
  {
    extensions=TypeConstructor.resolve(typeScope,extensions);
  }

  void typecheck()
  {
    try{
      Typing.introduce(tc);
      Typing.leq(tc,extensions);
      Typing.imp(tc,implementations);
      Typing.abs(tc,abstractions);
    }
    catch(TypingEx e){
      User.error(name,"Error in class "+name+" :"+e.getMessage());
    }
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
  List /* of TypeSymbol */ typeParameters;
  private List /* of TypeConstructor */ extensions;
  private List /* of TypeConstructor */ implementations;
  private List /* of TypeConstructor */ abstractions;
  private List /* of MonoSymbol */ fields;
  private List methods;
}
