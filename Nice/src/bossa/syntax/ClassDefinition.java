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
//$Modified: Mon Nov 15 20:04:18 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;

import gnu.bytecode.*;
import java.util.*;

/**
 * Abstract syntax for a class definition.
 */
public class ClassDefinition extends Node
  implements Definition
{
  public ClassDefinition(LocatedString name, 
			 boolean isFinal, boolean isAbstract,
			 boolean isSharp,
			 List typeParameters,
			 List extensions, List implementations, List abstractions,
			 List fields, List methods)
  {
    super(Node.global);
    
    this.name=name;
    this.isFinal=isFinal;
    this.isAbstract=isAbstract;
    this.isSharp=isSharp;

    if(typeParameters==null)
      this.typeParameters=new ArrayList(0);
    else
      this.typeParameters=typeParameters;
    this.extensions=extensions;

    this.fields=keepFields(fields,isSharp);

    this.tc=new TypeConstructor(this);
    addTypeSymbol(this.tc);
            
    if(isSharp)
      // The sharp class must not declare children, 
      // since the associated class has already done so.
      // Anyway, those should be null.
      {
	this.implementations=implementations;
	this.abstractions=abstractions;
	this.methods=methods;
	addChildren(computeAccessMethods(this.fields));
      }
    else
      {
	this.implementations=addChildren(implementations);
	this.abstractions=addChildren(abstractions);
	this.methods=addChildren(methods);
	addChildren(computeAccessMethods(this.fields));
      }
    
    // if this class is final, 
    // no #class is created below.
    // the associated #class is itself
    if(isFinal && !isSharp)
      addTypeMap("#"+name.content,this.tc);
  }

  /**
   * Creates an immediate descendant, 
   * that abstracts and doesn't implement Top<n>.
   */
  public Collection associatedDefinitions()
  {
    if(isFinal || isAbstract)
      return new ArrayList(0);
    Collection res=new ArrayList(1);
    LocatedString name=new LocatedString("#"+this.name.content,this.name.location());
    List parent=new ArrayList(1);
    parent.add(this.tc);
    ClassDefinition c=new ClassDefinition
      (name,true,false,true,typeParameters,parent,
       new LinkedList(),new LinkedList(),fields,methods);
    res.add(c);
    return res;
  }
  
  /****************************************************************
   * Fields
   ****************************************************************/

  private List keepFields(List fields, boolean local)
  {
    List result=new ArrayList(fields.size());
    for(Iterator i=fields.iterator();i.hasNext();)
      {
	Field f=(Field)i.next();
	if(f.isLocal==local)
	  result.add(f);
      }
    return result;
  }
  
  public static class Field
  {
    public Field(MonoSymbol sym, boolean isFinal, boolean isLocal)
    {
      this.sym=sym;
      this.isFinal=isFinal;
      this.isLocal=isLocal;
    }

    MonoSymbol sym;
    boolean isFinal;
    boolean isLocal;
  }
  
  private List computeAccessMethods(List fields)
  {
    List res=new ArrayList(fields.size());
    for(Iterator i=fields.iterator();i.hasNext();)
      {
	Field f=(Field)i.next();

	MonoSymbol s=f.sym;
	List params=new LinkedList();
	params.add(getMonotype());
	MethodDefinition m=new MethodDefinition(s.name,new Constraint(typeParameters,null),s.type,params);
	m.isFieldAccess=true;
	res.add(m);
      }
    return res;
  }
  
  /****************************************************************
   * Selectors
   ****************************************************************/

  Constraint getConstraint()
  {
    return new Constraint(typeParameters,null);
  }

  Polytype getType()
  {
    return new Polytype(new Constraint(typeParameters,null),getMonotype());
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
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
    try{
      Typing.initialLeq(tc,extensions);
      Typing.assertImp(tc,implementations,true);
      Typing.assertImp(tc,abstractions,true);
      Typing.assertAbs(tc,abstractions);
      if(isFinal)
	Typing.assertAbs(tc,InterfaceDefinition.top(typeParameters.size()));
      if(!isSharp)
	Typing.assertImp(tc,InterfaceDefinition.top(typeParameters.size()),true);
    }
    catch(TypingEx e){
      User.error(name,"Error in class "+name+" : "+e.getMessage());
    }
  }

  public static void createSpecialContext()
  {
    try{
      for(Iterator i=specialTypeConstructors.iterator();i.hasNext();)
	{
	  TypeConstructor tc = (TypeConstructor)i.next();
	  bossa.typing.Typing.introduce(tc);
	  Typing.assertImp(tc,InterfaceDefinition.top(0),true);
	}
    }
    catch(TypingEx e){
      Internal.error("Initial context error in java classes");
    }
  }
  
  /**
   * Registers a type constructor that was not defined by a class.
   *
   * This is used to add special type constructors
   * -- like <code>JavaTypeConstructor</code> --
   * in the initial context.
   */
  static void addSpeciaTypeConstructor(TypeConstructor tc)
  {
    specialTypeConstructors.add(tc);
  }
  
  private static ArrayList specialTypeConstructors = new ArrayList(10);
  
  /****************************************************************
   * Module Interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    if(!isSharp)
    s.print
      (
         (isFinal ? "final " : "")
       + (isAbstract ? "abstract " : "")
       + "class "
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
   * Code generation
   ****************************************************************/

  public void compile(bossa.modules.Module module)
  {
    if(!isSharp)
      return;
    
    ClassType c=ClassType.make(module.className(name.toString().substring(1)),gnu.bytecode.Type.pointer_type);
    addFields(c);    
    module.addClass(c);
  }

  private void addFields(ClassType c)
  {
    for(Iterator i=fields.iterator();
	i.hasNext();)
      {
	Field f=(Field)i.next();
	gnu.bytecode.Field field=
	  c.addField(f.sym.name.toString(),
		     Type.pointer_type,
		     Access.PUBLIC);
	c.setSuper(Type.pointer_type);
      }
    for(Iterator i=extensions.iterator();
	i.hasNext();)
      ((TypeConstructor)i.next()).definition.addFields(c);
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
  private List /* of ClassDefinition.Field */ fields;
  private List methods;
  private boolean isFinal;
  boolean isAbstract;
  boolean isSharp; // This class is a #A (not directly visible to the user)
}
