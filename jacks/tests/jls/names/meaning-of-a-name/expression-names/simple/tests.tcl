tcltest::test 6.5.6.1-local-1 { If a simple expression name appears in the
        scope of a local variable or parameter, it denotes that variable } {
    empty_class T6561l1 {
        int i;
        void foo(Object i) {
            i = null; // refers to the parameter, not field
        }
    }
} PASS

tcltest::test 6.5.6.1-local-2 { If a simple expression name appears in the
        scope of a local variable or parameter, it denotes that variable } {
    empty_main T6561l2 {
        final int i = 1;
        new Object() {
            void foo() {
                Object i;
                i = null; // refers to Object i, which shadows int i
            }
        };
    }
} PASS

tcltest::test 6.5.6.1-local-3 { If a simple expression name appears in the
        scope of a local variable or parameter, it denotes that variable } {
    empty_main T6561l3 {
        final int i = 1;
        new Object() {
            Object i;
            void foo() {
                i = null; // the field Object i shadows the local int i
            }
        };
    }
} PASS

tcltest::test 6.5.6.1-local-4 { If a simple expression name appears in the
        scope of a local variable or parameter, it denotes that variable } {
    empty_class T6561l4 {
        class Super {
            Object i;
        }
        void foo(final int i) {
            new Super() {
                void foo() {
                    // the inherited field Object i shadows the parameter int i
                    i = null;
                }
            };
        }
    }
} PASS

tcltest::test 6.5.6.1-field-1 { If a simple expression name is not in the
        scope of a local variable or parameter, it must represent a single
        accessible field of the innermost type with the field } {
    empty_class T6561f1 {
        int i = no_such_field;
    }
} FAIL

tcltest::test 6.5.6.1-field-2 { If a simple expression name is not in the
        scope of a local variable or parameter, it must represent a single
        accessible field of the innermost type with the field } {
    empty_class T6561f2 {
        int i;
        int j = i;
    }
} PASS

tcltest::test 6.5.6.1-field-3 { If a simple expression name is not in the
        scope of a local variable or parameter, it must represent a single
        accessible field of the innermost type with the field } {
    empty_class T6561f3 {
        int i;
        class Inner {
            Object i;
            void foo() {
                i = null; // refers to Object Inner.i, not int T6561f3.i
            }
        }
    }
} PASS

tcltest::test 6.5.6.1-field-4 { If a simple expression name is not in the
        scope of a local variable or parameter, it must represent a single
        accessible field of the innermost type with the field } {
    empty_class T6561f4 {
        int i;
        class Super {
            Object i;
        }
        class Sub extends Super {
            void foo() {
                // refers to inherited Object Super.i, not int T6561f3.i
                i = null;
            }
        }
    }
} PASS

tcltest::test 6.5.6.1-field-5 { If a simple expression name is not in the
        scope of a local variable or parameter, it must represent a single
        accessible field of the innermost type with the field } {
    empty_class T6561f5 {
        class C {
            int i;
        }
        interface I {
            int i = 1;
        }
        class Sub extends C implements I {
            int j = i; // i is ambiguous between C.i, I.i
        }
    }
} FAIL

tcltest::test 6.5.6.1-field-6 { If a simple expression name is not in the
        scope of a local variable or parameter, it must represent a single
        accessible field of the innermost type with the field } {
    compile [saveas p1/T6561f6c.java {
package p1;
class T6561f6a {
    int i;
}
interface T6561f6b {
    int i = 1;
}
public class T6561f6c extends T6561f6a implements T6561f6b {}
}] [saveas T6561f6d.java {
class T6561f6d extends p1.T6561f6c {
    int j = i; // only T6561f6b.i is inherited
}
}]
} PASS

tcltest::test 6.5.6.1-instance-1 { An expression name which resolves to an
        instance field must not occur in a static context } {
    empty_class T6561i1 {
        int i;
        static { i++; }
    }
} FAIL

tcltest::test 6.5.6.1-instance-2 { An expression name which resolves to an
        instance field must not occur in a static context } {
    empty_class T6561i2 {
        int i;
        static int j = i;
    }
} FAIL

tcltest::test 6.5.6.1-instance-3 { An expression name which resolves to an
        instance field must not occur in a static context } {
    empty_class T6561i3 {
        int i;
        static void m() { i++; }
    }
} FAIL

tcltest::test 6.5.6.1-instance-4 { An expression name which resolves to an
        instance field must not occur in a static context } {
    empty_class T6561i4 {
        int i;
        T6561i4(int j) {}
        T6561i4() { this(i++); }
    }
} FAIL

tcltest::test 6.5.6.1-explicit-constructor-1 { Cannot access instance fields
        from this class within an explicit constructor } {
    empty_class T6561ec1 {
	int i;
	T6561ec1(int i) {}
	T6561ec1() {
	    this(i);
	}
    }
} FAIL

tcltest::test 6.5.6.1-explicit-constructor-2 { Cannot access instance fields
        from this class within an explicit constructor } {
    empty_class T6561ec2 {
	int i;
	T6561ec2(int i) {}
	class Sub extends T6561ec2 {
	    Sub() {
		super(i); // i is inherited
	    }
	}
    }
} FAIL

tcltest::test 6.5.6.1-explicit-constructor-3 { Cannot access instance fields
        from this class within an explicit constructor } {
    empty_class T6561ec3 {
	int i;
	class Inner {
	    Inner(int i) {}
	    Inner() {
		this(i); // i is not inherited, and this$0.i is available
	    }
	}
    }
} PASS

tcltest::test 6.5.6.1-explicit-constructor-4 { Cannot access instance fields
        from this class within an explicit constructor } {
    empty_class T6561ec4 {
	private int i;
	T6561ec4(int i) {}
	class Sub extends T6561ec4 {
	    Sub() {
		super(i); // i is not inherited, so it is the enclosing i
	    }
	}
    }
} PASS

tcltest::test 6.5.6.1-explicit-constructor-5 { Cannot access instance fields
        from this class within an explicit constructor } {
    compile [saveas p1/T6561ec5a.java {
package p1;
public class T6561ec5a {
    int i;
    class C extends p2.T6561ec5b {
	C(int j) {}
	C() {
	    // although c is a subclass of a, it does not inherit i, since
	    // its superclass is in a different package
	    this(i);
	}
    }
}
    }] [saveas p2/T6561ec5b.java {
package p2;
public class T6561ec5b extends p1.T6561ec5a {}
    }]
} PASS
