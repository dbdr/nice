tcltest::test 15.12.3-runtime-mode-1 { The invocation mode of a non-static,
        non-private method is virtual } {runtime} {
    saveas p1/T15123rm1_1.java {
package p1;
public class T15123rm1_1 {
    protected void foo() { // this is overridden in T15123rm1_3
        System.out.print("1");
    }
}
    }
    saveas p2/T15123rm1_2.java {
package p2;
import p1.*;
public class T15123rm1_2 extends T15123rm1_1 {
    protected void call() {
        foo(); // must be virtual call
        System.out.print(' ');
        new Object() {
            { foo(); } // must be virtual call of enclosing instance
        };
    }
}
    }
    saveas p3/T15123rm1_3.java {
package p3;
import p2.*;
public class T15123rm1_3 extends T15123rm1_2 {
    public static void main(String[] args) {
        new T15123rm1_3().call();
    }
    protected void foo() { // will be called virtually
        System.out.print("3");
    }
}
    }
    compile_and_run -classpath . p1/T15123rm1_1.java p2/T15123rm1_2.java p3/T15123rm1_3.java
} {3 3}

