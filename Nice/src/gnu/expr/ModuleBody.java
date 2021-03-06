package gnu.expr;
import gnu.mapping.*;
import gnu.lists.*;

/**
 * Abstract class for the dummy top-level function of a module.
 *
 * This provides the functionality of gnu.mapping.ApplyMethodContainer,
 * but it is class rather than an interface (thus ModuleMethod can use
 * faster virtual method calls instead of slower interface calls).
 */

// NICE: Avoid being dependent of gnu.mapping.MethodProc.
// NICE: This is bad because this class is needed at runtime.
public class ModuleBody extends ProcedureN// implements Runnable
{
  /*
  public void apply (CallContext stack)
  {
  }

  /** For backwards compatibily.
   * Earlier versions of Kawa used this to initialize a module.
   * Allows run(Consumer) to call those methods. */
  /*
  public Object run (Environment env)
  {
    return Values.empty;
  }

  public void run ()
  {
    run (new VoidConsumer());
  }

  public void run(Consumer out)
  {
    CallContext ctx = CallContext.getInstance();
    // For backwards compatibility - see run(Environment).
    run(ctx.getEnvironment());
    Consumer save = ctx.consumer;
    try
      {
	ctx.consumer = out;
	ctx.values = Values.noArgs;
	ctx.proc = this;
	ctx.run();
      }
    finally
      {
	ctx.consumer = save;
      }
  }

  public Object apply0 () throws Throwable
  {
    CallContext ctx = CallContext.getInstance();
    ctx.values = Values.noArgs;
    ctx.proc = this;
    return applyV(ctx);
  }
  */

  private static boolean mainPrintValues;

  /** True if runAsMain should print values (in top-level expressions). */
  public static boolean getMainPrintValues()
  {
    return mainPrintValues;
  }

  public static void setMainPrintValues(boolean value)
  {
    mainPrintValues = value;
  }


  /** This is invoked by main when ModuleBody is compiled with --main. */
  /*
  public final void runAsMain (String[] args)
  {
    kawa.repl.setArgs(args, 0);
    gnu.text.WriterManager.instance.registerShutdownHook();
    try
      {
	CallContext ctx = CallContext.getInstance();
	ctx.values = Values.noArgs;
	ctx.proc = this;
	ClassMemberConstraint.defineAll(this, ctx.getEnvironment());
	if (getMainPrintValues())
	  {
	    OutPort out = OutPort.outDefault();
	    ctx.consumer = kawa.Shell.getOutputConsumer(out);
	    ctx.runUntilDone();
	    out.freshLine();
	  }
	else
	  {
	    ctx.consumer = new VoidConsumer();
	    ctx.runUntilDone();
	  }
	// Redundant if registerShutdownHook succeeded (e.g on JDK 1.3).
	gnu.mapping.OutPort.runCleanups();
	kawa.repl.exitDecrement();
      }
    catch (Throwable ex)
      {
	ex.printStackTrace();
	gnu.mapping.OutPort.runCleanups();
	System.exit(-1);
      }
  }
  */

  // just to implement this abstract method
  public Object applyN(Object[] o)
  {
    throw new Error("Not implemented");
  }

  /**
   * A subclass will typically override this like:
   * switch (method.selector) {
   *   case 3:  return function3();
   *   case 5:  return function5();
   *   default:  super.apply0(method);
   * }
   */

  public Object apply0(ModuleMethod method)
  {
    return applyN(method, ProcedureN.noArgs);
  }

  public Object apply1(ModuleMethod method, Object arg1)
  {
    Object[] args = new Object[1];
    args[0] = arg1;
    return applyN(method, args);
  }

  public Object apply2(ModuleMethod method, Object arg1, Object arg2)
  {
    Object[] args = new Object[2];
    args[0] = arg1;
    args[1] = arg2;
    return applyN(method, args);
  }

  public Object apply3(ModuleMethod method,
                       Object arg1, Object arg2, Object arg3)
  {
    Object[] args = new Object[3];
    args[0] = arg1;
    args[1] = arg2;
    args[2] = arg3;
    return applyN(method, args);
  }

  public Object apply4(ModuleMethod method,
                       Object arg1, Object arg2, Object arg3, Object arg4)
  {
    Object[] args = new Object[4];
    args[0] = arg1;
    args[1] = arg2;
    args[2] = arg3;
    args[3] = arg4;
    return applyN(method, args);
  }

  public Object applyN(ModuleMethod method, Object[] args)
  {
    int count = args.length;
    int num = method.numArgs();
    if (count >= (num & 0xFFF)
	&& (num < 0 || count <= (num >> 12)))
      {
        switch (count)
          {
          case 0:
            return apply0(method);
          case 1:
            return apply1(method, args[0]);
          case 2:
            return apply2(method, args[0], args[1]);
          case 3:
            return apply3(method, args[0], args[1], args[2]);
          case 4:
            return apply4(method, args[0], args[1], args[2], args[3]);
          }
      }
    throw new WrongArguments(method, count);
  }

}
