package gnu.expr;

import gnu.bytecode.*;

/**
 * Pre/post incrementation of a local variable.
 * 
 * @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */

public class IncrementExp extends Expression
{
  public IncrementExp(Declaration decl, short increment, boolean prefix)
  {
    this.decl = decl;
    this.increment = increment;
    this.prefix = prefix;
  }
    
  Declaration decl;
  private short increment;
  private boolean prefix;
  
  Object walk(ExpWalker w)
  {
    return w.walkIncrementExp(this); 
  }
  
  public void compile(Compilation comp, Target target)
  {
    CodeAttr code = comp.getCode();
    boolean needValue = ! (target instanceof IgnoreTarget);
    
    if (decl.isSimple())
      {
	Variable var = decl.getVariable();    

	if (!prefix && needValue)
	  code.emitLoad(var);
	code.emitInc(var, increment);
	if (prefix && needValue)
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
	
	if (!prefix && needValue)
	  if (isStatic)
	    code.emitDup();
	  else
	    code.emitDupX();

	if(isLong)
	  code.emitPushLong(increment);
	else
	  code.emitPushInt(increment);
	code.emitAdd(type);

	if (prefix && needValue)
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

  public void print(java.io.PrintWriter pw)
  {
    pw.print("(#%increment (" + 
	     decl + ", " +
	     increment + ", " +
	     prefix +")");
  }
}
