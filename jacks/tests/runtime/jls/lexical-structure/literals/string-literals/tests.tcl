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
