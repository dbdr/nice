package gnu.expr;
import gnu.mapping.*;
import gnu.bytecode.Type;

/**
 * An Expression that evaluates to a constant value.
 * @author	Per Bothner
 */

public class QuoteExp extends Expression
{
  Object value;
  Type type = null;

  public final Object getValue() { return value; }

  public final gnu.bytecode.Type getType()
  {
    if (type != null)
      return type;
    if (value == Values.empty)
      return gnu.bytecode.Type.void_type;
    if (value == null)
      return gnu.bytecode.Type.nullType;
    return gnu.bytecode.Type.make(value.getClass());
  }

  static public QuoteExp undefined_exp
  = new QuoteExp (Undefined.getInstance());
  static public QuoteExp voidExp = new QuoteExp (Values.empty);
  static public QuoteExp trueExp = new QuoteExp(Boolean.TRUE);
  static public QuoteExp falseExp = new QuoteExp(Boolean.FALSE);
  static public QuoteExp nullExp = new QuoteExp(null);

  public QuoteExp (Object val) { value = val; }
  public QuoteExp (Object val, Type type) 
  { 
    this(val);
    this.type = type;
  }
  
  public Object eval (Environment env)
  {
    return value;
  }

  public void compile (Compilation comp, Target target)
  {
    comp.compileConstant(value, target);
  }
 
  protected Expression walk (ExpWalker walker)
  {
    return walker.walkQuoteExp(this);
  }

  public void print (OutPort out)
  {
    out.startLogicalBlock("(Quote", ")", 2);
    out.writeSpaceLinear();
    gnu.lists.FormatToConsumer saveFormat = out.objectFormat;
    try
      {
	out.objectFormat = Interpreter.getInterpreter().getFormat(true);
	out.print(value);
      }
    finally
      {
	out.objectFormat = saveFormat;
      }
    out.endLogicalBlock(")");
  }
}
