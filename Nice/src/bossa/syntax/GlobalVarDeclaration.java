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

// File    : GlobalVarDeclaration.java
// Created : Tue Jul 06 12:06:20 1999 by bonniot
//$Modified: Mon Aug 07 15:31:51 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;
import mlsub.typing.*;

import gnu.bytecode.*;
import java.util.*;

/**
 * Declaration of a local variable
 * with an optional initial value
 */
public class GlobalVarDeclaration extends Definition
{
  public GlobalVarDeclaration(LocatedString name, Monotype type, Expression value)
  {
    super(name, Node.global);
    
    this.left=new MonoSymbol(name,type);
    //addSymbol(left);
    addChild(left);
    
    if(value!=null)
      this.value=expChild(value);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }
  
  /****************************************************************
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
    // Not really related to the rigid context, 
    // but it a good time to do this (must be done after resolving).

    gnu.expr.Declaration declaration = 
      new gnu.expr.Declaration(left.name.toString(),
			       nice.tools.code.Types.javaType(left.type));

    if(!module.generatingBytecode())
      // The code is already there
      {
	declaration.field = module.getReadBytecode().getField(left.name.toString());
	
	if(declaration.field==null)
	  Internal.error(this,
			 "The compiled file is not consistant with the interface file");
      }
    else
      {
	declaration.field = module.getOutputBytecode()
	  .addField(left.name.toString(),
		    nice.tools.code.Types.javaType(left.type),
		    Access.PUBLIC|Access.STATIC);
	declaration.setSimple(false);
	declaration.noteValue(null);
      }
    
    module.getPackageScopeExp().addDeclaration(declaration);
    
    left.setDeclaration(declaration);
  }
  
  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  void typecheck()
  {
    if(value==null) return;
    try{
      value.resolveOverloading(left.getType());
      Typing.leq(value.getType(),left.getType());
    }
    catch(TypingEx e){
      User.error(this,"Typing error : "+left+" cannot be assigned value "+value+
		 " of type "+value.getType()+" : \n"+
		 e);
    }
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print("var "+left+
	    //	    (value==null ? "" : " = "+value.toString())+
	    ";\n");
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public void compile()
  {
    if(value!=null)
      module.addClassInitStatement
	(new gnu.expr.SetExp(left.getDeclaration(),value.compile()));    
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return left.type+" "+left+
      (value==null?"":" = "+value);
  }

  protected ExpressionRef value=null;
  // "name" after scoping
  MonoSymbol left;
}
