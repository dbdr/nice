# This first batch is covered in 8.8.5, but belongs here.

tcltest::test 8.8.5.1-before-block-statements-this-1 { An
        ExplicitConstructorInvocation must appear
        before the optional BlockStatements } {
    empty_class T8851bbst1 {
T8851bbst1() {}
T8851bbst1(int i) { this(); {} }
     }
} PASS

tcltest::test 8.8.5.1-before-block-statements-this-2 { An
        ExplicitConstructorInvocation must appear
        before the optional BlockStatements } {
    empty_class T8851bbst2 {
T8851bbst2() {}
T8851bbst2(int i) { {} this(); }
     }
} FAIL

tcltest::test 8.8.5.1-before-block-statements-this-3 { An
        ExplicitConstructorInvocation must appear
        before the optional BlockStatements } {
    empty_class T8851bbst3 {
T8851bbst3() {}
T8851bbst3(int i) { ; this(); }
     }
} FAIL


tcltest::test 8.8.5.1-before-block-statements-super-1 { An
        ExplicitConstructorInvocation must appear
        before the optional BlockStatements } {
    empty_class T8851bbss1 {
T8851bbss1(int i) { super(); {} }
     }
} PASS

tcltest::test 8.8.5.1-before-block-statements-super-2 { An
        ExplicitConstructorInvocation must appear
        before the optional BlockStatements } {
    empty_class T8851bbss2 {
T8851bbss2(int i) { {} super(); }
     }
} FAIL

tcltest::test 8.8.5.1-before-block-statements-super-3 { An
        ExplicitConstructorInvocation must appear
        before the optional BlockStatements } {
    empty_class T8851bbss3 {
T8851bbss3(int i) { ; super(); }
     }
} FAIL




tcltest::test 8.8.5.1-multiple-this-invocations-1 { It is
        a compile-time error for a constructor to invoke itself } {
    empty_class T8851mti1 {
T8851mti1() { this(); }
     }
} FAIL

tcltest::test 8.8.5.1-multiple-this-invocations-2 { It is
        a compile-time error for a constructor to indirectly
        invoke itself through a series of one or more
        explicit constructor invocations involving this } {
    empty_class T8851mti2 {
T8851mti2() { this(0); }
T8851mti2(int i) { this(); }
     }
} FAIL

tcltest::test 8.8.5.1-multiple-this-invocations-3 { It is
        a compile-time error for a constructor to indirectly
        invoke itself through a series of one or more
        explicit constructor invocations involving this } {
    empty_class T8851mti3 {
T8851mti3() { this(0); }
T8851mti3(int i) { this("noggy"); }
T8851mti3(String s) { this(true); }
T8851mti3(boolean state) { this(); }
     }
} FAIL



# Compiler implicitly supplies a call to super()

tcltest::test 8.8.5.1-accessible-implicit-super-invocation-1 {
        If a constructor body does not begin with an explicit
        constructor invocation, then the constructor
        body is implicitly assumed by the compiler to
        begin with a superclass constructor invocation } {
    empty_class T8851aisi1 {
T8851aisi1(int i) {}
     }
} PASS

tcltest::test 8.8.5.1-accessible-implicit-super-invocation-2 {
        An implicit constructor for an inner class can gain
        access to a private constructor in the enclosing class } {
    empty_class T8851aisi2 { 
    private T8851aisi2() {}

    static class T8851aisi2_Inner extends T8851aisi2 {
        T8851aisi2_Inner() {}
    }
    }
} PASS

tcltest::test 8.8.5.1-accessible-implicit-super-invocation-3 {
        An implicit constructor for an inner class can gain
        access to a private constructor in the enclosing class } {
    empty_class T8851aisi3 { 
    private T8851aisi3() {}

    class T8851aisi3_Inner extends T8851aisi3 {
        T8851aisi3_Inner() {}
    }
    }
} PASS


# Explicit call to super() made in constructor

