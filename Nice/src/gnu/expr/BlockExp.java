// Copyright (c) 1999  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.expr;
import gnu.bytecode.*;
import gnu.mapping.OutPort;

/**
 * Class used to implement a block that can be exited.
 * @author	Per Bothner
 */

public class BlockExp extends Expression
{
  Declaration label;
  Expression body;

  /** If non-null, evaluate this, but only if non-normal exit. */
  Expression exitBody;

  public BlockExp ()
  {
  }

  public BlockExp (Expression body)
  {
    setBody(body);
  }

  public void setBody(Expression body)
  {
    this.body = body;
  }

  public void setBody(Expression body, Expression exitBody)
  {
    this.body = body;
    this.exitBody = exitBody;
  }

  /* Target used to evaluate body. Temporary only used during compilation. */
  Target subTarget;
  /* Label to exit to.  Temporary only used during compilation. */
  Label exitLabel;

  public void compile (Compilation comp, Target target)
  {
    Target subTarget;
    if (target instanceof IgnoreTarget
	|| target == Target.pushObject)
      subTarget = target;
    else
      {
	// We can probably do better - and this is probably
	// wrong for TailTargets.  FIXME.
	subTarget = new StackTarget(getType());
      }
    gnu.bytecode.CodeAttr code = comp.getCode();
    exitLabel = new Label(code);
    this.subTarget = exitBody == null ? subTarget : Target.Ignore;

    initialTryState = code.getTryStack();
    body.compileWithPosition(comp, subTarget);

    if (exitBody != null)
      {
        Label doneLabel = new Label(code);
        code.emitGoto(doneLabel);
        exitLabel.define(code);
        exitBody.compileWithPosition(comp, subTarget);
        doneLabel.define(code);
      }
    else
      exitLabel.define(code);
    if (subTarget != target)
      target.compileFromStack(comp, subTarget.getType());
  }

  // The try state in place at the beginning of the block
  TryState initialTryState;

  void exit(CodeAttr code)
  {
    // Run enclosing finally clauses
    for (TryState st = code.getTryStack(); st != initialTryState;
         st = st.previous)
      {
        if (st.finally_subr != null)
          code.emitJsr(st.finally_subr);
      }

    code.emitGoto(exitLabel);
  }

  protected Expression walk (ExpWalker walker)
  {
    return walker.walkBlockExp(this);
  }

  protected void walkChildren (ExpWalker walker)
  {
    body = body.walk(walker);
    if (walker.exitValue == null && exitBody != null)
      exitBody = exitBody.walk(walker);
  }

  public void print (OutPort out)
  {
    out.startLogicalBlock("(Block", ")", 2);
    if (label != null)
     out.print(label.getName());
    out.writeSpaceLinear();
    body.print(out);
    if (exitBody != null)
      {
	out.writeSpaceLinear();
        out.print("else ");
        exitBody.print(out);
      }
    out.endLogicalBlock(")");
  }
}
