tcltest::test 6.5.6.2-package-1 { A qualified expression name may not be
        qualified by a package name } {
    empty_class T6562p1 {
        T6562p1 foo() { return null; }
        int i;
        int j = foo.i; // foo was classified as a (non-existant) package name
    }
} FAIL

tcltest::test 6.5.6.2-package-2 { A qualified expression name may not be
        qualified by a package name } {
    empty_class T6562p2 {
        int j = java.i; // java was classified as a package name
    }
} FAIL

tcltest::test 6.5.6.2-type-1 { A qualified expression name which is qualified
        by a type name must name a single accessible field of that type, which
        must be static } {
    empty_class T6562t1 {
        int i = T6562t1.no_such_field;
    }
} FAIL

tcltest::test 6.5.6.2-type-2 { A qualified expression name which is qualified
        by a type name must name a single accessible field of that type, which
        must be static } {
    empty_class T6562t2 {
        int i;
        int j = T6562t2.i; // i is not static
    }
} FAIL

tcltest::test 6.5.6.2-type-3 { A qualified expression name which is qualified
        by a type name must name a single accessible field of that type, which
        must be static } {
    empty_class T6562t3 {
        static int i;
        int j = T6562t3.i;
    }
} PASS

tcltest::test 6.5.6.2-type-4 { A qualified expression name which is qualified
        by a type name must name a single accessible field of that type, which
        must be static } {
    compile [saveas T6562t4.java {
interface T6562t4 {
    int i = 1;
    int j = T6562t4.i;
}
}]
} PASS

tcltest::test 6.5.6.2-type-5 { A qualified expression name which is qualified
        by a type name must name a single accessible field of that type, which
        must be static } {
    empty_class T6562t5 {
        class C {
            int i;
        }
        interface I {
            int i = 1;
        }
        class Sub extends C implements I {}
        // ambiguous between C.i and I.i, even though only I.i is static
        int j = Sub.i;
    }
} FAIL

tcltest::test 6.5.6.2-type-6 { A qualified expression name which is qualified
        by a type name must name a single accessible field of that type, which
        must be static } {
    compile [saveas p1/T6562t6c.java {
package p1;
class T6562t6a {
    int i;
}
interface T6562t6b {
    int i = 1;
}
public class T6562t6c extends T6562t6a implements T6562t6b {}
}] [saveas T6562t6d.java {
class T6562t6d {
    int i = p1.T6562t6c.i; // only T6562t6b.i is accessible
}
}]
} PASS

tcltest::test 6.5.6.2-type-7 { A qualified expression name which is qualified
        by a type name must name a single accessible field of that type, which
        must be static - this tests jikes bug 3044 } {
    compile [saveas T6562t7a.java {
interface T6562t7a {
    int i = T6562t7b.i;
}
    }] [saveas T6562t7b.java {
class T6562t7b {
    final static int i;
    static { i = 1; }
}
    }]
} PASS

tcltest::test 6.5.6.2-expression-1 { A qualified expression name which is
        qualified by an expression name must name a single accessible field
        of the type of the qualifier, which must be a reference } {
    empty_class T6562e1 {
        int i;
        int j = i .i; // a primitive type has no members
    }
} FAIL

tcltest::test 6.5.6.2-expression-2 { A qualified expression name which is
        qualified by an expression name must name a single accessible field
        of the type of the qualifier, which must be a reference } {
    empty_class T6562e2 {
        T6562e2 t;
        int i = t.no_such_field;
    }
} FAIL

tcltest::test 6.5.6.2-expression-3 { A qualified expression name which is
        qualified by an expression name must name a single accessible field
        of the type of the qualifier, which must be a reference } {
    empty_class T6562e3 {
        T6562e3 t;
        int i;
        int j = t.i;
    }
} PASS

tcltest::test 6.5.6.2-expression-4 { A qualified expression name which is
        qualified by an expression name must name a single accessible field
        of the type of the qualifier, which must be a reference } {
    compile [saveas T6562e4.java {
interface T6562e4 {
    T6562e4 t = null;
    int i = 1;
    int j = t.i;
}
}]
} PASS

tcltest::test 6.5.6.2-expression-5 { A qualified expression name which is
        qualified by an expression name must name a single accessible field
        of the type of the qualifier, which must be a reference } {
    empty_class T6562e5 {
        class C {
            int i;
        }
        interface I {
            int i = 1;
        }
        class Sub extends C implements I {}
        // ambiguous between C.i and I.i
        Sub s;
        int j = s.i;
    }
} FAIL

tcltest::test 6.5.6.2-expression-6 { A qualified expression name which is
        qualified by an expression name must name a single accessible field
        of the type of the qualifier, which must be a reference } {
    compile [saveas p1/T6562e6c.java {
package p1;
class T6562e6a {
    int i;
}
interface T6562e6b {
    int i = 1;
}
public class T6562e6c extends T6562e6a implements T6562e6b {}
}] [saveas T6562e6d.java {
class T6562e6d {
    p1.T6562e6c t;
    int i = t.i; // only T6562e6b.i is accessible
}
}]
} PASS

tcltest::test 6.5.6.2-expression-7 { A qualified expression name which is
        qualified by an expression name must name a single accessible field
        of the type of the qualifier, which must be a reference } {
    empty_class T6562e7 {
        Object o[] = {};
        int i = o.length;
    }
} PASS

tcltest::test 6.5.6.2-expression-8 { A qualified expression name may occur
        in a static context, even when it references an instance field } {
    empty_class T6562e8 {
        int i;
        static T6562e8 t;
        static {
            t.i++;
        }
    }
} PASS
