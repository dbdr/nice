tcltest::test 8.4.6.1-override-1 { instance methods may not override static } {
    empty_class T8461o1 {
	static class One {
	    static void m() {}
	}
	static class Two extends One {
	    void m() {}
	}
    }
} FAIL

tcltest::test 8.4.6.1-override-2 { overloading does not affect overriding } {
    empty_class T8461o2 {
	static class One {
	    static void m() {}
	}
	static class Two extends One {
	    int m(int i) throws Exception { return 1; }
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

tcltest::test 8.4.6.1-override-3 { instance methods cannot override what is not
        accessible in superclass } {
    compile [saveas p1/T8461o3a.java {
package p1;
public class T8461o3a {
    static int m() { return 1; }
}
    }] [saveas T8461o3b.java {
class T8461o3b extends p1.T8461o3a {
    void m() throws Exception {}
}
    }]
} PASS

tcltest::test 8.4.6.1-override-4 { instance methods cannot override what is not
        accessible in superclass } {
    compile [saveas p1/T8461o4a.java {
package p1;
public class T8461o4a {
    static void m() {}
}
class T8461o4c extends p2.T8461o4b {
    // if a.m were not static, this would override it. Therefore, there is
    // a conflict, and this instance method is overriding a static method, even
    // though a.m is not inherited
    void m() {}
}
    }] [saveas p2/T8461o4b.java {
package p2;
public class T8461o4b extends p1.T8461o4a {}
    }]
} FAIL

tcltest::test 8.4.6.1-override-5 { non-abstract methods implement abstract
        ones that they override } {
    empty_class T8461o5 {
	interface I {
	    void m();
	}
	class C implements I {
	    public void m() {}
	}
    }
} PASS
