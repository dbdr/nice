tcltest::test 15.12.3-explicit-constructor-1 { Cannot access instance methods
        from this class within an explicit constructor } {
    empty_class T15123ec1 {
	int m() { return 1; }
	T15123ec1(int i) {}
	T15123ec1() {
	    this(m());
	}
    }
} FAIL

tcltest::test 15.12.3-explicit-constructor-2 { Cannot access instance methods
        from this class within an explicit constructor } {
    empty_class T15123ec2 {
	int m() { return 1; }
	T15123ec2(int i) {}
	class Sub extends T15123ec2 {
	    Sub() {
		super(m()); // m is inherited
	    }
	}
    }
} FAIL

tcltest::test 15.12.3-explicit-constructor-3 { Cannot access instance methods
        from this class within an explicit constructor } {
    empty_class T15123ec3 {
	int m() { return 1; }
	class Inner {
	    Inner(int i) {}
	    Inner() {
		this(m()); // m is not inherited, and this$0.m is available
	    }
	}
    }
} PASS

tcltest::test 15.12.3-explicit-constructor-4 { Cannot access instance methods
        from this class within an explicit constructor } {
    empty_class T15123ec4 {
	int m() { return 1; }
	T15123ec4(int i) {}
	class Sub extends T15123ec4 {
	    Sub() {
		// calling the enclosing m, not the inherited m, is legal
		super(T15123ec4.this.m());
	    }
	}
    }
} PASS

tcltest::test 15.12.3-explicit-constructor-5 { Cannot access instance methods
        from this class within an explicit constructor } {
    empty_class T15123ec5 {
	int m() { return 1; }
	class Inner {
	    Inner(int i) {}
	    Inner() {
		// explicit mention of the only version of m
		this(T15123ec5.this.m());
	    }
	}
    }
} PASS

tcltest::test 15.12.3-explicit-constructor-6 { Cannot access instance methods
        from this class within an explicit constructor } {
    empty_class T15123ec6 {
	int m() { return 1; }
	T15123ec6(int i) {}
	class Sub extends T15123ec6 {
	    Sub() {
		// calling superclass version of inherited m is wrong
		super(super.m());
	    }
	}
    }
} FAIL

tcltest::test 15.12.3-explicit-constructor-7 { Cannot access instance methods
        from this class within an explicit constructor } {
    empty_class T15123ec7 {
	class One {
	    int m() { return 1; }
	}
	class Two extends One {
	    class Inner {
		Inner(int i) {}
		Inner() {
		    // calling super method of enclosing class is legal
		    this(Two.super.m());
		}
	    }
	}
    }
} PASS

tcltest::test 15.12.3-explicit-constructor-8 { Cannot access instance methods
        from this class within an explicit constructor } {
    empty_class T15123ec8 {
	abstract class One {
	    abstract int m();
	}
	class Two extends One {
	    int m() { return 1; }
	    class Inner {
		Inner(int i) {}
		Inner() {
		    // super method of enclosing class can't be abstract
		    this(Two.super.m());
		}
	    }
	}
    }
} FAIL

tcltest::test 15.12.3-explicit-constructor-9 { Cannot access instance methods
        from this class within an explicit constructor } {
    empty_class T15123ec9 {
	private int m() { return 1; }
	T15123ec9(int i) {}
	class Sub extends T15123ec9 {
	    Sub() {
		super(m()); // m is not inherited, so it is the enclosing m
	    }
	}
    }
} PASS

tcltest::test 15.12.3-explicit-constructor-10 { Cannot access instance methods
        from this class within an explicit constructor } {
    compile [saveas p1/T15123ec10a.java {
package p1;
public class T15123ec10a {
    int m() { return 1; }
    class C extends p2.T15123ec10b {
	C(int j) {}
	C() {
	    // c does not inherit m(), so use the enclosing version
	    this(m());
	}
    }
}
    }] [saveas p2/T15123ec10b.java {
package p2;
public class T15123ec10b extends p1.T15123ec10a {}
    }]
} PASS
