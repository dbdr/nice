tcltest::test 15.11.1-invalid-1 { Member types are not fields } {
    empty_class T15111i1 {
        static class A {
            static class B {
                static int i = 1;
            }
        }
        int j = new A().B.i;
    }
} FAIL

tcltest::test 15.11.1-ambiguous-1 { Accessing ambiguous fields is an error } {
    compile [saveas T1511a1a.java {
interface T1511a1a {
    int i = 1;
}
class T1511a1b {
    int i = 2;
}
class T1511a1c extends T1511a1b implements T1511a1a {
    static int j = new T1511a1c().i;
}
}]
} FAIL

tcltest::test 15.11.1-ambiguous-2 { There is no ambiguity if one field
        is not accessible } {
    compile [saveas p1/T1511a2a.java {
package p1;
public class T1511a2a {
    int i = 1;
}
}] [saveas T1511a2b.java {
interface T1511a2b {
    int i = 2;
}
class T1511a2c extends p1.T1511a2a implements T1511a2b {
    static int j = new T1511a2c().i; // only b.i is accessible
}
}]
} PASS

tcltest::test 15.11.1-ambiguous-3 { There is no ambiguity if one field
        is not accessible } {
    compile [saveas p1/T1511a3a.java {
package p1;
public class T1511a3a {
    protected int i = 1;
}
}] [saveas T1511a3b.java {
interface T1511a3b {
    int i = 2;
}
class T1511a3c extends p1.T1511a3a implements T1511a3b {}
class T1511a3d extends T1511a3c {
    // Even though d inherits two versions of i, the protected a.i
    // is only accessible if the qualifying expression is type d or lower
    static int j = new T1511a3c().i; // only b.i is accessible
}
}]
} PASS

tcltest::test 15.11.1-ambiguous-4 { There is no ambiguity if one field
        is not accessible } {
    compile [saveas p1/T1511a4a.java {
package p1;
public class T1511a4a {
    protected int i = 1;
}
}] [saveas T1511a4b.java {
interface T1511a4b {
    int i = 2;
}
class T1511a4c extends p1.T1511a4a implements T1511a4b {}
class T1511a4d extends T1511a4c {
    // Even though d inherits two versions of i, the protected a.i
    // is only accessible if the qualifying expression is type d or lower
    static int j = new T1511a4d().i; // both a.i and b.i are accessible
}
}]
} FAIL

