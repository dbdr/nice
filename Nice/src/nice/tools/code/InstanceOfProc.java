package nice.tools.code;

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

public class InstanceOfProc extends Procedure1 implements Inlineable
{
  public InstanceOfProc(Type type)
  {
    this.type = type;
  }

  private Type type;
  
  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    args[0].compile(comp, Target.pushObject);
    type.emitIsInstance(code);
    target.compileFromStack(comp, Type.boolean_type);
  }

  public Type getReturnType (Expression[] args)
  {
    return Type.boolean_type;
  }

  public Object apply1 (Object arg)
  {
    throw new Error("Not implemented");
  }
}
