// Copyright (c) 1997  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.bytecode;
import java.io.*;

/** Access flags. */
/* When using JDK 1.1, replace this class by java.lang.reflec.Modifiers. */

public class Access {
  static public final short PUBLIC      = 0x0001;
  static public final short PRIVATE     = 0x0002;
  static public final short PROTECTED   = 0x0004;
  static public final short STATIC      = 0x0008;
  static public final short FINAL       = 0x0010;
  static public final short SUPER       = 0x0020;
  static public final short SYNCHRONIZED= 0x0020;
  static public final short VOLATILE    = 0x0040;
  static public final short TRANSIENT   = 0x0080;
  static public final short NATIVE      = 0x0100;
  static public final short INTERFACE   = 0x0200;
  static public final short ABSTRACT    = 0x0400;

  public static String toString(int flags)
  {
    StringBuffer buf = new StringBuffer();
    if ((flags & PUBLIC) != 0)      buf.append(" public");
    if ((flags & PRIVATE) != 0)     buf.append(" private");
    if ((flags & PROTECTED) != 0)   buf.append(" protected");
    if ((flags & STATIC) != 0)      buf.append(" static");
    if ((flags & FINAL) != 0)       buf.append(" final");
    if ((flags & SYNCHRONIZED) != 0)buf.append(" synchronized");
    if ((flags & VOLATILE) != 0)    buf.append(" volatile");
    if ((flags & TRANSIENT) != 0)   buf.append(" transient");
    if ((flags & NATIVE) != 0)      buf.append(" native");
    if ((flags & INTERFACE) != 0)   buf.append(" interface");
    if ((flags & ABSTRACT) != 0)    buf.append(" abstract");
    int unknown = flags & ~(PUBLIC|PRIVATE|PROTECTED|STATIC|FINAL
			    |SYNCHRONIZED|VOLATILE|NATIVE|INTERFACE|ABSTRACT);
    if (unknown != 0)
      {
	buf.append(" 0x");
	buf.append(Integer.toHexString(unknown));
      }
    return buf.toString();
  }

  /**
     @return true if code in class c can access method m, with the first
     argument of the call being receiver.

     receiver is null if the call is static.
  */
  public static boolean legal(ClassType c, Method m, Type receiver)
  {
    int mod = m.getModifiers();
    if ((mod & PUBLIC) != 0)
      return true;

    ClassType target = m.getDeclaringClass();

    if ((mod & PRIVATE) != 0)
      return c == target;

    // clone is the only method overriden for arrays, where it is public.
    // (JLS-2 10.7)
    if (receiver != null && receiver.isArray() && m.getName().equals("clone"))
      return true;

    // DEFAULT (PACKAGE) OR PROTECTED
    if (c.getPackageName().equals(target.getPackageName()))
      return true;

    // PROTECTED
    return (mod & PROTECTED) != 0 &&
      c.isSubclass(target) && receiver.isSubtype(c);
  }

  /***
   * Returns true if classType is legally accessible in the given package.
   */
  public static boolean legal(ClassType classType, String packageName)
  {
    int mod = classType.getModifiers();

    if ((mod & PUBLIC) != 0)
      return true;

    if ((mod & PRIVATE ) != 0)
      return false;

    if (! packageName.equals(classType.getPackageName()))
      return false;

    return true;
  }
}
