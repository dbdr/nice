package gnu.expr;

import gnu.bytecode.Label;

public interface Branchable extends Inlineable
{
  /** 
      Jump to label if the expression yields true.
  */
  public void compileJump (Compilation comp, Expression[] args, Label to);

  /** 
      Jump to label if the expression yields false.
  */
  public void compileJumpNot (Compilation comp, Expression[] args, Label to);

}
