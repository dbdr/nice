tcltest::test 3.10.5-runtime-1 { Compile-time constant string
        expressions, and String.intern(), are == String objects
        } {runtime} {
    saveas testPackage/T3105r1.java {
package testPackage;
  class T3105r1 {
      public static void main(String[] args) {   
        String hello = "Hello", lo = "lo";
        System.out.print((hello == "Hello") + " ");
        System.out.print((Other1.hello == hello) + " ");
        System.out.print((other.Other.hello == hello) + " ");
        System.out.print((hello == ("Hel"+"lo")) + " ");
        System.out.print((hello == ("Hel"+lo)) + " ");
        System.out.print(hello == ("Hel"+lo).intern());
      }
}
class Other1 { static String hello = "Hello"; }
    }

    saveas other/Other.java {
package other;
public class Other { public static String hello = "Hello"; }
    }

    compile_and_run other/Other.java testPackage/T3105r1.java
} {true true true true false true}


tcltest::test 3.10.5-runtime-2 { Strings that occupy more than 0xffff UTF8
        bytes cannot be constant, according to JVMS.  However, since the
        JLS does not require compile-time failure, we must also test for
        an invalid constant pool entry } {runtime} {
    compile_and_run [saveas T3105r2.java {
class T3105r2 {
    static final String s0001 = "a";
    static final String s0002 = s0001 + s0001;
    static final String s0004 = s0002 + s0002;
    static final String s0008 = s0004 + s0004;
    static final String s0010 = s0008 + s0008;
    static final String s0020 = s0010 + s0010;
    static final String s0040 = s0020 + s0020;
    static final String s0080 = s0040 + s0040;
    static final String s0100 = s0080 + s0080;
    static final String s0200 = s0100 + s0100;
    static final String s0400 = s0200 + s0200;
    static final String s0800 = s0400 + s0400;
    static final String s1000 = s0800 + s0800;
    static final String s2000 = s1000 + s1000;
    static final String s4000 = s2000 + s2000;
    static final String s8000 = s4000 + s4000;
    static final String sffff = s8000 + s4000 + s2000 + s1000
                              + s0800 + s0400 + s0200 + s0100
                              + s0080 + s0040 + s0020 + s0010
                              + s0008 + s0004 + s0002 + "b"; // still constant!

    static final String toobig = sffff + "c"; // can't be constant

    public static void main(String[] args) {
        if (toobig.equals(""))
            System.out.println("String was lost completely");
        else if (toobig.endsWith("b"))
            System.out.println("String was truncated at 0xffff bytes");
        else if (toobig.endsWith("abc")) {
            String alternate = sffff;
            alternate += "c";
            alternate = alternate.intern();
            if (alternate != toobig)
                System.out.println("String was not interned");
            else
                System.out.println("OK");
        } else
            System.out.println("Unexpected case");            
    }
}
    }]
} OK
