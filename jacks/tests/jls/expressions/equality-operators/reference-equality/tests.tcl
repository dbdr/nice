tcltest::test 15.21.3-type-1 { In reference equality, the two types must be
        cast convertible } {
    empty_main T15213t1 {
	boolean b = new Integer(1) == "";
    }
} FAIL

tcltest::test 15.21.3-type-2 { In reference equality, the two types must be
        cast convertible } {
    empty_main T15213t2 {
	boolean b = new Integer(1) == (Object) "";
	b = (Object) new Integer(1) == "";
    }
} PASS

tcltest::test 15.21.3-type-3 { In reference equality, the two types must be
        cast convertible } {
    empty_class T15213t3 {
	interface I {}
	class C {}
	boolean m(I i, C c) {
	    return i == c;
	}
    }
} PASS

tcltest::test 15.21.3-type-4 { In reference equality, the two types must be
        cast convertible } {
    empty_class T15213t4 {
	interface I {}
	final class C {}
	boolean m(I i, C c) {
	    return i == c;
	}
    }
} FAIL

tcltest::test 15.21.3-type-4 { In reference equality, the two types must be
        cast convertible } {
    empty_class T15213t4 {
	interface I {}
	final class C {}
	boolean m(I i, C c) {
	    return (i == (Object) c) || ((Object) i == c);
	}
    }
} PASS

tcltest::test 15.21.3-type-6 { In reference equality, the two types must be
        cast convertible } {
    empty_class T15213t6 {
	interface I1 {
	    void m();
	}
	interface I2 {
	    int m();
	}
	boolean m(I1 i1, I2 i2) {
	    return i1 == i2;
	}
    }
} FAIL

tcltest::test 15.21.3-type-7 { In reference equality, the two types must be
        cast convertible } {
    empty_class T15213t7 {
	interface I1 {
	    void m();
	}
	interface I2 {
	    int m();
	}
	boolean m(I1 i1, I2 i2) {
	    return ((Object) i1 == i2) || (i1 == (Object) i2);
	}
    }
} PASS

tcltest::test 15.21.3-type-8 { In reference equality, the two types must be
        cast convertible } {
    empty_class T15213t8 {
	interface I {
	    void m();
	}
	class C {
	    int m() { return 1; }
	}
	boolean m(I i, C c) {
	    // Note that no subclass of C can implement i!
	    return i == c;
	}
    }
} PASS

tcltest::test 15.21.3-type-9 { In reference equality, the two types must be
        cast convertible } {
    empty_class T15213t9 {
	boolean m(Cloneable c, int[] i) {
	    return c == i;
	}
    }
} PASS

tcltest::test 15.21.3-type-10 { In reference equality, the two types must be
        cast convertible } {
    empty_class T15213t10 {
	interface I {}
	boolean m(I i, int[] a) {
	    return i == a;
	}
    }
} FAIL

tcltest::test 15.21.3-type-11 { In reference equality, the two types must be
        cast convertible } {
    empty_class T15213t11 {
	interface I {}
	boolean m(I i, int[] a) {
	    return ((Object)i == a) || (i == (Object) a);
	}
    }
} PASS

