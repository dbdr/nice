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

import java.util.*;
import bossa.util.*;

import mlsub.typing.Polytype;

/**
   A block : a list of statements with local variables.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class Block extends Statement
{
  public Block(List statements)
  {
    this.statements = cutInBlocks(statements);
  }

  public static class LocalDeclaration extends Statement
  {
    public LocalDeclaration(LocatedString name, Monotype type, Expression value)
    {
      this.left = new MonoSymbol(name,type);
      this.value = value;
    }
    
    public gnu.expr.Expression generateCode()
    {
      Internal.error("Should not be called");
      return null;
    }
    
    protected Expression value = null;
    MonoSymbol left;
  }

  ArrayList /* of LocalDeclaration */ locals = new ArrayList();

  /**
     Divides the statements in an hierarchy of blocks.
     
     Each block has a list of local variables
     accessible in the block, and a list of statements (possibly sub-blocks).
     
     @param statements the list of statements, with LocalDeclarations
     @return the list of statements of the current block.
   */
  private Statement[] cutInBlocks(List statements)
  {
    ArrayList res = new ArrayList();
    ListIterator i = statements.listIterator();

    // Finds locals at the beginning
    while(i.hasNext())
      {
	Object s = i.next();
	
	if (s == null) // emptyStatement
	  continue;
	
	if (s instanceof LocalDeclaration)
	  {
	    LocalDeclaration decl = (LocalDeclaration) s;
	    locals.add(decl);
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
	Object s = i.next();
	
	if (s == null) // emptyStatement
	  continue;
	
	if (s instanceof LocalDeclaration)
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
    
    return (Statement[]) res.toArray(new Statement[res.size()]);
  }

  /****************************************************************
   * Type checking
   ****************************************************************/
  
  Polytype getType()
  {
    if (statements.length > 0)
      {
	Object o = statements[statements.length - 1];
	if (o instanceof ReturnStmt)
	  {
	    ReturnStmt r = (ReturnStmt) o;
	    if (r.value != null)
	      {
		r.value.noOverloading();
		return r.value.getType();
	      }
	  }
      }
    return ConstantExp.voidPolytype;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression generateCode()
  {
    gnu.expr.ScopeExp save = Statement.currentScopeExp;

    gnu.expr.Expression body;

    if (statements.length != 0)
      body = new gnu.expr.BeginExp(null);
    else
      body = gnu.expr.QuoteExp.voidExp;

    // addLocals must appear before Statement.compile(statements)
    // in order to define the locals before their use.
    // That is why we can't set body's body at construction.
    gnu.expr.Expression res = addLocals(locals.iterator(), body);

    if (statements.length != 0)
      ((gnu.expr.BeginExp) body).setExpressions(Statement.compile(statements));

    Statement.currentScopeExp = save;

    return res;
  }

  private gnu.expr.Expression addLocals(Iterator vars, 
					gnu.expr.Expression body)
  {
    if (!vars.hasNext())
      return body;
    
    LocalDeclaration local = (LocalDeclaration) vars.next();

    gnu.expr.Expression[] eVal = new gnu.expr.Expression[1];
    gnu.expr.LetExp res = new gnu.expr.LetExp(eVal);

    res.outer = Statement.currentScopeExp;
    Statement.currentScopeExp = res;
    
    if (local.value == null)
      eVal[0] = nice.tools.code.Types.defaultValue(local.left.type);
    else
      eVal[0] = local.value.generateCode();
    gnu.expr.Declaration decl = 
      res.addDeclaration(local.left.name.toString(),
			 nice.tools.code.Types.javaType(local.left.type));
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

  Statement[] statements;
}
