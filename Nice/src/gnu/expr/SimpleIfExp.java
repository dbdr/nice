package gnu.expr;
import gnu.bytecode.*;
import gnu.mapping.*;

/**
 * A simplified IfExp that makes use of the Branchable interface
 */

public class SimpleIfExp extends IfExp
{

  public SimpleIfExp (Expression i, Expression t, Expression e)
  {
    super(i,t,e);
  }

  public static Expression make(Expression i, Expression t, Expression e)
  {
    return new SimpleIfExp(i, t, e);
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
    gnu.bytecode.CodeAttr code = comp.getCode();
    Label elseLabel;
    elseLabel = new Label(code); 
    Branchable branchOp = test.getBranchable();
    if (branchOp != null)
    {
      branchOp.compileJumpNot(comp, ((ApplyExp)test).getArgs(), elseLabel);
    }
    else
    {
      Target stack = new StackTarget(Type.boolean_type);    
      test.compile(comp, stack);
      code.emitGotoIfIntEqZero(elseLabel);
    }
    code.emitIfThen();
    then_clause.compileWithPosition(comp, target);
    if (else_clause instanceof QuoteExp && ((QuoteExp)else_clause).getValue()==Values.empty)
    {
      code.setUnreachable();
      elseLabel.define(code);
    }
    else
    {
      code.emitElse();
      elseLabel.define(code);
      else_clause.compileWithPosition(comp, target);
    }
    code.emitFi();
  }

}
