tcltest::test 8.1.3-circular-1 { Circular dependencies are illegal } {
    compile [saveas T813c1.java {
class T813c1 implements T813c1.Inner {
    interface Inner {}
}
    }]
} FAIL

tcltest::test 8.1.3-circular-2 { Circular dependencies are illegal } {
    compile [saveas T813c2.java {
class T813c2 extends T813c2.Inner {
    class Inner {}
}
    }]
} FAIL

tcltest::test 8.1.3-circular-3 { Circular dependencies are illegal } {
    compile [saveas T813c3.java {
class T813c3 extends T813c3 {}
    }]
} FAIL

tcltest::test 8.1.3-circular-4 { Circular dependencies are illegal } {
    compile [saveas T813c4.java {
class T813c4_1 extends T813c4_2 {}
class T813c4_2 extends T813c4_1 {}
    }]
} FAIL

tcltest::test 8.1.3-circular-5 { Circular dependencies are illegal } {
    compile [saveas T813c5.java {
class T813c5_1 implements T813c5_2.Inner2 {
    interface Inner1 {}
}
class T813c5_2 {
    interface Inner2 extends T813c5_1.Inner1 {}
}
    }]
} FAIL

tcltest::test 8.1.3-circular-6 { Circular dependencies are illegal } {
    empty_class T813c6 {
        void foo() {
            class Local extends Local {}
        }
    }
} FAIL

tcltest::test 8.1.3-non-circular-1 { In spite of cross-referencing, this
        declaration is not circular, but is legal } {
    compile [saveas T813nc1.java {
class T813nc1 {
    static class Inner1 extends Two.Inner2 {}
}
class Two extends T813nc1 {
    static class Inner2 {}
}
    }]
} PASS

tcltest::test 8.1.3-object-1 { Object must not have an extends clause } {
    set result [compile -classpath T813o1 \
[saveas T813o1/java/lang/Object.java {
package java.lang;
public class Object extends Object {
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
    saveas T813o1/java/dummy "need this for cleanup of directory we create"
    saveas T813o1/dummy "need this for cleanup of directory we create"
    return $result
} FAIL

tcltest::test 8.1.3-object-2 { Object must not have an extends clause } {
    set result [compile -classpath T813o2 \
[saveas T813o2/java/lang/Object.java {
package java.lang;
class C {}
public class Object extends C {
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
    saveas T813o2/java/dummy "need this for cleanup of directory we create"
    saveas T813o2/dummy "need this for cleanup of directory we create"
    return $result
} FAIL

tcltest::test 8.1.3-object-3 { Object must not have an extends clause } {
    set result [compile -classpath T813o3 \
[saveas T813o3/java/lang/Object.java {
package java.lang;
public class Object {
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
    saveas T813o3/java/dummy "need this for cleanup of directory we create"
    saveas T813o3/dummy "need this for cleanup of directory we create"
    return $result
} PASS

tcltest::test 8.1.3-object-4 { A class with no extends clause implicitly
        extends java.lang.Object, even if it is shadowed } {
    compile [saveas T813o4.java "class Object {}"]
} PASS

tcltest::test 8.1.3-superclass-1 { A superclass must be accessible } {
    compile [saveas T813s1.java "class T813s1 extends NoSuchClass {}"]
} FAIL

tcltest::test 8.1.3-superclass-2 { A superclass must be accessible } {
    compile [saveas p1/T813s2a.java "class T813s2a {}"] [saveas T813s2b.java {
        class T813s2b extends p1.T813s2a {}
    }]
} FAIL

tcltest::test 8.1.3-superclass-3 { A superclass must be a class } {
    compile [saveas T813s3a.java {
interface T813s3a {}
class T813s3b extends T813s3a {}
}]
} FAIL

tcltest::test 8.1.3-superclass-4 { A superclass must be non-final } {
    compile [saveas T813s4a.java {
final class T813s4a {}
class T813s4b extends T813s4a {}
}]
} FAIL

