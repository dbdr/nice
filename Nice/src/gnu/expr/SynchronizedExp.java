package gnu.expr;
import gnu.bytecode.*;
import gnu.mapping.*;

public class SynchronizedExp extends Expression
{
  Expression object;
  Expression body;

  public SynchronizedExp (Expression object, Expression body)
  {
    this.object = object;
    this.body = body;
  }

  public Object eval (Environment env) throws Throwable
  {
    Object value = object.eval(env);
    synchronized (value)
      {
	return body.eval(env);
      }
  }

  public void compile (Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    object.compile (comp, Target.pushObject);
    code.emitDup(1);
    Scope scope = code.pushScope();
    Variable objvar = scope.addVariable(code, Type.pointer_type, null);
    code.emitStore(objvar); 
    code.emitMonitorEnter();
    code.emitTryStart(true,
		      (target instanceof IgnoreTarget
		       || target instanceof ConsumerTarget) ? null
		      : target.getType());

    body.compileWithPosition(comp, target);
    
    code.emitTryEnd();
    code.emitFinallyStart();
    code.emitLoad(objvar);
    code.emitMonitorExit(); 
    code.emitFinallyEnd();
    code.emitTryCatchEnd();

    code.popScope();
 }

  protected Expression walk (ExpWalker walker)
  {
    return walker.walkSynchronizedExp(this);
  }

  protected void walkChildren(ExpWalker walker)
  {
    object = object.walk(walker);
    if (walker.exitValue == null)
      body = body.walk(walker);
  }

  public void print (OutPort ps)
  {
    ps.print("(Synchronized ");
    object.print(ps);
    ps.print(" ");
    body.print(ps);
    ps.print(")");
  }
}
