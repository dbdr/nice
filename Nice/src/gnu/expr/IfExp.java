// Copyright (c) 2001  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.expr;
import gnu.bytecode.*;
import gnu.mapping.*;

/**
 * This class represents a conditional.
 * @author	Per Bothner
 */

public class IfExp extends Expression
{
  Expression test;
  Expression then_clause;
  Expression else_clause;

  public IfExp (Expression i, Expression t, Expression e)
  {
    test = i;  then_clause = t;  else_clause = e;
  }

  /**
     Create an if expression.
     Optimizes the case where i is a constant.
   */
  public static Expression make(Expression i, Expression t, Expression e)
  {
    Interpreter interpreter = Interpreter.getInterpreter();

    if (i instanceof QuoteExp)
      if (interpreter.isTrue(((QuoteExp) i).getValue()))
	return t;
      else
	return e;

    return new IfExp(i, t, e);
  }

  protected final Interpreter getInterpreter()
  {
    return Interpreter.defaultInterpreter; // FIXME
  }

  public Object eval (Environment env) throws Throwable
  {
    Interpreter interpreter = getInterpreter();
    if (interpreter.isTrue((test.eval (env))))
      return then_clause.eval (env);
    else if (else_clause != null)
      return else_clause.eval (env);
    else
      return interpreter.noValue();
  }

  public void eval (Environment env, CallContext ctx) throws Throwable
  {
    Interpreter interpreter = getInterpreter();
    if (interpreter.isTrue((test.eval (env))))
      then_clause.eval (env, ctx);
    else if (else_clause != null)
      else_clause.eval(env, ctx);
  }

  public void compile (Compilation comp, Target target)
  {
    compile(test, then_clause,
	    else_clause == null ? QuoteExp.voidExp : else_clause,
	    comp, target);
  }

  public static void compile (Expression test, Expression then_clause,
			      Expression else_clause,
			      Compilation comp, Target target)
  {
    Interpreter interpreter = comp.getInterpreter();
    gnu.bytecode.CodeAttr code = comp.getCode();
    Label trueLabel, falseLabel;
    boolean trueInherited, falseInherited;
    // A constant else_clause results from the expansion of (and ...),
    // and also if the else_clause if elided, so we optimize this case.
    if (target instanceof ConditionalTarget
	&& else_clause instanceof QuoteExp)
      {
	falseInherited = true;
	Object value = ((QuoteExp) else_clause).getValue();
	if (interpreter.isTrue(value))
	  falseLabel = ((ConditionalTarget) target).ifTrue;
	else
	  falseLabel = ((ConditionalTarget) target).ifFalse;
      }
    else if (else_clause instanceof ExitExp
             && ((ExitExp) else_clause).result instanceof QuoteExp
             && ((ExitExp) else_clause).block.subTarget instanceof IgnoreTarget)
      {
        falseInherited = true;
        falseLabel = ((ExitExp) else_clause).block.exitLabel;
      }
    else
      {
	falseInherited = false;
	falseLabel = new Label(code);
      }
    // The expansion of "or" creates an IfExp with test==then_clause.
    // In that case, we know that the then_clause must be true.
    // Let's optimize that case.
    if (test == then_clause && target instanceof ConditionalTarget
	&& then_clause instanceof ReferenceExp)
      {
	trueInherited = true;
	trueLabel = ((ConditionalTarget) target).ifTrue;
      }
    else
      {
	trueInherited = false;
	trueLabel = new Label(code); 
      }
    ConditionalTarget ctarget
      = new ConditionalTarget(trueLabel, falseLabel, interpreter);
    if (trueInherited)
      ctarget.trueBranchComesFirst = false;
    test.compile(comp, ctarget);
    code.emitIfThen();
    if (! trueInherited /* && trueLabel.hasFixups()*/)
      {
	trueLabel.define(code);
	then_clause.compileWithPosition(comp, target);
      }
    if (! falseInherited /* && falseLabel.hasFixups()*/)
      {
	code.emitElse();
	falseLabel.define(code);
	if (else_clause == null)
	  comp.compileConstant(Values.empty, target);
	else
	  else_clause.compileWithPosition(comp, target);
      }
    else
      code.setUnreachable();
    code.emitFi();
  }

  public Type getType()
  {
    Type thenType = then_clause.getType();
    Type elseType = else_clause.getType();
    
    Type res;
    if (thenType.isVoid())
      res = thenType;
    else if (elseType.isVoid())
      res = elseType;
    else
      res = Type.lowestCommonSuperType(thenType, elseType);

    if(res==null)
      {
	throw new Error("Incompatible types in "+this+
			": "+thenType+" and "+elseType);
      }
    
    return res;
  }

  protected Expression walk (ExpWalker walker)
  {
    return walker.walkIfExp(this);
  }

  protected void walkChildren(ExpWalker walker)
  {
    test = test.walk(walker);
    if (walker.exitValue == null)
      then_clause = then_clause.walk(walker);
    if (walker.exitValue == null)
     else_clause = else_clause.walk(walker);
  }

  public void print (OutPort out)
  {
    out.startLogicalBlock("(If ", false, ")");
    out.setIndentation(-2, false);
    test.print(out);
    out.writeSpaceLinear();
    then_clause.print (out);
    if (else_clause != null)
      {
	out.writeSpaceLinear();
	else_clause.print (out);
      }
    out.endLogicalBlock(")");
  }
}
