package gnu.expr;

import gnu.bytecode.*;

/**
 * Pre/post incrementation of a local variable.
 * 
 * @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */

public class IncrementExp extends Expression
{
  /**
     Increment variable <code>decl</code> by <code>increment<code>,
     and return its value.

     The generated code is optimised. 
     For instance, the returned value is not pushed if it is not used.

     @return the value of <code>decl</code> 
       after incrementation if <code>pre</code> is true (i.e. ++x);
       the old value if if <code>pre</code> is false (i.e. x++)
  */
  public IncrementExp(Declaration decl, short increment, boolean pre)
  {
    this.decl = decl;
    this.increment = increment;
    this.pre = pre;
  }
  
  Declaration decl;
  private short increment;
  private boolean pre;
  
  public void compile(Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    boolean needValue = ! (target instanceof IgnoreTarget);
    
    if (decl.isSimple())
      {
	Variable var = decl.getVariable();    

	if (!pre && needValue)
	  code.emitLoad(var);

	if (var.getType().promote() == Type.int_type)
	  code.emitInc(var, increment);
	else
	  // The variable has a non-int type, but we know it must be 
	  // convertible to int.
	  {
	    PrimType type = Type.int_type;

	    code.emitLoad(var);
	    StackTarget.intTarget.compileFromStack(comp, var.getType());
	    code.emitPushInt(increment);
	    code.emitAdd(type);
	    StackTarget.getInstance(var.getType()).compileFromStack(comp, type);
	    code.emitStore(var);
	  }

	if (pre && needValue)
	  code.emitLoad(var);
      }
    else
      {
	Field field = decl.field;
	boolean isStatic = field.getStaticFlag();

	if (isStatic)
	  code.emitGetStatic(field);
	else
	  {
	    decl.loadOwningObject(comp);
	    code.emitDup();
	    code.emitGetField(field);
	  }
	
	boolean isLong = field.getType().getSize() > 4;
	PrimType type = isLong ? Type.long_type : Type.int_type;
	
	if (!pre && needValue)
	  if (isStatic)
	    code.emitDup();
	  else
	    code.emitDupX();

	if(isLong)
	  code.emitPushLong(increment);
	else
	  code.emitPushInt(increment);
	code.emitAdd(type);

	if (pre && needValue)
	  if (isStatic)
	    code.emitDup();
	  else
	    code.emitDupX();
	
	if (isStatic)
	  code.emitPutStatic(field);
	else
	  code.emitPutField(field);
      }
    
    if (needValue)
      target.compileFromStack(comp, getType());
  }

  public Type getType()
  {
    return decl.getType();
  }

  protected Expression walk(ExpWalker w)
  {
    return w.walkIncrementExp(this); 
  }
  
  public void print(gnu.mapping.OutPort out)
  {
    out.startLogicalBlock("(Increment", ")", 2);
    if (decl == null)
      out.print("<null declaration>");
    else
      out.print(decl.getName());
    out.endLogicalBlock(")");
  }
}
