tcltest::test 9.2-implicit-1 { Interfaces implicitly have all public method
        members of Object } {
    empty_class T92i1 {
        interface I {}
        void foo(I i) {
            i.getClass();
            i.toString();
            i.equals(null);
            i.hashCode();
            try {
                i.wait();
                i.wait(1);
                i.wait(1, 0);
                i.notifyAll();
                i.notify();
            } catch (Throwable t) {
            }
        }
    }
} PASS

tcltest::test 9.2-implicit-2 { It is an error for an interface method to
        conflict with a public one from Object } {
    empty_class T92i2 "interface I { int toString(); }"
} FAIL

tcltest::test 9.2-implicit-3 { It is an error for an interface method to
        conflict with a public one from Object } {
    empty_class T92i3 "interface I { String toString() throws Exception; }"
} FAIL

tcltest::test 9.2-implicit-4 { It is an error for an interface method to
        conflict with a public one from Object } {
    empty_class T92i4 "interface I { Class getClass(); }"
} FAIL

tcltest::test 9.2-implicit-5 { An interface may redeclare a non-final method
        of Object } {
    empty_class T92i5 "interface I { String toString() throws Error; }"
} PASS

# These next few tests are somewhat debatable going by the JLS alone, since
# the JVMS implements interfaces as true subtypes of Object. But Sun has
# issued further clarifications, see jikes bug 2878 for details.
tcltest::test 9.2-implicit-6 { Interfaces do not implicitly have any protected
        methods from Object. Javac compiles this, but the resulting class
        causes a VerifyError, so it should not have compiled. } {
    compile [saveas T92i6.java {
interface T92i6 extends Cloneable {
    class Inner {
        Object bar(T92i6 i) {
            try {
                // Because this call is nested in the interface, it would have
                // full access to protected i.clone() if that existed.
                return i.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }
}
}]
} FAIL

tcltest::test 9.2-implicit-7 { Interfaces do not implicitly have any protected
        methods from Object. Javac compiles this, but the resulting class
        causes a VerifyError, so it should not have compiled. } {
    compile [saveas T92i7.java {
interface T92i7 extends Cloneable {
    class Inner {
        void bar(T92i7 i) {
            try {
                // Because this call is nested in the interface, it would have
                // full access to protected i.finalize() if that existed.
                i.finalize();
            } catch (Throwable t) {
            }
        }
    }
}
}]
} FAIL

tcltest::test 9.2-implicit-8 { Interfaces do not implicitly have any protected
        methods from Object. Thus, this declaration is legal (although the
        interface is unimplementable) - see Jikes bug 2878 } {
    ok_pass_or_warn [empty_class T92i8 "interface I { int clone(); }"]
} OK

tcltest::test 9.2-implicit-9 { Interfaces do not implicitly have any protected
        methods from Object. Thus, this declaration is legal - see Jikes
        bug 2878 } {
    ok_pass_or_warn [empty_class T92i9 {
        interface I { Object clone() throws java.io.IOException; }
    }]
} OK

tcltest::test 9.2-implicit-10 { Interfaces do not implicitly have any protected
        methods from Object. Thus, this declaration is legal (although the
        interface is unimplementable) - see Jikes bug 2878 } {
    ok_pass_or_warn [empty_class T92i10 "interface I { int finalize(); }"]
} OK

tcltest::test 9.2-implicit-11 { Proof that the interfaces of tests 8 and 10
        are unimplementable, although the interface itself is legal - see
        Jikes bug 2878} {
    empty_class T92i11 {
        interface I { int clone(); }
        abstract class C implements I {}
    }
} FAIL

tcltest::test 9.2-implicit-12 { Proof that the interfaces of tests 8 and 10
        are unimplementable, although the interface itself is legal - see
        Jikes bug 2878} {
    empty_class T92i12 {
        interface I { int finalize(); }
        abstract class C implements I {}
    }
} FAIL

tcltest::test 9.2-implicit-13 { Although an interface may extend the throws
        clause for clone(), implementing classes can only throw the common
        subset of checked exceptions as compared to Object.clone } {
    ok_pass_or_warn [empty_class T92i13 {
        interface I { Object clone() throws java.io.IOException; }
        class C implements I {
            public Object clone() { return null; }
        }
    }]
} OK

tcltest::test 9.2-implicit-14 { Although an interface may extend the throws
        clause for clone(), implementing classes can only throw the common
        subset of checked exceptions as compared to Object.clone } {
    empty_class T92i14 {
        interface I { Object clone() throws java.io.IOException; }
        class C implements I {
            public Object clone() throws CloneNotSupportedException
            { return null; }
        }
    }
} FAIL

tcltest::test 9.2-implicit-15 { Although an interface may extend the throws
        clause for clone(), implementing classes can only throw the common
        subset of checked exceptions as compared to Object.clone } {
    empty_class T92i15 {
        interface I { Object clone() throws java.io.IOException; }
        class C implements I {
            public Object clone() throws java.io.IOException
            { return null; }
        }
    }
} FAIL

tcltest::test 9.2-implicit-16 { Although an interface may extend the throws
        clause for clone(), implementing classes can only throw the common
        subset of checked exceptions as compared to Object.clone } {
    ok_pass_or_warn [empty_class T92i16 {
        interface I { Object clone() throws java.io.IOException; }
        abstract class C implements I {
            public abstract Object clone();
        }
        void m(C c) {
            c.clone(); // cannot throw a checked exception
        }
    }]
} OK

tcltest::test 9.2-implicit-17 { The version of clone() inherited from Object
        does not satisfy clone() specified in an interface } {
    empty_class T92i17 {
        interface I { Object clone() throws java.io.IOException; }
        abstract class C implements I {}
    }
} FAIL

tcltest::test 9.2-implicit-18 { The version of clone() inherited from Object
        does not satisfy clone() specified in an interface } {
    empty_class T92i18 {
        interface I { Object clone() throws CloneNotSupportedException; }
        abstract class C implements I {}
    }
} FAIL

tcltest::test 9.2-implicit-19 { The version of finalize() inherited from Object
        does not satisfy finalize() specified in an interface } {
    empty_class T92i19 {
        interface I { void finalize() throws Throwable; }
        abstract class C implements I {}
    }
} FAIL
