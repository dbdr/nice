package kawa.standard;
import kawa.lang.*;
import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.expr.*;

public class or extends Procedure2 implements Inlineable
{
  public Object apply2 (Object arg1, Object arg2)
  {
    return new Boolean(((Boolean) arg1).booleanValue() || ((Boolean) arg2).booleanValue());
  }

  static gnu.bytecode.ClassType typeType;
  static gnu.bytecode.Method instanceMethod;

  public void compile (ApplyExp exp, Compilation comp, Target target)
  {
    Expression[] args = exp.getArgs();
    CodeAttr code = comp.getCode();

    Target stack = new StackTarget(retType);
    
    args[0].compile(comp, stack);
    args[1].compile(comp, stack);
    code.emitIOr();
    
    target.compileFromStack(comp, retType);
  }

  private final Type retType = //Type.boolean_type;
    Scheme.booleanType;
  
  
  public Type getReturnType (Expression[] args)
  {
    return retType;
  }
}
