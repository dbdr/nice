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
//$Modified: Fri Jul 16 19:27:10 1999 by bonniot $
// Description : Abstract syntax for a method body

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class MethodBodyDefinition extends Node implements Definition
{
  public MethodBodyDefinition(Ident name, Collection typeParameters,
			      List formals, Collection body)
  {
    this.name=name;
    this.typeParameters=typeParameters;
    this.formals=formals;
    this.body=new Block(body);
    this.definition=null;
  }

  private Collection buildSymbols(Collection names, Collection types)
  {
    Collection res=new ArrayList();
    Iterator n=names.iterator();
    Iterator t=types.iterator();
    
    while(n.hasNext())
      {
	User.error(!t.hasNext(),
		   "Method body "+this.name+" has two many parameters:\n"+
		   "It needs "+types.size()+
		   ", not "+names.size()
		   );
	res.add(new LocalSymb(((Pattern)n.next()).name,
			      new Polytype(definition.type.constraint,(Monotype)t.next())));
      }
    User.error(t.hasNext(),
	       "Method body "+this.name+" has not enough parameters");
    return res;
  }
  
  void setDefinition(MethodDefinition d)
  {
    User.error(d==null,"Method \""+name+"\" has not been declared");
    this.definition=d;

    // if the method is not a class member,
    // the "this" formal is useless
    if(!d.isMember())
      {
	User.error(!((Pattern)formals.get(0)).thisAtNothing(),
		   "Method \""+name+"\" is a global method"+
		   ", it cannot have a main pattern");
	formals.remove(0);
      }

    buildScope(d.getScope(),d.getTypeScope());
  }

  void buildScope(VarScope outer, TypeScope typeOuter)
  {
    parameters=buildSymbols(this.formals,definition.type.domain());
    this.scope=VarScope.makeScope(outer,this.parameters);
    this.typeScope=TypeScope.makeScope(typeOuter,this.typeParameters);
    buildScope(this.scope,this.typeScope,parameters);
    body.buildScope(this.scope,this.typeScope);
  }

  void resolveScope()
  {
    resolveScope(parameters);
    body.resolveScope();
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
  protected Ident name;
  protected Collection /* of VarSymbol */  parameters;
  protected List       /* of Patterns */   formals;
  protected Collection /* of TypeSymbol */ typeParameters;
  private Block body;
}
