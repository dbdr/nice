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
    super(Node.down);
    this.statements = addChildren(cutInBlocks(statements));
  }

  public static class LocalDeclaration extends Statement
  {
    public LocalDeclaration(LocatedString name, Monotype type, Expression value)
    {
      super(Node.forward);
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
  private List cutInBlocks(List statements)
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
	    addChild(decl.left);
	    if (decl.value != null)
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
    
    return res;
  }

  /****************************************************************
   * Type checking
   ****************************************************************/
  
  /**
     Checks that the local bindings are type-safe.
     
     This may involve overloading resolution on the values.
     
     We want fine-grained control about the order:
     it is more intuitive if right hand sides are typecheked first, 
     then assignments, 
     then statements.
   */
  void typecheck()
  {
    if (children != null)
      for (Iterator i = children.iterator(); i.hasNext();)
	{
	  Object o = i.next();
	  if (o instanceof Expression)
	    ((Expression) o).typecheck();
	}
    // removal is not necessary to avoid double typecheck
    // since there is a meachanism for this in Node,
    // that remembers if typechecking has already been done
    
    checkAssignments();
    
    // statements are children, they are going to be checked now
  }
  
  private void checkAssignments()
  {
    for(Iterator i = locals.iterator();
	i.hasNext();)
      {
	LocalDeclaration local = (LocalDeclaration) i.next();
	if (local.value == null)
	  continue;

	try{
	  local.value = 
	    AssignExp.checkAssignment(local.left.getType(),local.value);
	}
	catch(mlsub.typing.TypingEx t){
	  User.error(local.left,
		     "Typing error : " + local.left.name +
		     " cannot be assigned value " + local.value +
		     " of type " + local.value.getType(),
		     " since " +
		     local.left.name + " has type " + 
		     local.left.type);
	}
      }
  }
  
  Polytype getType()
  {
    if (statements.size()>0)
      {
	Object o = statements.get(statements.size()-1);
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

    if (statements.size() != 0)
      body = new gnu.expr.BeginExp(null);
    else
      body = gnu.expr.QuoteExp.voidExp;

    // addLocals must appear before Statement.compile(statements)
    // in order to define the locals before their use.
    // That is why we can't set body's body at construction.
    gnu.expr.Expression res = addLocals(locals.iterator(), body);

    if (statements.size() != 0)
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

  List /* of Statement */ statements;
}
