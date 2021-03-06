 package gnu.mapping;
//import gnu.math.*;
import gnu.lists.*;

/** A procedure activation stack (when compiled with explicit stacks). */

public class CallContext implements Runnable
    // extends ValueStack ??? FIXME
{
  public Procedure proc;

  /* CPS: ??
  CallFrame frame;
  */

  /** The program location in the current procedure. */
  public int pc;

  /** Default place for function results.
   * In the future, function arguments will also use vstack. */
  public ValueStack vstack = new ValueStack();  // ?? super
  /** Function results are written to this Consumer.
   * This may point to vstack - or some other Consumer. */
  public Consumer consumer = vstack;

  /** Used for passing parameters.  (Will be replaced by vstack.) */
  public Object value1;
  public Object value2;
  public Object value3;
  public Object value4;
  public Object[] values;
  public int ivalue1;
  public int ivalue2;

  /** Number of actual arguments. */
  public int count;
  
  /** Index of next argument.
   * This is used by methods like getNextArg, used by callees. */
  public int next;

  /** Encoding of where the arguments are.
   * Each argument uses 4 bits.
   * Arguments beyond 8 are implicitly ARG_IN_VALUES_ARRAY.
   */
  int where;
  final static int ARG_IN_VALUES_ARRAY = 0;
  final static int ARG_IN_VALUE1 = 1;
  final static int ARG_IN_VALUE2 = 2;
  final static int ARG_IN_VALUE3 = 3;
  final static int ARG_IN_VALUE4 = 4;
  final static int ARG_IN_IVALUE1 = 5;
  final static int ARG_IN_IVALUE2 = 6;

  public Object getArgAsObject(int i)
  {
    if (i < 8)
      {
        switch ((this.where >> (4 * i)) & 15)
          {
          case ARG_IN_VALUE1:  return value1;
          case ARG_IN_VALUE2:  return value2;
          case ARG_IN_VALUE3:  return value3;
          case ARG_IN_VALUE4:  return value4;
          //case ARG_IN_IVALUE1:  return IntNum.make(ivalue1);
          //case ARG_IN_IVALUE2:  return IntNum.make(ivalue2);
          }
      }
    return values[i];
  }

  /** Get the next incoming argument.
   * Throw WrongArguments if there are no more arguments.
   */
  public Object getNextArg()
  {
    if (next >= count)
      throw new WrongArguments(proc, count);
    return getArgAsObject(next++);
  }

  /** Get the next incoming argument.
   * Return defaultValue if there are no more arguments.
   */
  public Object getNextArg(Object defaultValue)
  {
    if (next >= count)
      return defaultValue;
    return getArgAsObject(next++);
  }

  /** Note that we are done with the input arguments.
   * Throw WrongArguments if there are unprocessed arguments.
   */
  public void lastArg()
  {
    if (next < count)
      throw new WrongArguments(proc, count);
    values = null;
  }

  public void setArgs()
  {
    count = 0;
    where = 0;
    next = 0;
  }

  public void setArgs(Object arg1)
  {
    value1 = arg1;
    count = 1;
    where = ARG_IN_VALUE1;
    next = 0;
  }

  public void setArgs(Object arg1, Object arg2)
  {
    value1 = arg1;
    value2 = arg2;
    count = 2;
    where = ARG_IN_VALUE1|(ARG_IN_VALUE2<<4);
    next = 0;
  }
  public void setArgs(Object arg1, Object arg2, Object arg3)
  {
    value1 = arg1;
    value2 = arg2;
    value3 = arg3;
    count = 3;
    where = ARG_IN_VALUE1|(ARG_IN_VALUE2<<4)|(ARG_IN_VALUE3<<8);
    next = 0;
  }

  public void setArgs(Object arg1, Object arg2, Object arg3, Object arg4)
  {
    value1 = arg1;
    value2 = arg2;
    value3 = arg3;
    value4 = arg4;
    count = 4;
    where = (ARG_IN_VALUE1|(ARG_IN_VALUE2<<4)
      |(ARG_IN_VALUE3<<8|ARG_IN_VALUE4<<12));
    next = 0;
  }

  public void setArgsN(Object[] args)
  {
    values = args;
    count = args.length;
    where = 0;
    next = 0;
  }

  public Object[] getArgs()
  {
    if (where == 0)
      return values;
    else
      {
	int i = count;
	Object[] args = new Object[i];
	while (--i >= 0)
	  args[i] = getArgAsObject(i);
	return args;
      }
  }

  public void runUntilDone()  throws Throwable
  {
    for (;;)
      {
	/** Cps
	CallFrame frame = this.frame;
	if (frame == null)
	  break;
	frame.step(this);
	*/
	Procedure proc = this.proc;
	if (proc == null)
	  break;
	this.proc = null;
	proc.apply(this);
      }
  }

  /** Run until no more continuations, returning final result. */
  public final Object runUntilValue() throws Throwable
  {
    Consumer consumerSave = consumer;
    ValueStack vst = vstack;
    consumer = vst;
    int dindexSave = vst.gapStart;
    int oindexSave = vst.oindex;
    try
      {
	runUntilDone();
	return Values.make(vst, dindexSave, vst.gapStart);
      }
    finally
      {
	consumer = consumerSave;
	vst.gapStart = dindexSave;
	vst.oindex = oindexSave;
      }
  }

  /** Run until no more continuations, sending result to a COnsumer. */
  public final void runUntilValue(Consumer out) throws Throwable
  {
    Consumer consumerSave = consumer;
    consumer = out;
    try
      {
	runUntilDone();
      }
    finally
      {
	consumer = consumerSave;
      }
  }

  public void run()
  {
    try
      {
	runUntilDone();
      }
    catch (RuntimeException ex)
      {
	throw ex;
      }
    catch (Error ex)
      {
	throw ex;
      }
    catch (Throwable ex)
      {
	throw new WrappedException(ex);
      }
  }

  /** Write values (of function result) to current consumer. */
  public void writeValue(Object value)
  {
    Values.writeValues(value, consumer);
  }
}
