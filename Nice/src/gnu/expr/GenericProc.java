// Copyright (c) 1999  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.expr;
import gnu.mapping.*;
import gnu.bytecode.Type;

/** A collection of MethodProcs;  one is chosen at apply time. */

public class GenericProc extends MethodProc
{
  MethodProc[] methods;
  int count;
  int minArgs;
  int maxArgs;

  public int numArgs()
  {
    return minArgs | (maxArgs << 12);
  }

  public void add(MethodProc method)
  {
    if (methods == null)
      methods = new MethodProc[8];
    else if (count >= methods.length)
      {
        MethodProc[] copy = new MethodProc[2 * methods.length];
        System.arraycopy(methods, 0, copy, 0, count);
        methods = copy;
      }
    methods[count++] = method;

    int n = method.minArgs();
    if (n < minArgs)
      minArgs = n;
    n = method.maxArgs();
    if (n == -1 || n > maxArgs)
      maxArgs = n;
  }

  public Object applyN(Object[] args) throws Throwable
  {
    checkArgCount(this, args.length);
    MethodProc best = null;
    CallContext bestVars = null;
    CallContext vars = null;
    for (int i = count;  --i >= 0; )
      {
        MethodProc method = methods[i];
        if (vars == null)
          vars = new CallContext();
        if (method.match(vars, args) == 0)
          {
            if (best == null)
              {
                best = method;
                bestVars = vars;
                vars = null;
              }
            else
              {
                best = MethodProc.mostSpecific(best, method);
                if (best == method)
                  {
                    bestVars = vars;
                    vars = null;
                  }
              }
            
          }
      }
    if (best == null)
      throw new WrongType(this, WrongType.ARG_UNKNOWN, null);
    return best.applyV(bestVars);
  }

  public int isApplicable(Type[] args)
  {
    int best = -1;
    for (int i = count;  --i >= 0; )
      {
        MethodProc method = methods[i];
        int result = method.isApplicable(args);
        if (result == 1)
          return 1;
        if (result == 0)
          best = 0;
      }
    return best;
  }

  public int match (CallContext ctx, Object[] args)
  {
    if (count == 1)
      return methods[0].match(ctx, args);
    MethodProc best = null;
    CallContext vars = null;
    CallContext bestVars = null;
    for (int i = count;  --i >= 0; )
      {
        MethodProc method = methods[i];
	if (vars == null)
          vars = new CallContext();
	int code = method.match(vars, args);
	if (code == 0)
          {
            if (best == null)
              {
                best = method;
                bestVars = vars;
                vars = null;
              }
            else
              {
                best = MethodProc.mostSpecific(best, method);
                if (best == method)
                  {
                    bestVars = vars;
                    vars = null;
                  }
              }
            
          }
      }
    if (best != null)
      {
	ctx.value1 = best;
	ctx.value2 = bestVars;
	return 0;
      }
    return NO_MATCH;
  }

  public Object applyV(CallContext ctx) throws Throwable
  {
    return ((MethodProc) ctx.value1).applyV((CallContext) ctx.value2);
  }

  /** Create a GenericProc from one or more methods, plus properties. */
  public static GenericProc make (Object[] args)
  {
    int alen = args.length;
    int mlen = 0;
    GenericProc result = new GenericProc();
    for (int i = 0;  i < alen;  i++)
      {
	Object arg = args[i];
	if (arg instanceof Keyword)
	  {
	    String name = ((Keyword) arg).getName();
	    Object value = args[++i];
	    if (name == "name")
	      result.setName(value.toString());
	    else if (name == "method")
	      result.add((MethodProc) value);
	    else
	      result.setProperty(name, value);
	  }
	else
	  result.add((MethodProc) arg);
      }
    return result;
  }
}
