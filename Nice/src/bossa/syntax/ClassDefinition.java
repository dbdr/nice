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
//$Modified: Wed Oct 13 18:00:39 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.typing.*;

/**
 * Abstract syntax for a class definition.
 */
public class ClassDefinition extends Node
  implements Definition
{
  public ClassDefinition(LocatedString name, 
			 boolean isFinal, boolean isSharp,
			 List typeParameters,
			 List extensions, List implementations, List abstractions,
			 List fields,
			 List methods)
  {
    super(Node.global);
    
    this.name=name;
    this.isFinal=isFinal;
    this.isSharp=isSharp;

    if(typeParameters==null)
      this.typeParameters=new ArrayList(0);
    else
      this.typeParameters=typeParameters;
    this.extensions=extensions;

    this.tc=new TypeConstructor(this);
    addTypeSymbol(this.tc);

    this.fields=fields;
    if(isSharp)
      // The sharp class must not declare children, 
      // since the associated class has already done so.
      // Anyway, those must be null.
      {
	this.implementations=implementations;
	this.abstractions=abstractions;
	this.methods=methods;
      }
    else
      {
	this.implementations=addChildren(implementations);
	this.abstractions=addChildren(abstractions);
	this.methods=addChildren(methods);
	addChildren(computeAccessMethods(fields));
      }
    
    if(isFinal && !isSharp)
      addTypeMap("#"+name.content,this.tc);
  }

  public Collection associatedDefinitions()
  {
    // Creates an immediate descendant, that abstracts and doesn't implement Top<n>
    if(isFinal)
      return new ArrayList(0);
    Collection res=new ArrayList(1);
    LocatedString name=new LocatedString("#"+this.name.content,this.name.location());
    List parent=new ArrayList(1);
    parent.add(this.tc);
    ClassDefinition c=new ClassDefinition(name,true,true,typeParameters,parent,new LinkedList(),new LinkedList(),fields,methods);
    res.add(c);
    return res;
  }
  
  private List computeAccessMethods(List fields)
  {
    List res=new ArrayList(fields.size());
    for(Iterator i=fields.iterator();i.hasNext();)
      {
	MonoSymbol s=(MonoSymbol)i.next();
	List params=new LinkedList();
	params.add(getMonotype());
	MethodDefinition m=new MethodDefinition(s.name,typeParameters,Constraint.True(),s.type,params);
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

  /****************************************************************
   * Typechecking
   ****************************************************************/

  void typecheck()
  {
    try{
      Typing.introduce(tc);
      Typing.leq(tc,extensions);
      Typing.assertImp(tc,implementations);
      Typing.assertImp(tc,abstractions);
      Typing.assertAbs(tc,abstractions);
      if(isFinal)
	Typing.assertAbs(tc,InterfaceDefinition.top(typeParameters.size()));
      if(!isSharp)
	Typing.assertImp(tc,InterfaceDefinition.top(typeParameters.size()));
    }
    catch(TypingEx e){
      User.error(name,"Error in class "+name+" : "+e.getMessage());
    }
  }

  /****************************************************************
   * Module Interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    if(!isSharp)
    s.print
      ("class "
       + name.toString()
       + Util.map("<",", ",">",typeParameters)
       + Util.map(" extends ",", ","",extensions)
       + Util.map(" implements ",", ","",implementations)
       + Util.map(" abstracts ",", ","",abstractions)
       + " {\n"
       + Util.map("",";\n",";\n",fields)
       + Util.map(methods)
       + "}\n\n"
       );
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

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
  private List /* of Interface */ implementations;
  private List /* of Interface */ abstractions;
  private List /* of MonoSymbol */ fields;
  private List methods;
  private boolean isFinal;
  private boolean isSharp; // This class is a #A (not directly visible to the user)
}