tcltest::test 8.8.5.1-accessible-explicit-super-invocation-1 {
        explicit superclass constructor invocation } {
    empty_class T8851aesi1 {
T8851aesi1(int i) { super(); }
     }
} PASS

tcltest::test 8.8.5.1-accessible-explicit-super-invocation-2 {
        An explicit constructor for an inner class can gain
        access to a private constructor in the enclosing class } {
    empty_class T8851aesi2 { 
    private T8851aesi2() {}

    static class T8851aesi2_Inner extends T8851aesi2 {
        T8851aesi2_Inner() { super(); }
    }
    }
} PASS

tcltest::test 8.8.5.1-accessible-explicit-super-invocation-3 {
        An explicit constructor for an inner class can gain
        access to a private constructor in the enclosing class } {
    empty_class T8851aesi3 { 
    private T8851aesi3() {}

    class T8851aesi3_Inner extends T8851aesi3 {
        T8851aesi3_Inner() { super(); }
    }
    }
} PASS


# Explicit call to super() in constructor with arguments

tcltest::test 8.8.5.1-accessible-explicit-super-invocation-args-1 {
        explicit superclass constructor invocation } {

    saveas T8851aesia1.java {
class T8851aesia1_super {
    T8851aesia1_super(int i) {}
}
class T8851aesia1 extends T8851aesia1_super {
    T8851aesia1() { super(1); }
}
}

    compile T8851aesia1.java
} PASS

tcltest::test 8.8.5.1-accessible-explicit-super-invocation-args-2 {
        explicit superclass constructor invocation } {

    saveas T8851aesia2.java {
class T8851aesia2_super {
    T8851aesia2_super(Object o) {}
}
class T8851aesia2 extends T8851aesia2_super {
    T8851aesia2() { super(null); }
}
}

    compile T8851aesia2.java
} PASS

tcltest::test 8.8.5.1-accessible-explicit-super-invocation-args-3 {
        a private constructor in an enclosing class is accessible } {

    saveas T8851aesia3.java {
class T8851aesia3_super {
    private T8851aesia3_super(int i) {}

    static class T8851aesia3 extends T8851aesia3_super {
        T8851aesia3() { super(1); }
    }
}
}

    compile T8851aesia3.java
} PASS


tcltest::test 8.8.5.1-accessible-explicit-super-invocation-args-4 {
        a private constructor in an enclosing class is accessible } {

    saveas T8851aesia4.java {
class T8851aesia4_super {
    private T8851aesia4_super(Object o) {}

    static class T8851aesia4 extends T8851aesia4_super {
        T8851aesia4() { super(null); }
    }
}
}

    compile T8851aesia4.java
} PASS


tcltest::test 8.8.5.1-accessible-explicit-super-invocation-args-5 {
        pass lots of types to multiple accessible constructors } {

    saveas T8851aesia5.java {
class T8851aesia5_super {
    private T8851aesia5_super(Object o) {}
    private T8851aesia5_super(String s) {}
    private T8851aesia5_super(boolean state) {}
    private T8851aesia5_super(int i) {}
    private T8851aesia5_super(long l) {}
    private T8851aesia5_super(float f) {}
    private T8851aesia5_super(double d) {}
    private T8851aesia5_super(String s, char c) {}
    private T8851aesia5_super(double d, short s) {}
    private T8851aesia5_super(byte b, float f) {}

    static class T8851aesia5_o extends T8851aesia5_super {
        T8851aesia5_o() { super( new Object() ); }
    }
    static class T8851aesia5_s extends T8851aesia5_super {
        T8851aesia5_s() { super("hello"); }
    }
    class T8851aesia5_b extends T8851aesia5_super {
        T8851aesia5_b() { super(true); }
    }
    class T8851aesia5_i extends T8851aesia5_super {
        T8851aesia5_i() { super(1); }
    }
    class T8851aesia5_f extends T8851aesia5_super {
        T8851aesia5_f() { super(0.0F); }
    }
    class T8851aesia5_d extends T8851aesia5_super {
        T8851aesia5_d() { super(0.0D); }
    }
    Object l_obj = new T8851aesia5_super(0L) {
        void foo() {}
    };
    Object sb_obj = new T8851aesia5_super("hello", 'b') {
        void foo() {}
    };
    Object bf_obj = new T8851aesia5_super((byte) 2, 1.0F) {
        void foo() {}
    };
    
}
}

    compile T8851aesia5.java
} PASS





