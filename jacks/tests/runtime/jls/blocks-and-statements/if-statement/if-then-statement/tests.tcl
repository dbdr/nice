tcltest::test 14.9.1-runtime-1 { Tests that compiler does not over-optimize,
        as reported in jikes bug 2982 } {runtime} {
    compile_and_run [saveas T1491r1.java {
import java.util.*;
class T1491r1 {
    static Set s = new HashSet();
    public static void main(String[] args) {
	if (null == s)
            System.out.print("oops");
	System.out.print(null == s ? "oops" : "OK");
    }
}
    }]
} OK

tcltest::test 14.9.1-runtime-2 { Tests that compiler does not over-optimize,
        as reported in jikes bug 2895 } {runtime} {
    compile_and_run [saveas T1491r2.java {
class T1491r2 {
    public static void main(String [] args) {
	foo();
	System.out.print("OK");
    }
    static void foo() {
	boolean a = false;
	if (a);
	else if (false);
    }
}
}]
} OK
