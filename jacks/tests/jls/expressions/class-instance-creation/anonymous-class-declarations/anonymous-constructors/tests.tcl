tcltest::test 15.9.5.1-implicit-1 { An anonymous class cannot have an
        explicit constructor } {
    empty_main T15951i1 {
        new Object() {
            Object() {}
        };
    }
} FAIL

tcltest::test 15.9.5.1-superconstructor-1 { An anonymous class is invalid if
        the superconstructor does not exist } {
    empty_main T15951s1 {
        new Object(1) {};
    }
} FAIL

tcltest::test 15.9.5.1-superconstructor-2 { An anonymous class is invalid if
        the superconstructor is not accessible } {
    compile [saveas T15951s2.java {
class T15951s2 {
    private T15951s2() {}
}
class Other2 {
    Object o = new T15951s2() {};
}
    }]
} FAIL

tcltest::test 15.9.5.1-superconstructor-3 { An anonymous class is invalid if
        the superconstructor is ambiguous } {
    empty_class T15951s3 {
        T15951s3(int i, byte b) {}
        T15951s3(byte b, int i) {}
        byte b;
        Object o = new T15951s3(b, b) {};
    }
} FAIL

tcltest::test 15.9.5.1-superconstructor-4 { Accessibility disambiguates
        the superconstructor of an anonymous class } {
    compile [saveas T15951s4.java {
class T15951s4 {
    private T15951s4(int i, byte b) {}
    T15951s4(byte b, int i) {}
}
class Other4 {
    byte b;
    Object o = new T15951s4(b, b) {};
}
    }]
} PASS

tcltest::test 15.9.5.1-superconstructor-5 { An anonymous class may access
        a private superconstructor within the same enclosing class } {
    empty_class T15951s5 {
        private T15951s5() {}
        Object o = new T15951s5() {};
    }
} PASS

tcltest::test 15.9.5.1-superconstructor-6 { An anonymous class may access
        the protected constructor of a class from another package } {
    saveas p1/T15951s6_1.java {
package p1;
public class T15951s6_1 {
    protected T15951s6_1() {}
}
    }
    saveas p2/T15951s6_2.java {
package p2;
import p1.T15951s6_1;
class T15951s6_2 {
    Object o = new T15951s6_1() {};
}
    }
    compile p1/T15951s6_1.java p2/T15951s6_2.java
} PASS

tcltest::test 15.9.5.1-exception-1 { An anonymous class constructor legally
        throws whatever exception the superconstructor does } {
    empty_class T15951e1 {
        T15951e1() throws InterruptedException {}
        void foo() {
            try {
                new T15951e1() {};
            } catch (InterruptedException e) {
            }
        }
    }
} PASS

tcltest::test 15.9.5.1-exception-2 { An anonymous class constructor legally
        throws whatever exception the superconstructor does } {
    empty_class T15951e2 {
        T15951e2() throws RuntimeException, Error {}
        void foo() {
            try {
                new T15951e2() {};
            } catch (RuntimeException re) {
            } catch (Error e) {
            }
        }
    }
} PASS

tcltest::test 15.9.5.1-exception-3 { An anonymous class constructor legally
        throws whatever exception an instance initializer throws } {
    empty_main T15951e3 {
        try {
            new Object() {
                int i = foo();
                int foo() throws InterruptedException { return 0; }
            };
        } catch (InterruptedException e) {
        }
    }
} PASS

tcltest::test 15.9.5.1-exception-4 { An anonymous class constructor legally
        throws whatever exception an instance initializer throws } {
    empty_main T15951e4 {
        try {
            new Object() {
                {
                    if (true) // initializer must be able to complete normally
                        throw new InterruptedException();
                }
            };
        } catch (InterruptedException e) {
        }
    }
} PASS

