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
  */
  public LoopExp(Expression whileExp,
		 Expression loopBody,
		 Expression beforeNextIteration)
  {
    this.whileExp = whileExp;
    this.loopBody = loopBody;
    this.beforeNextIteration = beforeNextIteration;
  }
    
  private Expression whileExp, loopBody, beforeNextIteration;
    
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
      
    code.emitGoto(test);

    start.define(code);
    if (loopBody != null)
      loopBody.compile(comp, Target.Ignore);
    beforeNextIteration.compile(comp, Target.Ignore);

    test.define(code);
    compileIfJump(comp, whileExp, start);
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
