package bossa.engine;

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

  final public static boolean debugK0 = getBoolean(true, "debug.K0");

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
  final public static boolean debugTower = getBoolean(false, "debug.tower");

  /**
   * debugHpj created by Gilles Malfreyt 1999/05/05 to find why we have
   * a difference in execution on "Tests\overloading\t4.jzz"
   * between (1) the one interpreted with "java"
   *     and (2) the one compiled with "hpj"
   * i.e.
   * between (1) java fr.ensmp.cma.jazz.Main -I ../../Src/lib -v t4.jzz
   *     and (2) ../../hpj/jazz -I ../../Src/lib -v t4.jzz
   * (1) says: "t4.jzz":11.14-11.14: ambiguous member access: may refer to C2.x or C1.x
   * (2) says: "t4.jzz":11.14-11.14: ambiguous member access: may refer to C1.x or C2.x
   * Finally, we think it may have been a problem of "ClassPath" during
   * compilation ("sj") .java->.class or during compilation ("hpj") .class->.exe.
   * Maybe a sensibility to the order for "HashTable" ?
   * Nevertheless, after return to the original ClassPath, the bug didn't
   * reproduced, so what ?...
   */
  final public static boolean debugHpj = getBoolean(false, "debug.hpj");
  
  /**
   * Created by Gilles Malfreyt, 1999/05/05.
   * Used to understand the functioning of "MethInfo".
   */
  final public static boolean debugGilMethInfo = getBoolean(false, "debug.gil.methInfo");

  /**
   * Created by Gilles Malfreyt, 1999/05/17.
   * A bug in an error message in ProgramParser.parseModule.
   */
  final public static boolean debugGilParseModule = getBoolean(false, "debug.gil.parseModule");
  
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

  public static void assert(boolean cond) {
    assert(cond, "");
  }
  public static void assert(boolean cond, Object message) {
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
