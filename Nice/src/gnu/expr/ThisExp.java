package gnu.expr;
import gnu.bytecode.*;

/** Evaluates to the "this" implicit variable.
 * This is currently neither robust nor general.  FIXME!
 */

public class ThisExp extends ReferenceExp
{
  /** The class which this refers to. */
  Expression context;

  public ThisExp ()
  {
    super("$this$");
  }

  public ThisExp(Expression context)
  {
    super("$this$");
    this.context = context;
  }

  public ThisExp (Declaration binding)
  {
    super("$this", binding);
  }

  public ThisExp (ClassType type)
  {
    this(new Declaration("this", type));
  }

  public void compile (Compilation comp, Target target)
  {
    // If we have been captured, load ourselve the hard way.
    if (binding != null && binding.field != null)
      binding.load(comp);
    else
      comp.getCode().emitPushThis();
  }

  protected Expression walk (ExpWalker walker)
  {
    return walker.walkThisExp(this);
  }

  public final gnu.bytecode.Type getType()
  {
    if (binding != null)
      return binding.getType();
    if (context != null && context instanceof ClassExp)
      return ((ClassExp) context).getType();
    return Type.pointer_type;
  }
}
