/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import mlsub.typing.*;

import gnu.bytecode.*;
import gnu.expr.Declaration;

import java.util.*;

/**
   Declaration of a local variable
   with an optional initial value.
   
   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class GlobalVarDeclaration extends Definition
{
  public GlobalVarDeclaration(LocatedString name, Monotype type, Expression value)
  {
    super(name, Node.global);
    
    this.left = new MonoSymbol(name,type)
      {
	Declaration getDeclaration()
	{
	  Declaration res = super.getDeclaration();
	  
	  if (res == null)
	    {
	      res = module.addGlobalVar
		(left.name.toString(), 
		 nice.tools.code.Types.javaType(left.type));
	      setDeclaration(res);
	    }

	  return res;
	}
      };
    addChild(left);
    
    if (value != null)
      this.value = value;
  }

  public Collection associatedDefinitions()
  {
    return null;
  }
  
  void resolve()
  {
    if (value != null)
      value = bossa.syntax.dispatch.analyse(value, scope, typeScope);
  }
  
  /****************************************************************
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
  }

  /****************************************************************
   * Type checking
   ****************************************************************/
  
  void typecheck()
  {
    if(value==null) return;
    try{
      value.resolveOverloading(left.getType());
      bossa.syntax.dispatch.typecheck(value);
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
    gnu.expr.Declaration declaration = left.getDeclaration();

    if (value == null)
      declaration.noteValue(null);
    else
      declaration.noteValue(value.compile());
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return left + (value==null ? "" : " = " + value);
  }

  protected Expression value=null;
  // "name" after scoping
  MonoSymbol left;
}
