tcltest::test 16.8-constructor-1 { A blank final instance variable is DA
        after alternate constructor invocation } {
    empty_class T168c1 {
        final int i;
        T168c1() {
            this(1);
            i = 1;
        }
        T168c1(int i) {
            i = 1;
        }
    }
} FAIL

tcltest::test 16.8-constructor-2 { A blank final instance variable is DA
        after alternate constructor invocation } {
    empty_class T168c2 {
        final int i;
        T168c2() {
            this(1);
        }
        T168c2(int j) {
            i = 1;
        }
    }
} PASS

tcltest::test 16.8-constructor-3 { A blank final instance variable is DA
        after superconstructor if it is DA after initializers } {
    empty_class T168c3 {
        final int i;
        static {}
        Object o = new Object();
        T168c3() {
            i = 1;
        }
    }
} PASS

tcltest::test 16.8-constructor-4 { A blank final instance variable is DA
        after superconstructor if it is DA after initializers } {
    empty_class T168c4 {
        final int i;
        Object o = new Integer(i = 1);
        T168c4() {
            i = 1;
        }
    }
} FAIL

tcltest::test 16.8-constructor-5 { A blank final instance variable is DA
        after superconstructor if it is DA after initializers } {
    empty_class T168c5 {
        final int i;
        Object o = new Object();
        { i = 1; }
        T168c5() {
            i = 1;
        }
    }
} FAIL

tcltest::test 16.8-constructor-6 { A blank final must be DU before assignment
        in an instance initializer } {
    empty_class T168c6 {
	final int i;
	{ if (false) i = 1; }
	int j = i = 1;
    }
} FAIL

tcltest::test 16.8-constructor-7 { A blank final must be DU before assignment
        in an instance initializer } {
    empty_class T168c7 {
	final int i;
	{ if (false) i = 1; }
	{ i = 1; }
    }
} FAIL

tcltest::test 16.8-constructor-8 { A blank final must be DU before assignment
        in a constructor } {
    empty_class T168c8 {
	final int i;
	{ if (false) i = 1; }
	T168c8() { i = 1; }
    }
} FAIL

tcltest::test 16.8-constructor-9 { A blank final must be DA after every
        constructor } {
    empty_class T168c9 {
	final int i;
	{ if (false) i = 1; }
	T168c9() { if (false) i = 1; }
    }
} FAIL

tcltest::test 16.8-parameter-1 { Final parameters may not be assigned in a
        constructor } {
    empty_class T168p1 {
	class Super {
	    Super(int i) {}
	}
	class Sub extends Super {
	    Sub(final int i) {
		super(i = 1);
	    }
	}
    }
} FAIL

tcltest::test 16.8-parameter-2 { Final parameters may not be assigned in a
        constructor } {
    empty_class T168p2 {
	T168p2(int i) {}
	T168p2(final short s) {
	    this((int) (s = 1));
	}
    }
} FAIL

tcltest::test 16.8-parameter-3 { Final parameters may not be assigned in a
        constructor } {
    empty_class T168p3 {
	class Super {}
	class Sub extends Super {
	    Sub(final boolean b) {
		((b = true) ? new T168p3() : null).super();
	    }
	}
    }
} FAIL

