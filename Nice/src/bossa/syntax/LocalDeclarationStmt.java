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

// File    : LocalDeclarationStmt.java
// Created : Tue Jul 06 12:06:20 1999 by bonniot
//$Modified: Sat Dec 04 12:08:47 1999 by bonniot $

package bossa.syntax;

import bossa.util.*;
import bossa.typing.*;

import gnu.bytecode.*;
import java.util.*;

/**
 * Declaration of a local variable
 * with an optional initial value
 */
public class LocalDeclarationStmt extends Statement
  implements Definition
{
  public LocalDeclarationStmt(LocatedString name, Monotype type, Expression value, boolean global)
  {
    if(global)
      this.propagate=Node.global;
    
    this.left=new MonoSymbol(name,type);
    //addSymbol(left);
    addChild(left);
    
    if(value!=null)
      this.value=expChild(value);

    setLocation(left.location());
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
    // No really the rigid context, but it is the right time to do this.
    // (must be done after resolving).
    
    declaration = new gnu.expr.Declaration(left.name.toString(),left.type.getJavaType());

    if(module.bytecode!=null)
      // We are in an interface file, module.bytecode is the compiled class
      {
	declaration.field = module.bytecode.getField(left.name.toString());
	
	if(declaration.field==null)
	  Internal.error(this,
			 "The compiled file is not consistant with the interface file");
      }
    
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

  private gnu.expr.Declaration declaration;
  
  public void compile()
  {
    if(declaration.field!=null)
      Internal.error(this,
		     "Field should only be set with imported modules, which should not be compiled");
    
    //declaration.assignField(module.compilation);
    declaration.field = module.bytecode.addField(left.name.toString(),
						 left.type.getJavaType());
    declaration.field.setStaticFlag(true);
    declaration.setSimple(false);
    
    if(value!=null)
      module.addClassInitStatement
	(new gnu.expr.SetExp(declaration,value.compile()));    
  }

  public gnu.expr.Expression generateCode()
  {
    Internal.error(this,"generateCode in "+this.getClass());
    return null;  
  }  

  /****************************************************************
   * Module
   ****************************************************************/
  
  private bossa.modules.Module module;
  
  public void setModule(bossa.modules.Module module)
  {
    this.module = module;
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
