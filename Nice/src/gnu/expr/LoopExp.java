package gnu.expr;

import gnu.bytecode.*;

/**
 * A generic loop.
 * 
 * @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */

public class LoopExp extends Expression
{
  public LoopExp(Expression whileExp,
		 Expression loopBody,
		 Expression beforeNextIteration)
  {
    this.whileExp = whileExp;
    this.loopBody = loopBody;
    this.beforeNextIteration = beforeNextIteration;
  }
    
  private Expression whileExp, loopBody, beforeNextIteration;
    
  Object walk(ExpWalker w)
  {
    whileExp.walk(w);
    loopBody.walk(w);
    beforeNextIteration.walk(w);
    return this;
  }
  
  public void compile(Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    Label start = new Label(code);
    Label 
      trueLabel = new Label(code), 
      falseLabel = new Label(code);
      
    ConditionalTarget ctarget;
    ctarget = new ConditionalTarget(trueLabel, falseLabel, 
				    comp.getInterpreter());

    start.define(code);
    whileExp.compile(comp,
		     ctarget
		     //Target.pushObject
		     //Type.boolean_type
		     );
    
    code.emitIfThen();
    trueLabel.define(code);
    loopBody.compile(comp, Target.Ignore);
    beforeNextIteration.compile(comp, Target.Ignore);
    code.emitGoto(start);

    code.emitElse();
    falseLabel.define(code);
    code.emitFi();
  }

  public Type getType()
  {
    return Type.void_type;
  }

  public void print(java.io.PrintWriter pw)
  {
    pw.print("(#%loop (");
    whileExp.print(pw);
    pw.print(", ");
    loopBody.print(pw);
    pw.print(", ");
    beforeNextIteration.print(pw);
    pw.print(")");
  }
}
