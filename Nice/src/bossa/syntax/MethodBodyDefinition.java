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
//$Modified: Mon Aug 30 15:44:51 1999 by bonniot $
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
    super(Node.down);
    this.name=name;
    this.typeParameters=typeParameters;
    this.binders=binders; 
    this.formals=formals;
    this.body=new Block(body);
    this.definition=null;
    
    addChild(this.body);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }
  
  private Collection buildSymbols(Collection names, Collection types)
  {
    if(names.size()!=types.size())
      switch(types.size()){
      case 0: User.error(this,"Method "+name+" has no parameters");
      case 1: User.error(this,"Method "+name+" has 1 parameter");
      default:User.error(this,"Method "+name+" has "+types.size()+
			 " parameters");
      }
    
    Collection res=new ArrayList(names.size());
    for(Iterator n=names.iterator(),t=types.iterator();
	n.hasNext();)
      {
	Pattern p=(Pattern)n.next();
	Monotype domt=(Monotype)t.next();
	res.add(new MonoSymbol(p.name,
			       Monotype.fresh(p.name,domt)));
      }
    return res;
  }
  
  private void setDefinition(MethodDefinition d)
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

    parameters=buildSymbols(this.formals,definition.type.domain());
    addSymbols(parameters);
  }

  Scopes buildScope(VarScope outer, TypeScope typeOuter)
  {
    setDefinition((MethodDefinition)outer.lookupLast(name));

    //addTypeSymbols(definition.type.getTypeParameters());
    //addTypeSymbols(definition.type.getConstraint().binders);
    
    // Get imperative type parameters
    if(typeParameters.size()>0)
      try{
	addTypeMaps
	  (TypeConstructor.toLocatedString(typeParameters),
	   definition.type.getTypeParameters());
      }
      catch(BadSizeEx e){
	User.error(name,"Method \""+name+"\" expects "+e.expected+
		   " imperative type parameters");
      }

    // Get functional type parameters
    if(binders!=null)
    try{
      addTypeMaps
	(binders,
	 definition.type.getConstraint().binders);
    }
    catch(BadSizeEx e){
      User.error(name,"Method \""+name+"\" expects "+e.expected+
		 " functional type parameters");
    }

    Scopes res=super.buildScope(outer,typeOuter);

    return res;
  }

  void resolve()
  {
    Pattern.resolve(typeScope,formals);
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  void typecheck()
  {
    Typing.enter(definition.type.getTypeParameters(),
		 "method body of "+name);

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
      
      try{
	Typing.in
	  (VarSymbol.getType(parameters),
	   Pattern.getDomain(formals));
      }
      catch(TypingEx e){
	User.error(name,"The patterns are not correct");
      }
      
      Typing.implies();

    }
    catch(BadSizeEx e){
      Internal.error("Bad size in MethodBodyDefinition.typecheck()");
    }
    catch(TypingEx e) {
      User.error(name,"Typing error in method body \""+name+"\":\n"+e);
    }
  }

  void endTypecheck()
  {
    try{
      Type t=body.getType();
      if(t==null)
	User.error(this,"Last statement of body should be \"return\"");
      Typing.leq(t,new Polytype(definition.type.codomain()));
      Typing.leave();
    }
    catch(TypingEx e){
      User.error(this,"Return type is not correct :"+e);
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
