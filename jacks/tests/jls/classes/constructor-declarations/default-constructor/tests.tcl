tcltest::test 8.8.7-default-1 { If a class
        contains no constructor declarations,
        then a default constructor that takes
        no parameters is automatically provided } {
    empty_class T887d1 {
Object o = new T887d1();
    }
} PASS

tcltest::test 8.8.7-default-2 { If a class
        contains no constructor declarations,
        then a default constructor that takes
        no parameters is automatically provided } {
    saveas T887d2.java {
class T887d2_super {}
class T887d2_subclass extends T887d2_super {}
class T887d2 {
    Object o = new T887d2_subclass();
}
    }

    compile T887d2.java
} PASS



tcltest::test 8.8.7-throws-1 { A default
        constructor has no throws clause } {
    saveas T887t1.java {
class T887t1_super {
    T887t1_super() throws Exception {
        throw new Exception();
    }
}
class T887t1_subclass extends T887t1_super {}
    }

    compile T887t1.java
} FAIL





# A compile-time error occurs if a default constructor
# is provided by the compiler but the superclass does
# not have an accessible constructor that takes no arguments.

tcltest::test 8.8.7-inaccessible-default-constructor-toplevel-1 {
        A private constructor is not accessible in a subclass } {
    saveas T887idct1.java {
class T887idct1_super {
    private T887idct1_super() {}
}

class T887idct1_subclass extends T887idct1_super {}
    }

    compile T887idct1.java
} FAIL


tcltest::test 8.8.7-inaccessible-default-constructor-toplevel-2 {
        A constructor with default accessibility in some other
        package is not accessible in a subclass } {

    saveas T887idct2_super.java {
package T887idct2_pkg;
public class T887idct2_super {
    T887idct2_super() {}
}
    }

    saveas T887idct2.java {
import T887idct2_pkg.T887idct2_super;
class T887idct2_subclass extends T887idct2_super {}
    }

    compile T887idct2_super.java T887idct2.java
} FAIL


# test the accessibility of private constructors by subclasses
# enclosed in the same top-level class
  # private constructor in top-level class