# inaccessible super class constructors

tcltest::test 8.8.5.1-inaccessible-implicit-super-invocation-1 {
        A private constructor is not accessible in a subclass } {
    saveas T8851iisi1.java {
class T8851iisi1_super {
    private T8851iisi1_super() {}
}

class T8851iisi1 extends T8851iisi1_super {
    T8851iisi1() {}
}
}

    compile T8851iisi1.java
} FAIL


tcltest::test 8.8.5.1-inaccessible-explicit-super-invocation-1 {
        A private constructor is not accessible in a subclass } {
    saveas T8851iesi1.java {
class T8851iesi1_super {
    private T8851iesi1_super() {}
}

class T8851iesi1 extends T8851iesi1_super {
    T8851iesi1() { super(); }
}
}

    compile T8851iesi1.java
} FAIL



tcltest::test 8.8.5.1-inaccessible-explicit-super-invocation-args-1 {
        A private constructor is not accessible in a subclass } {
    saveas T8851iesia1.java {
class T8851iesia1_super {
    private T8851iesia1_super(int i) {}
}

class T8851iesia1 extends T8851iesia1_super {
    T8851iesia1() { super(1); }
}
}

    compile T8851iesia1.java
} FAIL

tcltest::test 8.8.5.1-inaccessible-explicit-super-invocation-args-2 {
        A private constructor is not accessible in a subclass } {
    saveas T8851iesia2.java {
class T8851iesia2_super {
    private T8851iesia2_super(Object o) {}
}

class T8851iesia2 extends T8851iesia2_super {
    T8851iesia2() { super(null); }
}
}

    compile T8851iesia2.java
} FAIL





# The batch of tests below are explicitly mentioned in 8.8.5.1

tcltest::test 8.8.5.1-example-1 { Example of qualified superconstructor } {
    empty_class T8851e1 {
	static class Outer {
	    class Inner {}
	}
	static class ChildOfInner extends Outer.Inner {
	    ChildOfInner() {
		(new Outer()).super();
	    }
	}
    }
} PASS

tcltest::test 8.8.5.1-example-2 { Example of error accessing instance field } {
    empty_class T8851e2 {
	static class Point {
	    int x, y;
	    Point(int x, int y) {
		this.x = x;
		this.y = y;
	    }
	}
	static class ColoredPoint extends Point {
	    int color;
	    ColoredPoint(int x, int y) {
		this(x, y, color);
	    }
	}
    }
} FAIL

tcltest::test 8.8.5.1-example-3 { Example of error accessing instance field
        when anonymous class is in explicit constructor invocation } {
    empty_class T8851e3 {
	static class Top {
	    int x;
	    class Dummy {
		Dummy(Object o) {}
	    }
	    class Inside extends Dummy {
		Inside() {
		    super(new Object() { int r = x; }); // error
		}
	    }
	}
    }
} FAIL

tcltest::test 8.8.5.1-example-4 { Example of legal access of parameter
        when anonymous class is in explicit constructor invocation } {
    empty_class T8851e4 {
	static class Top {
	    int x;
	    class Dummy {
		Dummy(Object o) {}
	    }
	    class Inside extends Dummy {
		Inside(final int y) {
		    super(new Object() { int r = y; }); // correct
		}
	    }
	}
    }
} PASS

# FIXME: Add tests for Superclass constructor invocations



tcltest::test 8.8.5.1-alternate-constructor-invocation-1 {
        An alternate constructor invocation invokes a
        constructor of the same class } {
    empty_class T8851aci1 {
T8851aci1() { this(0); }
T8851aci1(int i) {}
     }
} PASS

