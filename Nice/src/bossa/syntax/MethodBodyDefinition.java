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
//$Modified: Fri Sep 10 19:07:25 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;
import bossa.typing.*;

/**
 * Definition of an alternative for a method.
 */
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

  /**
   * Returns the symbol of the method this declaration belongs to
   */
  private VarSymbol findSymbol(VarScope scope)
  {
    VarSymbol res;
    if(!(scope.overloaded(name)))
      return scope.lookupOne(name);

    Collection symbols=scope.lookup(name);
    if(symbols.size()==0) return null;
    
    // TODO
    for(Iterator i=symbols.iterator();i.hasNext();){
      VarSymbol s=(VarSymbol)i.next();
      if(!(s instanceof MethodDefinition))
	i.remove();
      else
	{
	  MethodDefinition m=(MethodDefinition)s;
	  try{
	    Typing.enter();
	    Typing.implies();
	    Typing.in(Pattern.getPolytype(formals),
		      Domain.fromMonotypes(m.getType().domain()));
	    Typing.leave();
	  }
	  catch(TypingEx e){
	    i.remove();
	  }
	  catch(BadSizeEx e){
	    i.remove();
	  }
	}
    }
    if(symbols.size()==1) return (VarSymbol)symbols.iterator().next();
    User.error(symbols.size()==0,this,
	       "No definition of \""+name+"\" is compatible with the patterns");
    User.error(this,"There is an ambiguity about which version of the overloaded method \""+
	       name+"\" this alternative belongs to.\n"+
	       "Try to use more patterns.");
    return null;
  }
  
  Scopes buildScope(VarScope outer, TypeScope typeOuter)
  // The scoping is delayed to enable overloading
  {
    this.scope=outer;
    this.typeScope=typeOuter;
    return new Scopes(outer,typeOuter);
  }

  void doResolve()
  // Resolution is delayed to enable overloading
  {
  }
  
  void lateBuildScope(VarScope outer, TypeScope typeOuter)
  {
    Pattern.resolve(typeScope,formals);
    VarSymbol s=findSymbol(outer);

    User.error(s==null,this,name+" is not defined");
    User.error(!(s instanceof MethodDefinition),this,name+" is not a method");
    setDefinition((MethodDefinition)s);

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

    //return res;
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  void typecheck()
  {
    lateBuildScope(this.scope,this.typeScope);
    super.doResolve();
    
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
      User.error(this,"Return type is not correct"," :"+e);
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