tcltest::test 8.8.7-accessible-default-constructor-inner-1 {
        A constructor with private accessibility can be accessed
        from a nested class defined in the same class } {
    empty_class T887adci1 {
        private T887adci1() {}
        static class T887adci1_nested extends T887adci1 {}
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-2 {
        A constructor with private accessibility can be accessed
        from an inner class defined inside the same class } {
    empty_class T887adci2 {
        private T887adci2() {}
        class T887adci2_inner extends T887adci2 {}
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-3 {
        A constructor with private accessibility can be accessed
        from an inner anonymous class defined inside the same class } {
    empty_class T887adci3 {
        private T887adci3() {}
        Object o = new T887adci3() {};
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-4 {
        A constructor with private accessibility can be accessed
        from a static anonymous class defined inside the same class } {
    empty_class T887adci4 {
        private T887adci4() {}
        static Object o = new T887adci4() {};
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-5 {
        A constructor with private accessibility can be accessed
        from an inner local class defined inside the same class } {
    empty_class T887adci5 {
        private T887adci5() {}
        {
            class T887adci5_inner_local extends T887adci5 {}
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-6 {
        A constructor with private accessibility can be accessed
        from a static local class defined inside the same class } {
    empty_class T887adci6 {
        private T887adci6() {}
        static {
            class T887adci6_static_local extends T887adci6 {}
        }
    }
} PASS

  # private constructor in static nested class
tcltest::test 8.8.7-accessible-default-constructor-inner-7 {
        A constructor with private accessibility can be accessed
        from a nested class defined in the same class } {
    empty_class T887adci7 {
        static class A {
            private A() {}
        }
        static class B extends A {}
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-8 {
        A constructor with private accessibility can be accessed
        from an inner class defined inside the same class } {
    empty_class T887adci8 {
        static class A {
            private A() {}
        }
        class B extends A {}
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-9 {
        A constructor with private accessibility can be accessed
        from an inner anonymous class defined inside the same class } {
    empty_class T887adci9 {
        static class A {
            private A() {}
        }
        Object o = new A() {};
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-10 {
        A constructor with private accessibility can be accessed
        from a static anonymous class defined inside the same class } {
    empty_class T887adci10 {
        static class A {
            private A() {}
        }
        static Object o = new A() {};
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-11 {
        A constructor with private accessibility can be accessed
        from an inner local class defined inside the same class } {
    empty_class T887adci11 {
        static class A {
            private A() {}
        }
        {
            class B extends A {}
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-12 {
        A constructor with private accessibility can be accessed
        from a static local class defined inside the same class } {
    empty_class T887adci12 {
        static class A {
            private A() {}
        }
        static {
            class B extends A {}
        }
    }
} PASS

  # private constructor in inner class
tcltest::test 8.8.7-accessible-default-constructor-inner-13 {
        A constructor with private accessibility can be accessed
        from a nested class defined in the same class. However,
        there is no enclosing class with respect to the superclass. } {
    empty_class T887adci13 {
        class A {
            private A() {}
        }
        static class B extends A {}
    }
} FAIL

tcltest::test 8.8.7-accessible-default-constructor-inner-14 {
        A constructor with private accessibility can be accessed
        from an inner class defined inside the same class } {
    empty_class T887adci14 {
        class A {
            private A() {}
        }
        class B extends A {}
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-15 {
        A constructor with private accessibility can be accessed
        from an inner anonymous class defined inside the same class } {
    empty_class T887adci15 {
        class A {
            private A() {}
        }
        Object o = new A() {};
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-16 {
        A constructor with private accessibility can be accessed
        from a static anonymous class defined inside the same class } {
    empty_class T887adci16 {
        class A {
            private A() {}
        }
        static Object o = new T887adci16().new A() {};
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-17 {
        A constructor with private accessibility can be accessed
        from an inner local class defined inside the same class } {
    empty_class T887adci17 {
        class A {
            private A() {}
        }
        {
            class B extends A {}
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-18 {
        A constructor with private accessibility can be accessed
        from a static local class defined inside the same class. However,
        there is no enclosing class with respect to the superclass. } {
    empty_class T887adci18 {
        class A {
            private A() {}
        }
        static {
            class B extends A {}
        }
    }
} FAIL

  # private constructor in static local class
tcltest::test 8.8.7-accessible-default-constructor-inner-19 {
        A constructor with private accessibility can be accessed
        from an inner class defined inside the same class } {
    empty_class T887adci19 {
        static {
            class A {
                private A() {}
                class B extends A {}
            }
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-20 {
        A constructor with private accessibility can be accessed
        from an inner anonymous class defined inside the same class } {
    empty_class T887adci20 {
        static {
            class A {
                private A() {}
                Object o = new A() {};
            }
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-21 {
        A constructor with private accessibility can be accessed
        from a static anonymous class defined inside the same class } {
    empty_class T887adci21 {
        static {
            class A {
                private A() {}
            }
            new A() {};
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-22 {
        A constructor with private accessibility can be accessed
        from an inner local class defined inside the same class } {
    empty_class T887adci22 {
        static {
            class A {
                private A() {}
                {
                    class B extends A {}
                }
            }
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-23 {
        A constructor with private accessibility can be accessed
        from a static local class defined inside the same class. } {
    empty_class T887adci23 {
        static {
            class A {
                private A() {}
            }
            class B extends A {}
        }
    }
} PASS

  # private constructor in inner local class
tcltest::test 8.8.7-accessible-default-constructor-inner-24 {
        A constructor with private accessibility can be accessed
        from an inner class defined inside the same class } {
    empty_class T887adci24 {
        {
            class A {
                private A() {}
                class B extends A {}
            }
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-25 {
        A constructor with private accessibility can be accessed
        from an inner anonymous class defined inside the same class } {
    empty_class T887adci25 {
        {
            class A {
                private A() {}
            }
            new A() {};
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-26 {
        A constructor with private accessibility can be accessed
        from a static anonymous class defined inside the same class.
        However, there is no enclosing class for the superclass.} {
    empty_class T887adci26 {
        T887adci26(Object o) {}
        {
            class A {
                private A() {}
            }
            class B extends T887adci26 {
                B() {
                    super(new A() {});
                }
            }
        }
    }
} FAIL

tcltest::test 8.8.7-accessible-default-constructor-inner-27 {
        A constructor with private accessibility can be accessed
        from an inner local class defined inside the same class } {
    empty_class T887adci27 {
        {
            class A {
                private A() {}
            }
            class B extends A {}
        }
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-28 {
        A constructor with private accessibility can be accessed
        from an inner class of an anonymous class } {
    empty_class T887adci28 {
        Object o = new Object() {
            class Inner {
                private Inner() {}
            }
        }.new Inner();
    }
} PASS

tcltest::test 8.8.7-accessible-default-constructor-inner-29 {
        A constructor with private accessibility can be accessed
        from an inner class of an anonymous class } {
    empty_class T887adci29 {
        Object o = new Object() {
            class Inner {
                private Inner() {}
            }
        }.new Inner(){};
    }
} PASS



# FIXME: add tests for default protection that depends on class protection
