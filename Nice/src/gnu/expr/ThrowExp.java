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

  public void compile (Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    exception.compile(comp, Target.pushObject);
    code.emitThrow();
  }

  public final Type getType()
  {
    return Type.void_type;
  }

  Expression exception;

  protected Expression walk(ExpWalker w)
  {
    return w.walkThrowExp(this);
  }
  
  public void print(gnu.mapping.OutPort out)
  {
    out.startLogicalBlock("(Throw", ")", 2);
    if (exception == null)
      out.print("<null exception>");
    else
      exception.print(out);
    out.endLogicalBlock(")");
  }
}
