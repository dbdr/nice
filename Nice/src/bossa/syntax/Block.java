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

import nice.tools.code.Types;

import bossa.util.*;
import java.util.*;

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

  static abstract class LocalDeclaration extends Statement
  {
    abstract gnu.expr.Expression compile(gnu.expr.LetExp let);

    public gnu.expr.Expression generateCode()
    {
      Internal.error("Should not be called");
      return null;
    }
  }

  public static class LocalVariable extends LocalDeclaration
  {
    public LocalVariable(LocatedString name, Monotype type, Expression value)
    {
      this.value = value;
      this.left = new MonoSymbol(name,type);
      this.last = this;
    }
    
    gnu.expr.Expression compile(gnu.expr.LetExp let)
    {
      gnu.expr.Declaration decl = 
	let.addDeclaration(left.name.toString(), Types.javaType(left.type));
      decl.noteValue(null);
      left.setDeclaration(decl);

      if (value == null)
	return Types.defaultValue(left.type);
      else
	return value.generateCode();
    }

    public Location location()
    {
      return left.name.location();
    }

    Expression value;
    MonoSymbol left;
    public MonoSymbol getLeft() { return left; }

    /* 
       Local variables are chained to handle multiple var declarations like
       "int x = 3, y = x, z = y;"
       We have to be careful about the order, so that dependencies like above
       work as expected.
    */
    LocalVariable next, last;

    public void addNext(LocatedString name, Expression value)
    {
      last.next = new LocalVariable(name, left.syntacticType, value);
      last = last.next;
    }
  }

  public static class LocalFunction extends LocalDeclaration
  {
    public static LocalFunction make
      (LocatedString name, Monotype returnType, 
       FormalParameters parameters, Statement body)
    {
      Expression value;
      value = new FunExp(Constraint.True, parameters.getMonoSymbols(), body);

      FunSymbol symbol = new FunSymbol(name, 
				       Constraint.True, parameters, 
				       returnType, parameters.size);
      symbol.syntacticType.getMonotype().nullness = Monotype.sure;
      return new LocalFunction(symbol, value);
    }

    private LocalFunction(FunSymbol symbol, Expression value)
    {
      this.value = value;
      this.left = symbol;
    }

    mlsub.typing.Polytype inferredReturnType()
    {
      return ((FunExp) value).inferredReturnType();
    }

    mlsub.typing.Monotype declaredReturnType()
    {
      return Types.codomain(left.getType());
    }

    gnu.expr.Expression compile(gnu.expr.LetExp let)
    {
      gnu.expr.Declaration decl = 
	let.addDeclaration(left.name.toString(),
			   Types.javaType(left.type));
      decl.noteValue(null);
      left.setDeclaration(decl);

      return value.generateCode();
    }

    public Location location()
    {
      return left.name.location();
    }

    Expression value;
    FunSymbol left;
    public FunSymbol getLeft() { return left; }
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
	
        if (s instanceof LocalVariable)
          {
            LocalVariable decl = (LocalVariable) s;
            do
              {
                locals.add(decl);
                decl = decl.next;
              }
            while (decl != null);
          }
	else if (s instanceof LocalDeclaration)
	  locals.add(s);
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
	    // keeps this LocalDeclStmt.
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
  
  mlsub.typing.Polytype getType()
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
    gnu.expr.Expression body;

    if (statements.length != 0)
      body = new gnu.expr.BeginExp();
    else
      body = gnu.expr.QuoteExp.voidExp;

    // addLocals must appear before Statement.compile(statements)
    // in order to define the locals before their use.
    // That is why we can't set body's body at construction.
    gnu.expr.Expression res = addLocals(locals.iterator(), body);

    if (statements.length != 0)
      ((gnu.expr.BeginExp) body).setExpressions(Statement.compile(statements));

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

    eVal[0] = local.compile(res);
    
    res.setBody(addLocals(vars,body));
    res.setLine(local.location().getLine());
    
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
