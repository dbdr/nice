tcltest::test 8.1-enclosing-1 { A class may not have the same simple name as
        an enclosing type } {
    empty_class T81e1 {
	class T81e1 {}
    }
} FAIL

tcltest::test 8.1-enclosing-2 { A class may not have the same simple name as
        an enclosing type } {
    empty_class T81e2 {
	static class Foo {
	    // even though there is no enclosing instance T81e2, this clashes
	    class T81e2 {}
	}
    }
} FAIL

tcltest::test 8.1-enclosing-3 { A class may not have the same simple name as
        an enclosing type } {
    compile [saveas T81e3.java {
interface T81e3 {
    class T81e3 {}
}
    }]
} FAIL

tcltest::test 8.1-enclosing-4 { A class may not have the same simple name as
        an enclosing type } {
    empty_class T81e4 {
	Object o = new T81e4() {}; // anonymous classes have no simple name
    }
} PASS

tcltest::test 8.1-enclosing-5 { A class may not have the same simple name as
        an enclosing type } {
    empty_main T81e5 {
	class T81e5 {} // local classes have the same restrictions
    }
} FAIL

tcltest::test 8.1-enclosing-6 { A class may not have the same simple name as
        an enclosing type } {
    empty_main T81e6 {
	class Local {
	    class Local {}
	}
    }
} FAIL

tcltest::test 8.1-enclosing-7 { A class may not have the same simple name as
        an enclosing type } {
    empty_main T81e7 {
	class Local {
	    {
		class Local {}
	    }
	}
    }
} FAIL

tcltest::test 8.1-enclosing-8 { A class may not have the same simple name as
        an enclosing type } {
    empty_main T81e8 {
	class Local {
	    Local(Object o) {}
	    Local() {
		this(new Object() {
		    class Local {}
		});
	    }
	}
    }
} FAIL
