tcltest::test 8.1.4-object-1 { Object must not have an implements clause.
        The JLS doesn't list this, but compare 8.1.3 for extends clauses } {
    set result [compile -classpath T814o1 \
[saveas T814o1/java/lang/Object.java {
package java.lang;
interface I {}
public class Object implements I {
    public native boolean equals(Object o);
    public native int hashCode();
    public native String toString();
    protected native void finalize() throws Throwable;
    protected native Object clone() throws CloneNotSupportedException;
    public final native Class getClass();
    public final native void notify() throws IllegalMonitorStateException;
    public final native void notifyAll() throws IllegalMonitorStateException;
    public final native void wait()
        throws IllegalMonitorStateException, InterruptedException;
    public final native void wait(long ms)
        throws IllegalMonitorStateException, InterruptedException;
    public final native void wait(long ms, int ns)
        throws IllegalMonitorStateException, InterruptedException;
}
}]]
    saveas T814o1/java/dummy "need this for cleanup of directory we create"
    saveas T814o1/dummy "need this for cleanup of directory we create"
    return $result
} FAIL

tcltest::test 8.1.4-superinterface-1 { A superinterface must be accessible } {
    compile [saveas T814s1.java "class T814s1 implements NoSuchType {}"]
} FAIL

tcltest::test 8.1.4-superinterface-2 { A superinterface must be accessible } {
    compile [saveas p1/T814s2a.java {
interface T814s2a {}
}] [saveas T814s2b.java {
class T814s2b implements p1.T814s2a {}
}]
} FAIL

tcltest::test 8.1.4-superinterface-3 { A superinterface must be an
        interface } {
    compile [saveas T814s3a.java {
class T814s3a {}
class T814s3b implements T814s3a {}
}]
} FAIL

tcltest::test 8.1.4-duplicate-1 { A class may not have duplicate
        superinterfaces } {
    compile [saveas T814d1.java {
class T814d1 implements Cloneable, Cloneable {}
}]
} FAIL

tcltest::test 8.1.4-duplicate-2 { A class may not have duplicate
        superinterfaces } {
    compile [saveas T814d2.java {
class T814d2 implements java.lang.Cloneable, Cloneable {}
}]
} FAIL

tcltest::test 8.1.4-duplicate-3 { A class may not have duplicate
        superinterfaces } {
    compile [saveas T814d3a.java {
interface T814d3a {}
interface T814d3b extends T814d3a {}
class T814d3c implements T814d3a, T814d3b {}
}]
} PASS

tcltest::test 8.1.4-duplicate-4 { A class may not have duplicate
        superinterfaces } {
    compile [saveas T814d4a.java {
class T814d4a {
    interface I {}
}
class T814d4b extends T814d3a {}
class T814d4c implements T814d4a.I, T814d4b.I {}
}]
} FAIL

tcltest::test 8.1.4-abstract-1 { A class must implement, or inherit an
        implementation, of all methods of its superinterfaces, or be
        abstract } {
    compile [saveas T814a1a.java {
interface T814a1a {
    String toString();
}
class T814a1b implements T814a1a {}
}]
} PASS

tcltest::test 8.1.4-abstract-2 { A class must implement, or inherit an
        implementation, of all methods of its superinterfaces, or be
        abstract } {
    compile [saveas T814a2a.java {
interface T814a2a {
    String foo();
}
class T814a2b implements T814a2a {}
}]
} FAIL

tcltest::test 8.1.4-abstract-3 { A class must implement, or inherit an
        implementation, of all methods of its superinterfaces, or be
        abstract } {
    compile [saveas T814a3a.java {
interface T814a3a {
    String foo();
}
abstract class T814a3b implements T814a3a {}
}]
} PASS

tcltest::test 8.1.4-abstract-4 { A class must implement, or inherit an
        implementation, of all methods of its superinterfaces, or be
        abstract } {
    compile [saveas T814a4.java {
interface Fish {
    int getNumberOfScales();
}
interface Piano {
    int getNumberOfScales();
}
class T814a4 implements Fish, Piano {
    // You can tune a piano, but can you tuna fish?
    public int getNumberOfScales() { return 91; }
}
}]
} PASS

tcltest::test 8.1.4-conflict-1 { Multiple superinterfaces must not conflict } {
    compile [saveas T814c1.java {
interface Fish {
    int getNumberOfScales();
}
interface StringBass {
    double getNumberOfScales();
}
class T814c1 implements Fish, StringBass {
    public double getNumberOfScales() { return 91; }
}
}]
} FAIL

tcltest::test 8.1.4-conflict-2 { Multiple superinterfaces must not conflict } {
    compile [saveas T814c2a.java {
class E1 extends Exception {}
class E2 extends Exception {}
class E3 extends Exception {}
interface T814c2a { void foo() throws E1, E2; }
interface T814c2b { void foo() throws E2, E3; }
class T814c2c implements T814c2a, T814c2b {
    public void foo() throws E2 {}
}
}]
} PASS

tcltest::test 8.1.4-conflict-3 { Multiple superinterfaces must not conflict } {
    compile [saveas T814c3.java {
class E1 extends Exception {}
class E2 extends Exception {}
class E3 extends Exception {}
interface T814c3a { void foo() throws E1, E2; }
interface T814c3b { void foo() throws E2, E3; }
class T814c3c implements T814c3a, T814c3b {
    public void foo() throws E1, E3 {}
}
}]
} FAIL

