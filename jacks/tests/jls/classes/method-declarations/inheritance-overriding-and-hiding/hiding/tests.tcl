tcltest::test 8.4.6.2-hiding-1 { static methods may not override instance } {
    empty_class T8462h1 {
	static class One {
	    void m() {}
	}
	static class Two extends One {
	    static void m() {}
	}
    }
} FAIL

tcltest::test 8.4.6.2-hiding-2 { overloading does not affect hiding } {
    empty_class T8462h2 {
	static class One {
	    void m() {}
	}
	static class Two extends One {
	    static int m(int i) throws Exception { return 1; }
	}
	static class Three extends Two {
	    {
		m();
		this.m();
		try {
		    m(1);
		    this.m(1);
		} catch (Exception e) {
		}
	    }
	}
    }
} PASS

tcltest::test 8.4.6.2-hiding-3 { static methods cannot hide what is not
        accessible in superclass } {
    compile [saveas p1/T8462h3a.java {
package p1;
public class T8462h3a {
    int m() { return 1; }
}
    }] [saveas T8462h3b.java {
class T8462h3b extends p1.T8462h3a {
    static void m() throws Exception {}
}
    }]
} PASS

tcltest::test 8.4.6.2-hiding-4 { static methods cannot hide what is not
        accessible in superclass } {
    compile [saveas p1/T8462h4a.java {
package p1;
public class T8462h4a {
    void m() {}
}
class T8462h4c extends p2.T8462h4b {
    // if this were not static, it would override a.m. Therefore, there is
    // a conflict, and this static method is hiding an instance method, even
    // though a.m is not inherited
    static void m() {}
}
    }] [saveas p2/T8462h4b.java {
package p2;
public class T8462h4b extends p1.T8462h4a {}
    }]
} FAIL

tcltest::test 8.4.6.2-hiding-5 { static methods cannot implement abstract
        ones } {
    empty_class T8462h5 {
	interface I {
	    void m();
	}
	static class C implements I {
	    public static void m() {}
	}
    }
} FAIL
