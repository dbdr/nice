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

# These next few tests are debatable, since the JLS claims interfaces do
# not have the protected methods clone() or finalize(), but the JVMS
# implements interfaces as true subtypes of Object.
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
        methods from Object. Yet, allowing a conflicting return type or
        throws clause would make an interface non-instantiable } {
    empty_class T92i8 "interface I { int clone(); }"
} FAIL

tcltest::test 9.2-implicit-9 { Interfaces do not implicitly have any protected
        methods from Object. Yet, allowing a conflicting return type or
        throws clause would make an interface non-instantiable } {
    empty_class T92i9 {
        interface I { Object clone() throws java.io.IOException; }
    }
} FAIL

tcltest::test 9.2-implicit-10 { Interfaces do not implicitly have any protected
        methods from Object. Yet, allowing a conflicting return type or
        throws clause would make an interface non-instantiable } {
    empty_class T92i10 "interface I { int finalize(); }"
} FAIL

