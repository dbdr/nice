tcltest::test 6.5.5.2-package-1 { A qualified type name must refer to a
        single accessible member type of the qualifying package, or an error
        occurs } {
    compile [saveas p1/T6552p1.java {
package p1;
class T6552p1 {
    p1.T6552p1 t;
}
}]
} PASS

tcltest::test 6.5.5.2-package-2 { A qualified type name must refer to a
        single accessible member type of the qualifying package, or an error
        occurs } {
    compile [saveas p1/T6552p2.java {
package p1;
class T6552p2 {
    p1.NoSuchType t;
}
}]
} FAIL

tcltest::test 6.5.5.2-package-3 { A qualified type name must refer to a
        single accessible member type of the qualifying package, or an error
        occurs } {
    compile [saveas p1/T6552p3a.java {
package p1;
class T6552p3a {}
}] [saveas T6552p3b.java {
// p1.T6552p3a is not accessible
class T6552p3b extends p1.T6552p3a {}
}]
} FAIL

tcltest::test 6.5.5.2-package-4 { A qualified type name must refer to a
        single accessible member type of the qualifying package, or an error
        occurs } {
    empty_class T6552p4 {
        // java.lang is not a type, but a package
        java.lang j;
    }
} FAIL

tcltest::test 6.5.5.2-type-1 { A qualified type name must refer to a single
        accessible member type of the qualifying type, or an error occurs } {
    empty_class T6552t1 {
        class Inner {}
        T6552t1.Inner t;
    }
} PASS

tcltest::test 6.5.5.2-type-2 { A qualified type name must refer to a single
        accessible member type of the qualifying type, or an error occurs } {
    empty_class T6552t2 {
        T6552t2.NoSuchClass t;
    }
} FAIL

tcltest::test 6.5.5.2-type-3 { A qualified type name must refer to a single
        accessible member type of the qualifying type, or an error occurs } {
    compile [saveas T6552t3a.java {
class T6552t3a {
    private class C {}
}
class T6552t3b extends T6552t3a {
    // T6552t3a.C is not accessible
    T6552t3a.C t;
}
}]
} FAIL

tcltest::test 6.5.5.2-type-4 { A qualified type name must refer to a single
        accessible member type of the qualifying type, or an error occurs } {
    compile [saveas T6552t4a.java {
class T6552t4a {
    class C {}
}
interface T6552t4b {
    class C {}
}
class T6552t4c extends T6552t4a implements T6552t4b {
    // T6552t4c.C is ambiguous between T6552t4a.C and T6552t4b.C
    T6552t4c.C t;
}
}]
} FAIL

tcltest::test 6.5.5.2-type-5 { A qualified type name must refer to a single
        accessible member type of the qualifying type, or an error occurs } {
    compile [saveas p1/T6552t5c.java {
package p1;
class T6552t5a {
    class C {}
}
interface T6552t5b {
    class C {}
}
public class T6552t5c extends T6552t5a implements T6552t5b {}
}] [saveas T6552t5d.java {
class T6552t5d extends p1.T6552t5c {
    // only the accessible T6552t5b.C is inherited
    T6552t5d.C t;
}
}]
} PASS

tcltest::test 6.5.5.2-example-1 { Example in the specs } {
    empty_class T6552e1 {
        public static void main(String[] args) {
            java.util.Date date
                = new java.util.Date(System.currentTimeMillis());
            // Just like Sun to deprecate their example!
            System.out.println(date/*.toLocaleString()*/);
        }
    }
} PASS
