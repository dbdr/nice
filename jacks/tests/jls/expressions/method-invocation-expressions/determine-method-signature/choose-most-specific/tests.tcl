tcltest::test 15.12.2.2-ambiguous-1 { More than one maximally specific method
        is ambiguous, and illegal.  Here, A.foo(String) and
        T151222a1.foo(Object) are both maximally specific, as the latter
        does not override the former. } {
    compile [saveas T151222a1.java {
interface A {
    void foo(String s);
}
abstract class T151222a1 implements A {
    void foo(Object o) {}
    {
	foo("");
    }
}
    }]
} FAIL

tcltest::test 15.12.2.2-ambiguous-2 { More than one maximally specific method
        is ambiguous, and illegal.  Here, A.foo(String) and
        B.foo(Object) are both maximally specific, as the latter
        does not override the former. } {
    compile [saveas T151222a2.java {
interface A {
    void foo(String s);
}
interface B {
    void foo(Object o);
}
abstract class T151222a2 implements A, B {
    {
	foo("");
    }
}
    }]
} FAIL

tcltest::test 15.12.2.2-ambiguous-3 { More than one maximally specific method
        is ambiguous, and illegal.  Here, A.foo(String) and
        T151222a3.foo(Object) are both maximally specific, as the latter
        does not override the former. } {
    compile [saveas T151222a3.java {
class A {
    void foo(String s) {}
}
abstract class T151222a3 extends A {
    void foo(Object o) {}
    {
	foo("");
    }
}
    }]
} FAIL

tcltest::test 15.12.2.2-ambiguous-4 { When multiple maximally specific methods
        have the same signature, any non-abstract version is chosen } {
    compile [saveas T151222a4a.java {
class T151222a4a {
    public void m() {}
}
interface T151222a4b {
    void m();
}
class T151222a4c extends T151222a4a implements T151222a4b {
    { m(); }
}
}]
} PASS

tcltest::test 15.12.2.2-ambiguous-5 { When multiple maximally specific methods
        have the same signature, and all are abstract, it is arbitrary which
        is chosen, although the throws clauses are merged } {
    compile [saveas T151222a4a.java {
abstract class T151222a5a {
    public abstract void m();
}
interface T151222a5b {
    void m();
}
abstract class T151222a5c extends T151222a5a implements T151222a5b {
    { m(); }
}
}]
} PASS

tcltest::test 15.12.2.2-ambiguous-6 { When multiple maximally specific methods
        have the same signature, and all are abstract, it is arbitrary which
        is chosen, although the throws clauses are merged } {
    compile [saveas T151222a6a.java {
class E1 extends Exception {}
class E2 extends Exception {}
class E3 extends Exception {}
abstract class T151222a6a {
    public abstract void m() throws E1, E2 {}
}
interface T151222a6b {
    void m() throws E2, E3;
}
abstract class T151222a6c extends T151222a6a implements T151222a6b {
    {
        try {
            m(); // whether a.m() or b.m() is chosen, it cannot throw E1 or E3
        } catch (E2 e2) {
        }
    }
}
}]
} PASS

tcltest::test 15.12.2.2-ambiguous-7 { Example where accessibility makes
        what would otherwise be ambiguous maximally specific methods with
        conflicting signatures have a single resolution } {
    compile [saveas p1/T151222a7a.java {
package p1;
public class T151222a7a {
    void m(Object o, String s) {}
}
}] [saveas T151222a7b.java {
interface T151222a7b {
    void m(String s, Object o);
}
abstract class T151222a7c extends p1.T151222a7a implements T151222a7b {
    { m("", ""); } // only b.m(String, Object) is accessible
}
}]
} PASS

tcltest::test 15.12.2.2-ambiguous-8 { Example where accessibility makes
        what would otherwise be ambiguous maximally specific methods with
        conflicting signatures have a single resolution } {
    compile [saveas p1/T151222a8a.java {
package p1;
public class T151222a8a {
    protected void m(Object o, String s) {}
}
}] [saveas T151222a8b.java {
interface T151222a8b {
    void m(String s, Object o);
}
abstract class T151222a8c extends p1.T151222a8a implements T151222a8b {}
abstract class T151222a8d extends T151222a8c {
    // Even though d inherits two versions of m, the protected a.m
    // is only accessible if the qualifying expression is type d or lower
    void foo(T151222a8c c) {
        c.m("", "");
    } // only b.m(String, Object) is accessible
}
}]
} PASS

tcltest::test 15.12.2.2-ambiguous-9 { Example where accessibility makes
        what would otherwise be ambiguous maximally specific methods with
        conflicting signatures have a single resolution } {
    compile [saveas p1/T151222a9a.java {
package p1;
public class T151222a9a {
    protected void m(Object o, String s) {}
}
}] [saveas T151222a9b.java {
interface T151222a9b {
    void m(String s, Object o);
}
abstract class T151222a9c extends p1.T151222a9a implements T151222a9b {}
abstract class T151222a9d extends T151222a9c {
    // Even though d inherits two versions of m, the protected a.m
    // is only accessible if the qualifying expression is type d or lower
    void foo(T151222a9d d) {
        d.m("", "");
    } // both a.m(Object, String) and b.m(String, Object) are accessible
}
}]
} FAIL

