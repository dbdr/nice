package gnu.expr;

import gnu.bytecode.CodeAttr;
import gnu.bytecode.Type;

/**
 * Expression that throws an exception.
 * 
 * @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */

public class ThrowExp extends Expression
{
  public ThrowExp(Expression exception)
  {
    this.exception = exception;
  }

  Object walk(ExpWalker w)
  {
    return w.walkThrowExp(this);
  }
  
  public void compile (Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    exception.compile(comp, Target.pushObject);
    code.emitThrow();
  }

  public void print(java.io.PrintWriter pw)
  {
    pw.print("(%throw ");
    exception.print(pw);
    pw.print(")");
  }
  
  public final Type getType()
  {
    return Type.void_type;
  }

  Expression exception;
}