tcltest::test 8.8.5.1-alternate-constructor-invocation-2 {
        An alternate constructor invocation can invoke
        a chain of constructors in the same class } {
    empty_class T8851aci2 {
T8851aci2() { this(0); }
T8851aci2(int i) { this("noggy"); }
T8851aci2(String s) { this(true); }
T8851aci2(boolean state) { }
     }
} PASS

tcltest::test 8.8.5.1-alternate-constructor-invocation-3 {
        An alternate constructor must exist } {
    empty_class T8851aci3 {
T8851aci3(int i) { this(); }
     }
} FAIL



# FIXME: add tests for instance variables in explicit constructor invocation statement

# FIXME: add tests for instance methods in explicit constructor invocation statement

# FIXME: add tests for above in anonymous class instance creation expression

tcltest::test 8.8.5.1-qualified-1 { Qualified explicit invocation allows one
        to specify the enclosing instance of the superclass } {
    compile [saveas T8851q1.java {
class T8851q1 {
    class Inner {}
}
class Sub1 extends T8851q1.Inner {
    Sub1() {
        new T8851q1().super();
    }
}
    }]
} PASS

tcltest::test 8.8.5.1-qualified-2 { Qualified explicit invocation allows one
        to specify the enclosing instance of the superclass } {
    compile [saveas T8851q2.java {
class T8851q2 {
    class Inner {}
}
class Sub2 extends T8851q2.Inner {
    Sub2() {
        new T8851q2(){}.super();
    }
}
    }]
} PASS

tcltest::test 8.8.5.1-qualified-3 { Qualified explicit invocation allows one
        to specify the enclosing instance of the superclass } {
    compile [saveas T8851q3.java {
class T8851q3 {
    class Inner {}
}
class Sub3 extends T8851q3.Inner {
    Sub3() {
        // this will not execute, but must compile
        ((T8851q3) null).super();
    }
}
    }]
} PASS

tcltest::test 8.8.5.1-qualified-4 { Qualified explicit invocation allows one
        to specify the enclosing instance of the superclass } {
    compile [saveas T8851q4.java {
class T8851q4 {
    class Inner {}
}
class Sub4 extends T8851q4.Inner {
    Sub4(T8851q4 t) {
        // using a parameter is legal
        t.super();
    }
}
    }]
} PASS

tcltest::test 8.8.5.1-qualified-5 { Qualified explicit invocation allows one
        to specify the enclosing instance of the superclass } {
    compile [saveas T8851q5.java {
class T8851q5 {
    class Inner {}
}
class Sub5 extends T8851q5.Inner {
    T8851q5 t;
    Sub5() {
        // using a member declared in this class is illegal
        t.super();
    }
}
    }]
} FAIL

tcltest::test 8.8.5.1-qualified-6 { Qualified explicit invocation allows one
        to specify the enclosing instance of the superclass } {
    compile [saveas T8851q6.java {
class T8851q6 {
    class Inner {
        T8851q6 t;
    }
}
class Sub6 extends T8851q6.Inner {
    Sub6() {
        // using an inherited member is not legal
        t.super();
    }
}
    }]
} FAIL

tcltest::test 8.8.5.1-qualified-7 { There is no qualified this() explicit
        constructor invocation } {
    empty_class T8851q7 {
        class Inner {
            Inner() {}
            Inner(int i) {
                new T8851q7().this();
            }
        }
    }
} FAIL

tcltest::test 8.8.5.1-qualified-8 { Qualified explicit constructors must
        have a superclass that is an inner class } {
    empty_class T8851q8 {
        T8851q8() {
            new Object().super();
        }
    }
} FAIL

tcltest::test 8.8.5.1-qualified-9 { Qualified explicit constructors must
        have a superclass that is an inner class } {
    compile [saveas T8851q9.java {
class T8851q9 {
    static class Inner {}
}
class Sub9 extends T8851q9.Inner {
    Sub9() {
        new T8851q9().super();
    }
}
    }]
} FAIL

