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

// File    : MethodBodyDefinition.java
// Created : Thu Jul 01 18:12:46 1999 by bonniot
//$Modified: Fri Aug 13 14:08:47 1999 by bonniot $
// Description : Abstract syntax for a method body

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.typing.*;

public class MethodBodyDefinition extends Node 
  implements Definition, Located
{
  public MethodBodyDefinition(LocatedString name, 
			      Collection typeParameters,
			      Collection binders,
			      List formals, List body)
  {
    this.name=name;
    this.typeParameters=typeParameters;
    this.binders=binders; 
    this.formals=formals;
    this.body=new Block(body);
    this.definition=null;
  }

  void setDefinitionAndBuildScope(MethodDefinition d,VarScope scope,
				  TypeScope typeScope)
  {
    User.error(d==null,this,"Method \""+name+"\" has not been declared");
    this.definition=d;

    // if the method is not a class member,
    // the "this" formal is useless
    if(!d.isMember())
      {
	User.error(!((Pattern)formals.get(0)).thisAtNothing(),
		   this,
		   "Method \""+name+"\" is a global method"+
		   ", it cannot have a main pattern");
	formals.remove(0);
      }

    buildScope(scope,typeScope);
  }

  private Collection buildSymbols(Collection names, Collection types)
  {
    Collection res=new ArrayList(names.size());
    Iterator n=names.iterator();
    Iterator t=types.iterator();
    
    while(n.hasNext())
      {
	User.error(!t.hasNext(),
		   "Method body "+this.name+" has two many parameters:\n"+
		   "It needs "+types.size()+
		   ", not "+names.size()
		   );

	Pattern p=(Pattern)n.next();
	Monotype domt=(Monotype)t.next();
	
	res.add(new MonoSymbol(p.name,
			      Monotype.fresh(p.name,domt),null));
      }
    User.error(t.hasNext(),
	       "Method body "+this.name+" has not enough parameters");
    return res;
  }
  
  void buildScope(VarScope outer, TypeScope typeOuter)
  {
    parameters=buildSymbols(this.formals,definition.type.domain());
    try
      {
	this.scope=VarScope.makeScope(outer,this.parameters);
      }
    catch(DuplicateIdentEx e)
      {
	User.error(this.name,"Identifier "+e.ident+
		   " was defined twice in the body of this method");
      }

    // Get imperative type parameters
    try{
      this.typeScope=TypeScope.makeScope
	(typeOuter,
	 TypeConstructor.toLocatedString(this.typeParameters),
	 definition.type.getTypeParameters());
    }
    catch(BadSizeEx e){
      User.error(name,"Method \""+name+"\" expects "+e.expected+
		 " imperative type parameters");
    }

    // Get functional type parameters
    if(binders!=null)
    try{
      this.typeScope=TypeScope.makeScope
	(this.typeScope,
	 binders,
	 definition.type.getConstraint().binders);
    }
    catch(BadSizeEx e){
      User.error(name,"Method \""+name+"\" expects "+e.expected+
		 " functional type parameters");
    }

    Node.buildScope(definition.scope,this.typeScope,parameters);
    body.buildScope(this.scope,this.typeScope);
  }

  void resolveScope()
  {
    Pattern.resolve(typeScope,formals);
    resolveScope(parameters);
    body.resolveScope();
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  void typecheck()
  {
    Typing.enter(definition.type.getTypeParameters(),"method body of "+name);

    try{
      try { definition.type.getConstraint().assert(); }
      catch(TypingEx e){
	User.error(name,"the constraint will never be satisfied");
      }
      
      Collection monotypes=MonoSymbol.getMonotype(parameters);
      Typing.introduce(monotypes);
      
      Typing.leqMono
	(monotypes,
	 definition.type.domain());
      
      Typing.in
	(VarSymbol.getType(parameters),
	 Pattern.getDomain(formals));

      Typing.implies();

    }
    catch(BadSizeEx e){
      Internal.error("Bad size in MethodBodyDefinition.typecheck()");
    }
    catch(TypingEx e) {
      User.error(name,"Typing error in method body "+name+e);
    }

    body.typecheck();
    try{
      Type t=body.getType();
      if(t==null)
	User.error(this,"Last statement of body should be \"return\"");
      Typing.leq(t,new Polytype(definition.type.codomain()));
      Typing.leave();
    }
    catch(TypingEx e){
      User.error(this,"Return type is not correct");
    }
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public Location location()
  {
    return name.location();
  }

  public String toString()
  {
    String res;
    res=name.toString()
      +"("
      + Util.map("",", ","",parameters)
      + ") "
      + body
      + "\n";
    return res;
  }

  private MethodDefinition definition;
  protected LocatedString name;
  protected Collection /* of FieldSymbol */  parameters;
  protected List       /* of Patterns */   formals;
  protected Collection /* of TypeConstructor */ typeParameters;
  Collection /* of LocatedString */ binders; // Null if functional type parameters are not bound
  private Block body;
}
