tcltest::test 6.5.2-simple-1 { A simple ambiguous name is reclassified as
        an expression name if it appears in the scope of any variable } {
    empty_class T652s1 {
        T652s1 t, u;
        static class t {
            static int u;
        }
        void foo() { // t refers to the field, not the class, thus t.u
            t.u = null; // is an Object, not an int
        }
    }
} PASS

tcltest::test 6.5.2-simple-2 { A simple ambiguous name is reclassified as
        an expression name if it appears in the scope of any variable } {
    empty_class T652s2 {
        static class Super {
            Super t, u;
        }
        static class Sub extends Super {
            static class t {
                static int u;
            }
            void foo() { // t refers to the inherited field, not the locally
                t.u = null; // declared class, thus t.u is an Object
            }
        }
    }
} PASS

tcltest::test 6.5.2-simple-3 { A simple ambiguous name is reclassified as
        a type name if it appears in the scope of any type, but no variable } {
    compile [saveas p1/T652s3a.java {
package p1;
public class T652s3a {}
}] [saveas T652s3b.java {
    class T652s3b {
        static class p1 {
            static int T652s3a;
        }
        void foo() {
            // Even though p1.T652s3a is also a type, this is resolved to
            // the int T652s3b.p1.T652s3a.
            p1.T652s3a++;
        }
    }
}]
} PASS

tcltest::test 6.5.2-simple-4 { A simple ambiguous name is reclassified as
        a type name if it appears in the scope of any type, but no variable } {
    compile [saveas p1/T652s4a.java {
package p1;
public class T652s4a {}
}] [saveas p2/T652s4b.java {
package p2;
public class T652s4b {
    public static class p1 {
        public static int T652s4a;
    }
}
}] [saveas T652s4c.java {
    class T652s4c extends p2.T652s4b {
        void foo() {
            // Even though p1.T652s4a is also a type, this is resolved to
            // the inherited int T652s4b.p1.T652s4a.
            p1.T652s4a++;
        }
    }
}]
} PASS

tcltest::test 6.5.2-simple-5 { A simple ambiguous name is reclassified as
        a package name if it is not in the scope of a variable or type;
        an error will eventually occur if the package is not observable } {
    empty_class T652s5 {
        Object foo() { return null; }
        void bar() {
            foo.toString(); // there is no package foo
        }
    }
} FAIL

tcltest::test 6.5.2-simple-6 { A simple ambiguous name is reclassified as
        a package name if it is not in the scope of a variable or type;
        an error will eventually occur if the package is not observable } {
    empty_class T652s6 {
        T652s6 foo() { return null; }
        int i;
        void bar() {
            foo.i++; // there is no package foo
        }
    }
} FAIL

tcltest::test 6.5.2-qualified-1 { If the qualifier of an ambiguous name is
        reclassified as a package name, the full name is reclassified as
        a type name if the package exists and has a type by that name } {
    compile [saveas p1/T652q1.java {
package p1;
class T652q1 {
    static int i;
    void foo() {
        p1.T652q1.i++; // T652q1 is a type in package p1
    }
}
}]
} PASS

tcltest::test 6.5.2-qualified-2 { If the qualifier of an ambiguous name is
        reclassified as a package name, the full name is reclassified as
        a package name if the package exists but has no type by that name } {
    empty_class T652q2 {
        // java is a package, but does not contain a type lang, so
        // java.lang is classified as a PackageName
        String s = java.lang.String.valueOf(1);
    }
} PASS

tcltest::test 6.5.2-qualified-3 { If the qualifier of an ambiguous name is
        reclassified as a package name, the full name is reclassified as
        a package name if the package exists but has no type by that name;
        an error will eventually occur if the subpackage is not observable } {
    compile [saveas p1/T652q3.java {
package p1;
class T652q3 {
    // p1.foo is classified as a PackageName, but p1 has no subpackage foo
    Object o = p1.foo.bar();
}
}]
} FAIL

tcltest::test 6.5.2-qualified-4 { If the qualifier of an ambiguous name is
        reclassified as a package name but the package is not observable, the
        full name is reclassified as a package name and an error will
        eventually occur } {
    empty_class T652q4 {
        static T652q4 foo() { return null; }
        Object o;
        void bar() {
            // foo.o is classified as a PackageName, even though there is
            // no package foo (let alone foo.o)
            foo.o.toString();
        }
    }
} FAIL

