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
//$Modified: Wed Dec 01 17:29:45 1999 by bonniot $
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
  }

  public static class LocalDeclaration
  {
    public LocalDeclaration(LocatedString name, Monotype type, Expression value)
    {
      this.left=new MonoSymbol(name,type);
      if(value!=null)
	this.value=new ExpressionRef(value);
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
	if(s instanceof LocalDeclaration)
	  {
	    // Removes all the statements already in res, 
	    // keeps this LocalDeclarationStmt.
	    res.add(new Block(statements.subList(i.previousIndex(),statements.size())));
	    break;
	  }
	else 
	  res.add(s);
      }
    // idem
    res.trimToSize();
    
    return res;
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  /**
   * Checks that the local bindings are type-safe.
   * This may involve overloading resolution on the values.
   */
  void typecheck()
  {
    for(Iterator i=locals.iterator();
	i.hasNext();)
      {
	LocalDeclaration local = (LocalDeclaration) i.next();
	if(local.value==null)
	  continue;
	try{
	  AssignStmt.checkAssignment(local.left.getType(),local.value);
	}
	catch(bossa.typing.TypingEx t){
	  User.error(local.left,"Typing error : "+local.left+
		     " cannot be assigned value "+local.value);
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

  public gnu.expr.Expression compile()
  {
    if(statements.size()==0)
      return new gnu.expr.QuoteExp(null);
    
    gnu.expr.Expression[] eVal=new gnu.expr.Expression[locals.size()];
    gnu.expr.LetExp res=new gnu.expr.LetExp(eVal);
    
    int n=0;
    for(Iterator i=locals.iterator();
	i.hasNext();n++)
      {
	LocalDeclaration local = (LocalDeclaration) i.next();
	if(local.value==null)
	  eVal[n]=new gnu.expr.QuoteExp(null);
	else
	  eVal[n]=local.value.compile();
	local.left.setDeclaration(res.addDeclaration(local.left.name.toString(),
						     local.left.type.getJavaType()));
      }
    
    res.setBody(new gnu.expr.BeginExp(Statement.compile(statements)));
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
