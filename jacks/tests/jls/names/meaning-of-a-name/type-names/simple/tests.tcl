tcltest::test 6.5.5.1-nested-1 { A simple type name must occur in the scope
of a type by that name } {
    empty_class T6551n1 {
        NoSuchType t;
    }
} FAIL

tcltest::test 6.5.5.1-nested-2 { A simple type name favors a visible local
        class declaration over member types } {
    empty_class T6551n2 {
        interface C {}
        void foo() {
            class C {}
            // C refers to the local class, not member interface
            class Sub extends C {}
        }
    }
} PASS

tcltest::test 6.5.5.1-nested-3 { A simple type name favors a visible local
        class declaration over member types } {
    empty_main T6551n3 {
        class C { // local
            static final int i = 1;
        }
        new Object() {
            class C { // member
                static final int i = 2;
            }
            void foo(int j) {
                switch (j) {
                    case 0:
                    case 1:
                    // C refers to the member class, since the local is
                    // shadowed, hence C.i is 2
                    case C.i:
                }
            }
        };
    }
} PASS

tcltest::test 6.5.5.1-nested-4 { A simple type name favors a visible local
        class declaration over member types } {
    empty_class T6551n4 {
        static class Super {
            interface C {} // member
        }
        void foo() {
            class C {} // local
            new Super() {
                // C refers to the inherited member class, since the
                // local is shadowed, hence C is an interface
                class Sub implements C {}
            };
        }
    }
} PASS

tcltest::test 6.5.5.1-nested-5 { A simple type name favors a visible local
        class declaration over member types } {
    empty_main T6551n5 {
        class C {
            static final int i = 1;
        }
        new Object() {
            void foo(int j) {
                class C {
                    static final int i = 2;
                }
                switch (j) {
                    case 0:
                    case 1:
                    // C refers to the innermost local class, since it shadows
                    // the other version, hence C.i is 2
                    case C.i:
                }
            }
        };
    }
} PASS

tcltest::test 6.5.5.1-nested-6 { A simple type name must not have ambiguity in
       referring to visible member types } {
    empty_class T6551n6 {
        interface C {}
        void foo() {
            new Object() {
                class C {}
                // C refers to the innermost class C, not the outer interface
                class Sub extends C {}
            };
        }
    }
} PASS

tcltest::test 6.5.5.1-nested-7 { A simple type name must not have ambiguity in
       referring to visible member types } {
    empty_class T6551n7 {
        interface C {}
        static class Super {
            class C {}
        }
        void foo() {
            new Super() {
                // C refers to the inherited class Super.C, not the shadowed
                // interface T6551n7.C
                class Sub extends C {}
            };
        }
    }
} PASS

tcltest::test 6.5.5.1-nested-8 { A simple type name must not have ambiguity in
       referring to visible member types } {
    empty_class T6551n8 {
        interface I {
            class C {}
        }
        class Super {
            class C {}
        }
        class Sub extends Super implements I {
            C c; // C is ambiguous between I.C, Super.C
        }
    }
} FAIL

tcltest::test 6.5.5.1-nested-9 { A simple type name must not have ambiguity in
       referring to visible member types } {
    compile [saveas p1/T6551n9c.java {
package p1;
class T6551n9a {
    class C {}
}
interface T6551n9b {
    class C {}
}
public class T6551n9c extends T6551n9a implements T6551n9b {}
}] [saveas T6551n9d.java {
class T6551n9d extends p1.T6551n9c {
    C c; // T6551n9a.C is not accessible, so this is T6551n9b.C
}
}]
} PASS

tcltest::test 6.5.5.1-nested-10 { A simple type name favors a member type
        over top-level and single-type imports in the same compilation unit } {
    compile [saveas p1/T6551n10a.java {
package p1;
public interface T6551n10a {}
}] [saveas T6551n10b.java {
import p1.T6551n10a;
class T6551n10b {
    class T6551n10a {}
    // T6551n10a refers to the member class, not the imported interface
    class Sub extends T6551n10a {}
}
}]
} PASS

tcltest::test 6.5.5.1-nested-11 { A simple type name favors a member type
        over top-level and single-type imports in the same compilation unit } {
    compile [saveas T6551n11a.java {
interface T6551n11a {}
class T6551n11b {
    class T6551n11a {}
    // T6551n11a refers to the member class, not the top-level interface
    class Sub extends T6551n11a {}
}
}]
} PASS

tcltest::test 6.5.5.1-nested-12 { A simple type name favors a member type
        over top-level and single-type imports in the same compilation unit } {
    compile [saveas p1/T6551n12a.java {
package p1;
public class T6551n12a {
    public interface T6551n12b {}
}
}] [saveas T6551n12b.java {
class T6551n12b extends p1.T6551n12a implements p1.T6551n12a.T6551n12b {
    void foo() {
        // resolves to the inherited interface, not the current class
        new T6551n12b();
    }
}
}]
} FAIL

tcltest::test 6.5.5.1-import-1 { A simple type name favors a single-type
        import over a top-level type in the same package } {
    compile [saveas p1/T6551i1a.java {
package p1;
public interface T6551i1a {}
}] [saveas T6551i1a.java {
class T6551i1a {}
}] [saveas T6551i1b.java {
import p1.T6551i1a;
// T6551i1a refers to the imported interface, not the class in the same package
class T6551i1b implements T6551i1a {}
}]
} PASS

tcltest::test 6.5.5.1-import-2 { A simple type name favors a single-type
        import over import-on-demand } {
    compile [saveas p1/T6551i2a.java {
package p1;
public class T6551i2a {
    public static final int i = 2;
}
}] [saveas p2/T6551i2a.java {
package p2;
public class T6551i2a {
    public static final int i = 1;
}
}] [saveas T6551i2b.java {
import p1.T6551i2a;
import p2.*;
class T6551i2b {
    void foo(int j) {
        switch (j) {
            case 0:
            case 1:
            // T6551i2a refers to the specific p1 import, not the on-demand p2,
            // so T6551i2a.i resolves to 2
            case T6551i2a.i:
        }
    }
}
}]
} PASS

tcltest::test 6.5.5.1-import-3 { A simple type name favors a top-level type
        in the same package over import-on-demand } {
    compile [saveas p1/T6551i3a.java {
package p1;
public interface T6551i3a {}
}] [saveas T6551i3a.java {
class T6551i3a {}
}] [saveas T6551i3b.java {
import p1.*;
// T6551i3a refers to the class in the same package, not an imported interface
class T6551i3b extends T6551i3a {}
}]
} PASS

tcltest::test 6.5.5.1-import-4 { A simple type name may not resolve to
        ambiguous types from import-on-demand } {
    compile [saveas p1/T6551i4a.java {
package p1;
public class T6551i4a {}
}] [saveas p2/T6551i4a.java {
package p2;
public class T6551i4a {}
}] [saveas T6551i4b.java {
import p1.*;
import p2.*;
// T6551i4a is ambiguous between p1 and p2
class T6551i4b extends T6551i4a {}
}]
} FAIL