tcltest::test 8.8.5.1-qualified-10 { Qualified explicit constructors are
        allowed even when the enclosing class of the superclass can be
        determined already } {
    empty_class T8851q10 {
        class One {}
        class Two extends One {
            Two() {
                new T8851q10().super();
            }
            Two(int i) {}
        }
    }
} PASS

tcltest::test 8.8.5.1-qualified-11 { Explicit constructors may reference
        qualified this or super which names an enclosing class } {
    empty_class T8851q11 {
        class A {}
        class B extends A {
            B() {
                T8851q11.this.super();
            }
        }
    }
} PASS

tcltest::test 8.8.5.1-qualified-12 { Explicit constructors may reference
        qualified this or super which names an enclosing class } {
    empty_class T8851q12 {
        class Super {
            Middle m;
        }
        class Middle extends Super {
            class A {}
            class B extends A {
                B() {
                    Middle.super.m.super();
                }
            }
        }
    }
} PASS

tcltest::test 8.8.5.1-qualified-13 { Explicit constructors may reference
        qualified this or super which names an enclosing class } {
    empty_class T8851q13 {
        T8851q13(Object o) {}
        class Middle extends T8851q13 {
            Middle(int i) {
                super(null);
            }
            Middle() {
                // Here, the innermost instance of T8851q13 to enclose
		// new Middle is this
                super(new Middle(1).new Inner() {});
            }
            class Inner {}
        }
    }
} FAIL

tcltest::test 8.8.5.1-qualified-14 { Explicit constructors may reference
        qualified this or super which names an enclosing class } {
    empty_class T8851q14 {
        T8851q14(Object o) {}
        class Middle extends T8851q14 {
            Middle(int i) {
                super(null);
            }
            Middle() {
                // Here, new Middle is a member of Middle
                super(T8851q14.this.new Middle(1).new Inner() {});
            }
            class Inner {}
        }
    }
} PASS

tcltest::test 8.8.5.1-qualified-15 { Explicit constructors may reference
        qualified this or super which names an enclosing class } {
    empty_class T8851q15 {
        T8851q15(Object o) {}
        private class Middle extends T8851q15 {
            Middle(int i) {
                super(null);
            }
            Middle() {
                // Here, the innermost instance of T8851q15 to enclose
		// new Middle is this
                super(new Middle(1).new Inner() {});
            }
            class Inner {}
        }
    }
} FAIL

tcltest::test 8.8.5.1-qualified-16 { Explicit constructors may reference
        qualified this or super which names an enclosing class } {
    empty_class T8851q16 {
        T8851q16(Object o) {}
        private class Middle extends T8851q16 {
            Middle(int i) {
                super(null);
            }
            Middle() {
                // Here, new Middle is a member of T8851q16, since it was
		// private and not inherited as a member of Middle
                super(T8851q16.this.new Middle(1).new Inner() {});
            }
            class Inner {}
        }
    }
} PASS

tcltest::test 8.8.5.1-qualified-17 { Explicit constructors may reference
        qualified this or super which names an enclosing class } {
    compile [saveas T8851q17.java {
class T8851q17 {
    T8851q17(Object o) {}
}
class T8851q17a {
    class Middle extends T8851q17 {
	Middle(int i) {
	    super(null);
	}
	Middle() {
	    // Here, the innermost instance of T8851q17a to enclose
	    // new Middle is T8851q17a.this
	    super(new Middle(1).new Inner() {});
	}
	class Inner {}
    }
}
    }]
} PASS

tcltest::test 8.8.5.1-qualified-18 { Explicit constructors may reference
        qualified this or super which names an enclosing class } {
    compile [saveas T8851q18.java {
class T8851q18 {
    T8851q18(Object o) {}
}
class T8851q18a {
    class Middle extends T8851q18 {
	Middle(int i) {
	    super(null);
	}
	Middle() {
	    // Here, the innermost instance of T8851q18a to enclose
	    // new Middle is T8851q18a.this
	    super(T8851q18a.this.new Middle(1).new Inner() {});
	}
	class Inner {}
    }
}
    }]
} PASS
