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
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
*/
public class GlobalVarDeclaration extends Definition
{
  public GlobalVarDeclaration(LocatedString name, Monotype type, Expression value, boolean constant)
  {
    super(name, Node.global);
    
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
          res = new gnu.expr.Declaration
            (name.toString(), nice.tools.code.Types.javaType(type));
          setDeclaration(res);

          if (! inInterfaceFile())
            {
              // Compute the value first, which might use another global value,
              // which will then be properly initialized before use.
              res.noteValue(GlobalVarDeclaration.this.compileValue());
            }

          module.addGlobalVar(res, constant);
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
      s.print( "let ");
    else	
      s.print( "var ");

    s.print(left + " = " +value.toString() + ";\n");
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public void compile()
  {
    left.getDeclaration();
  }

  private gnu.expr.Expression compileValue()
  {
    return
      this.value != null ? this.value.compile() : null;
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
