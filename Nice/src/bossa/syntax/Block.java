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

// File    : Block.java
// Created : Wed Jul 07 17:42:15 1999 by bonniot
//$Modified: Fri May 12 18:58:03 2000 by Daniel Bonniot $
// Description : A block : a list of statements with local variables

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class Block extends Statement
{
  public Block(List statements)
  {
    super(Node.down);
    this.statements=addChildren(cutInBlocks(statements));
    //addChildren(locals);
  }

  public static class LocalDeclaration extends Statement
  {
    public LocalDeclaration(LocatedString name, Monotype type, Expression value)
    {
      super(Node.forward);
      this.left=new MonoSymbol(name,type);
      //addChild(this.left);
      
      if(value!=null)
	//this.value=expChild(value);
	this.value = new ExpressionRef(value);
    }
    
    public gnu.expr.Expression generateCode()
    {
      Internal.error("Should not be called");
      return null;
    }
    
    protected ExpressionRef value=null;
    MonoSymbol left;
  }

  private ArrayList /* of LocalDeclaration */ locals=new ArrayList();

  /**
   * Divides the statements in an hierarchy of blocks.
   *
   * Each block has a list of local variables
   * accessible in the block, and a list of statements (possibly sub-blocks).
   *
   * @param statements the list of statements, with LocalDeclarations
   * @return the list of statements of the current block.
   */
  private List cutInBlocks(List statements)
  {
    ArrayList res=new ArrayList();
    ListIterator i=statements.listIterator();

    // Finds locals at the beginning
    while(i.hasNext())
      {
	Object s=i.next();
	
	if(s==null) // emptyStatement
	  continue;
	
	if(s instanceof LocalDeclaration)
	  {
	    LocalDeclaration decl = (LocalDeclaration) s;
	    locals.add(decl);
	    addChild(decl.left);
	    if(decl.value!=null)
	      addChild(decl.value);
	  }
	else 
	  {
	    res.add(s);
	    break;
	  }
      }
    // There will be no more locals, let's save space :-)
    locals.trimToSize();
    
    // Finds statements. Creates a new Block if a local declaration is found
    while(i.hasNext())
      {
	Object s=i.next();
	
	if(s==null) // emptyStatement
	  continue;
	
	if(s instanceof LocalDeclaration)
	  {
	    // Removes all the statements already in res, 
	    // keeps this LocalDeclarationStmt.
	    res.add(new Block(statements.subList(i.previousIndex(),statements.size())));
	    break;
	  }
	
	res.add(s);
      }

    // idem
    res.trimToSize();
    
    return res;
  }

  /****************************************************************
   * Java Classes
   ****************************************************************/

  void findJavaClasses()
  {
    for(Iterator i=locals.iterator();
	i.hasNext();)
      {
	LocalDeclaration d = (LocalDeclaration) i.next();
	d.left.getMonotype().resolve(typeScope);
      }
  }
  
  /****************************************************************
   * Type checking
   ****************************************************************/

  /**
   * Checks that the local bindings are type-safe.
   *
   * This may involve overloading resolution on the values.
   * 
   * Typecheking is done after the children, since
   * an error in a child is more intuitive.
   * There is no dependency in the order.
   */
  void endTypecheck()
  {
    for(Iterator i=locals.iterator();
	i.hasNext();)
      {
	LocalDeclaration local = (LocalDeclaration) i.next();
	if(local.value==null)
	  continue;
	try{
	  AssignExp.checkAssignment(local.left.getType(),local.value);
	}
	catch(bossa.typing.TypingEx t){
	  User.error(local.left,"Typing error : "+local.left.name+
		     " cannot be assigned value \""+local.value+"\"",
		     " of type "+local.value.getType()+" since "+
		     local.left.name+" has type "+
		     local.left.type);
	}
      }
  }
  
  Polytype getType()
  {
    if(statements.size()>0)
      {
	Object o=statements.get(statements.size()-1);
	if(o instanceof ReturnStmt)
	  {
	    ((ReturnStmt)o).value.noOverloading();
	    return ((ReturnStmt)o).value.getType();
	  }
      }
    return Polytype.voidType(typeScope);
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression generateCode()
  {
    gnu.expr.ScopeExp save = Statement.currentScopeExp;

    gnu.expr.Expression body;

    if(statements.size()!=0)
      body = new gnu.expr.BeginExp(null);
    else
      body = gnu.expr.QuoteExp.voidExp;

    // addLocals must appear before Statement.compile(statements)
    // in order to define the locals before their use.
    // That is why we can't set body's body at construction.
    gnu.expr.Expression res = addLocals(locals.iterator(),body);

    if(statements.size()!=0)
      ((gnu.expr.BeginExp) body).setExpressions(Statement.compile(statements));

    Statement.currentScopeExp = save;

    return res;
  }

  private gnu.expr.Expression addLocals(Iterator vars, gnu.expr.Expression body)
  {
    if(!vars.hasNext())
      return body;
    
    LocalDeclaration local = (LocalDeclaration) vars.next();

    gnu.expr.Expression[] eVal=new gnu.expr.Expression[1];
    gnu.expr.LetExp res = new gnu.expr.LetExp(eVal);

    res.outer = Statement.currentScopeExp;
    Statement.currentScopeExp = res;
    
    if(local.value==null)
      eVal[0] = local.left.type.defaultValue();
    else
      eVal[0] = local.value.generateCode();
    gnu.expr.Declaration decl = 
      res.addDeclaration(local.left.name.toString(),
			 local.left.type.getJavaType());
    decl.noteValue(null);
    local.left.setDeclaration(decl);
    
    res.setBody(addLocals(vars,body));
    res.setLine(local.left.name.location().getLine());
    
    return res;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "{\n"
      + Util.map("",";\n",";\n",statements)
      + "}\n";
  }

  private List /* of Statement */ statements;
}
