package gnu.expr;

import gnu.bytecode.*;

/**
 * A generic loop.
 * 
 * @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */

public class LoopExp extends Expression
{
  /**
     @param loopBody can be null to mean "do nothing"
     @testFirst If true, test before each iteration. 
                If false, test after each iteration.
  */
  public LoopExp(Expression whileExp,
		 Expression beforeNextIteration,
		 boolean testFirst)
  {
    this.whileExp = whileExp;
    this.beforeNextIteration = beforeNextIteration;
    this.testFirst = testFirst;
  }

  public void setBody(Expression loopBody)
  {
    this.loopBody = loopBody;
  }

  private Expression whileExp, loopBody, beforeNextIteration;
  private boolean testFirst;

  public void compile(Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();

    /*
      The test is placed at the end of the loop.
      This leads to N+1 gotos for N iterations.
      
      A test at the begining would lead to 2xN+1 gotos.
    */
    Label start = new Label(code);
    Label test  = new Label(code);
    continueLabel = new Label(code);

    if (testFirst)
      code.emitGoto(test);

    start.define(code);
    if (loopBody != null)
      loopBody.compile(comp, Target.Ignore);

    continueLabel.define(code);
    beforeNextIteration.compile(comp, Target.Ignore);

    test.define(code);
    compileIfJump(comp, whileExp, start);

    continueLabel = null;
  }

  private Label continueLabel;

  /**
     Break out of the loop.
  */
  public class ContinueExp extends Expression
  {
    public void compile(Compilation comp, Target target)
    {
      comp.getCode().emitGoto(LoopExp.this.continueLabel);
    }

    public Type getType()
    {
      return Type.void_type;
    }

    public void print(gnu.mapping.OutPort out)
    {
      out.print("(Continue)");
    }
  }

  /** 
      Jump to label <code>to</code> if <code>ifExp</code> is true.

      Optimizes the case where ifExp is a integer comparison, 
      since specific JVM bytecode handle these cases.
  */
  private void compileIfJump(Compilation comp, Expression ifExp, Label to)
  {
    if (ifExp instanceof ApplyExp)
      {
	ApplyExp app = (ApplyExp) ifExp;
	if (app.func instanceof QuoteExp)
	  {
	    Object proc = ((QuoteExp) app.func).getValue();
	    if (proc instanceof nice.lang.inline.CompOp)
	      {
		nice.lang.inline.CompOp op = (nice.lang.inline.CompOp) proc;
		op.compileJump(comp, app.args, to);
		return;
	      }
	  }
      }

    // General case
    whileExp.compile(comp, Type.boolean_type);
    comp.getCode().emitGotoIfIntNeZero(to);
  }

  public Type getType()
  {
    return Type.void_type;
  }
  
  protected Expression walk(ExpWalker w)
  {
    whileExp.walk(w);
    if (loopBody != null)
      loopBody.walk(w);
    beforeNextIteration.walk(w);
    return this;
  }
  
  public void print(gnu.mapping.OutPort out)
  {
    out.startLogicalBlock("(Loop", ")", 2);
    if (whileExp != null)
     whileExp.print(out);
    out.writeSpaceLinear();
    if (loopBody != null)
      loopBody.print(out);
    if (beforeNextIteration != null)
      {
	out.writeSpaceLinear();
        out.print("next ");
        beforeNextIteration.print(out);
      }
    out.endLogicalBlock(")");
  }
}
