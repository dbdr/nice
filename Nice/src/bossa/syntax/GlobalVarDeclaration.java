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
  public GlobalVarDeclaration(LocatedString name, Monotype type, Expression value, boolean constant)
  {
    super(name, Node.global);
    if (constant && value == null)
      User.error(name,"A global constant needs a default value");
    
    this.left = new GlobalVarSymbol(name,type,constant);
    this.constant = constant;

    addChild(left);
    
    this.value = value;
  }

  class GlobalVarSymbol extends MonoSymbol 
  {
    GlobalVarSymbol(LocatedString name, Monotype type, boolean con)
    {
      super(name,type);
      constant = con;
    }
      
    boolean constant;

    boolean isAssignable()
    {
      return !constant;
    }

    Declaration getDeclaration()
    {
      Declaration res = super.getDeclaration();
      
      if (res == null)
        {
          res = module.addGlobalVar
    			(left.name.toString(),
		    	 nice.tools.code.Types.javaType(left.type),
			 constant);
          setDeclaration(res);
        }
    
      return res;
    }
    
    Expression getValue()
    {
      return GlobalVarDeclaration.this.value;
    }

    public Definition getDefinition()
    {
      return GlobalVarDeclaration.this;
    }
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
   * Type checking
   ****************************************************************/
  
  void typecheck()
  {
    if (value == null) 
      return;
    try{
      value = value.resolveOverloading(left.getType());
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
    if (constant) 
      s.print( "let " + left + " = " +value.toString() + ";\n");
    else	
      s.print( "var " + left + ";\n");
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public void precompile()
  {
    // Compute the value first, and this might use another global value,
    // which will then be properly initialized before use.
    gnu.expr.Expression value = 
      this.value != null ? this.value.compile() : null;

    gnu.expr.Declaration declaration = left.getDeclaration();
    if (constant) declaration.setFlag(Declaration.IS_CONSTANT);

    declaration.noteValue(value);
  }

  public void compile()
  {
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
  boolean constant;
}
