/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
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

  public Statement last()
  {
    return statements[statements.length - 1];
  }

  static abstract class LocalDeclaration extends Statement
  {
    abstract String getName();
    abstract VarSymbol getSymbol();
    abstract gnu.bytecode.Type getBytecodeType();

    Expression value;

    gnu.expr.Expression initValue()
    {
      return value.generateCode();
    }

    public Location location()
    {
      return getSymbol().name.location();
    }

    gnu.expr.Expression compile(gnu.expr.LetExp let)
    {
      gnu.expr.Declaration decl = 
	let.addDeclaration(getName(), getBytecodeType());
      decl.noteValue(null);
      if (! getSymbol().isAssignable())
	decl.setFlag(gnu.expr.Declaration.IS_CONSTANT);
      getSymbol().setDeclaration(decl);

      return initValue();
    }

    public gnu.expr.Expression generateCode()
    {
      Internal.error("Should not be called");
      return null;
    }
  }

  public static abstract class LocalValue extends LocalDeclaration
  {
    public LocalValue()
    {
      this.last = this;
    }

    /* 
       Local variables are chained to handle multiple var declarations like
       "int x = 3, y = x, z = y;"
       We have to be careful about the order, so that dependencies like above
       work as expected.
    */
    LocalValue next, last;
  }

  public static class LocalVariable extends LocalValue
  {
    public LocalVariable(LocatedString name, Monotype type, 
			 boolean constant, Expression value)
    {
      this.value = value;
      if (constant)
	this.left = new MonoSymbol(name,type) { 
	    boolean isAssignable() { return false; }
	  };
      else
	this.left = new MonoSymbol(name,type);
      this.last = this;
    }
    
    String getName() { return left.name.toString(); }
    VarSymbol getSymbol() { return left; }
    gnu.bytecode.Type getBytecodeType() { return Types.javaType(left.type); }

    gnu.expr.Expression initValue()
    {
      if (value == null)
	return Types.defaultValue(left.type);
      else
	return value.generateCode();
    }

    MonoSymbol left;

    public void addNext(LocatedString name, Expression value)
    {
      last.next = new LocalVariable(name, left.syntacticType, 
				    ! left.isAssignable(), value);
      last = last.next;
    }
  }

  public static class LocalConstant extends LocalValue
  {
    public LocalConstant(LocatedString name, Expression value)
    {
      this.value = value;
      this.left = new PolySymbol(name,null);
      this.last = this;
    }
    
    String getName() { return left.name.toString(); }
    VarSymbol getSymbol() { return left; }
    gnu.bytecode.Type getBytecodeType() { return Types.javaType(left.type); }

    PolySymbol left;

    public void addNext(LocatedString name, Expression value)
    {
      last.next = new LocalConstant(name, value);
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

    String getName() { return left.name.toString(); }
    VarSymbol getSymbol() { return left; }
    gnu.bytecode.Type getBytecodeType() { return Types.javaType(left.type); }

    mlsub.typing.Polytype inferredReturnType()
    {
      return ((FunExp) value).inferredReturnType();
    }

    mlsub.typing.Monotype declaredReturnType()
    {
      return Types.codomain(left.getType());
    }

    FunSymbol left;
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
	
        if (s instanceof LocalValue)
          {
            LocalValue decl = (LocalValue) s;
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
