package mlsub.typing.lowlevel;

import java.util.Properties;
import java.io.*;

/**
 * Repository of useful global variables and methods
 *
 * @version $Revision$, $Date$
 * @author Alexandre Frey
 **/
public class S {
  private S() {}

  final static boolean ALLOW_RC = false;
  
  final public static Properties props = new Properties();
  static {
    try {
      FileInputStream in =
        new FileInputStream(new File(System.getProperty("user.home"),
                                     ".jazzrc"));
      props.load(new BufferedInputStream(in));
      in.close();
    } catch (Exception e) {}
  }

  public static boolean getBoolean(boolean defaultValue, String name) {
    if (ALLOW_RC) {
      String value = props.getProperty(name);
      if (value == null) {
        return defaultValue;
      } else {
        return value.equals("true");
      }
    } else {
      return defaultValue;
    }
  }

  final public static boolean debug = getBoolean(false, "debug");

  final public static boolean debugK0 = getBoolean(false, "debug.K0");

  final public static boolean debugBlock = getBoolean(false, "debug.block");
  final public static boolean contextSimpl = getBoolean(true, "context.simplify");

  final public static boolean debugSimpl = getBoolean(false, "debug.simplify");
  final public static boolean trace = getBoolean(false, "trace");
  final public static boolean trace2 = getBoolean(false, "trace2");
  final public static boolean debugScope = getBoolean(false, "debug.scope");
  final public static boolean debugDepend = getBoolean(false, "debug.depend");

  final public static boolean simplifyBlock = getBoolean(true, "simplify.block");
  final public static boolean prettyPrint = getBoolean(false, "pretty.print");
  final public static boolean debugPrint = getBoolean(false, "debug.print");
  final public static boolean debugType = getBoolean(false, "debug.type");
  final public static boolean debugPoly = getBoolean(false, "debug.poly");

  final public static boolean traceDispatch = getBoolean(false, "trace.dispatch");
  final public static boolean traceDepth = getBoolean(false, "trace.depth");
  final public static boolean debugMeth = getBoolean(false, "debug.meth");
  final public static boolean debug1 = getBoolean(false, "debug.1");
  final public static boolean debug3 = getBoolean(false, "debug.3");
  final public static boolean debug2 = getBoolean(false, "debug.2");
  final public static boolean debugInterf = getBoolean(false, "debug.interf");
  final public static boolean allowOverloading = getBoolean(false, "allow.overloading");
  final public static boolean debugNative = getBoolean(false, "debug.native");
  final public static boolean debugBinders = getBoolean(false, "debug.binders");
  final public static boolean debugNullTypeArgs = getBoolean(false, "debug.nullTypeArgs");
  final public static boolean debugModule = getBoolean(false, "debug.module");
  
  final public static PrintWriter dbg = new PrintWriter(System.err, true);

  public static Error panic() {
    return panic("");
  }
  public static Error panic(String msg) {
    bossa.util.Internal.error("panic: " + msg);
    return null;
    
  }

  public static void printStack() {
    try {
      throw new Throwable();
    } catch (Throwable e) {
      e.printStackTrace(S.dbg);
    }
  }
  public static String address(Object x) {
    //    return (x == null ? "null" : Integer.toHexString(x.hashCode()));
    return (x == null ? "null" : Integer.toString(x.hashCode() % 10000));
  }

  public static void assume(boolean cond) {
    assume(cond, "");
  }
  public static void assume(boolean cond, Object message) {
    if (allowAssert && !cond) {
      S.panic("assertion failed: " + message);
    }
  }
  final private static boolean allowAssert = true;
  final public static boolean a = allowAssert;


  public final static int VERBOSE_MUTE = 0;
  public final static int VERBOSE_DEFAULT = 1;
  public final static int VERBOSE_DEVEL = 2;
  
  /**
   * verbose = 0: mute
   * verbose = 1: reasonable
   * verbose = 2: developer
   **/
  private static int verboseLevel = 1;
  static void setVerboseLevel(int verboseLevel) {
    S.verboseLevel = verboseLevel;
  }
  public static void verbosePrintln(int level, String msg) {
    if (level <= verboseLevel) {
      S.dbg.println(msg);
    }
  }
  public static void verbosePrint(int level, String msg) {
    if (level <= verboseLevel) {
      S.dbg.print(msg);
      S.dbg.flush();
    }
  }
}