tcltest::test 6.5.2-qualified-5 { If the qualifier of an ambiguous name is
        reclassified as a type name, the full name is reclassified as
        an expression name if the type contains a field by that name } {
    empty_class T652q5 {
        static T652q5 t, u;
        static class t {
            static int u;
        }
        void foo() { // t refers to the field, not the class, thus T652q5.t.u
            T652q5.t.u = null; // is an Object, not an int
        }
    }
} PASS

tcltest::test 6.5.2-qualified-6 { If the qualifier of an ambiguous name is
        reclassified as a type name, the full name is reclassified as
        an expression name if the type contains a field by that name } {
    empty_class T652q6 {
        static class Super {
            static Super t, u;
        }
        static class Sub extends Super {
            static class t {
                static int u;
            }
            void foo() { // t refers to the inherited field, not the locally
                Sub.t.u = null; // declared class, thus Sub.t.u is an Object
            }
        }
    }
} PASS

tcltest::test 6.5.2-qualified-7 { If the qualifier of an ambiguous name is
        reclassified as a type name, the full name is reclassified as
        a type name if there is a member type but no field in scope } {
    empty_class T652q7 {
        static class t {
            static int i;
        }
        int j = T652q7.t.i; // T652q7.t is a type name
    }
} PASS

tcltest::test 6.5.2-qualified-8 { If the qualifier of an ambiguous name is
        reclassified as a type name, the full name is reclassified as
        a type name if there is a member type but no field in scope } {
    empty_class T652q8 {
        static class Super {
            static class t {
                static int i;
            }
        }
        class Sub extends Super {
            int j = Sub.t.i; // Sub.t is a type name since class t is inherited
        }
    }
} PASS

tcltest::test 6.5.2-qualified-9 { If the qualifier of an ambiguous name is
        reclassified as a type name, it is an error if the type contains no
        field or member type of that name } {
    empty_class T652q9 {
        static T652q9 t() { return null; }
        // T652q9 has neither a field nor member class named t
        int i;
        int j = T652q9.t.i;
    }
} FAIL

tcltest::test 6.5.2-qualified-10 { If the qualifier of an ambiguous name is
        reclassified as an expression name, the full name is reclassified as
        an expression name it the type of the qualifier contains a field
        by that name } {
    empty_class T652q10 {
        T652q10 u;
        int i;
        void foo(T652q10 t) {
            t.u.i++; // u is a field of type T652q10, so t.u is an expression
        }
    }
} PASS

tcltest::test 6.5.2-qualified-11 { If the qualifier of an ambiguous name is
        reclassified as an expression name, the full name is reclassified as
        an expression name it the type of the qualifier contains a field
        by that name } {
    empty_class T652q11 {
        static class Super {
            Super sup;
        }
        static class Sub extends Super {
            void bar(Sub s) {
                // sup is an inherited field in type Sub, so s.sup is an
                s.sup.toString(); // expression name
            }
        }
    }
} PASS

tcltest::test 6.5.2-qualified-12 { If the qualifier of an ambiguous name is
        reclassified as an expression name, it is an error if the type of
        the qualifier does not contain a field by that name (in other words,
        an expression cannot qualify a member type) } {
    empty_class T652q12 {
        static class Member {
            static int i;
        }
        T652q12 t;
        // the type T652q12 does not contain a field named Member
        int i = t.Member.i;
    }
} FAIL

tcltest::test 6.5.2-example-1 { Example of ambiguous name resolution } {
    saveas org/rpgpoet/Music.java {
package org.rpgpoet;
import java.util.Random;
public interface Music { Random[] wizards = new Random[4]; }
}
    saveas org/dummy.txt "Here for cleanup purposes"
    saveas bazola/Gabriel.java {
package bazola;
class Gabriel {
    static int n = org.rpgpoet.Music.wizards.length;
    // org is classified as a package name
    // org.rpgpoet is classified as a package name
    // org.rpgpoet.Music is classified as a type name
    // org.rpgpoet.Music.wizards is classified as an expression name
}
}
    compile org/rpgpoet/Music.java bazola/Gabriel.java
} PASS
