tcltest::test 9.1-enclosing-1 { An interface may not have the same simple name
        as an enclosing type } {
    empty_class T91e1 {
	interface T91e1 {}
    }
} FAIL

tcltest::test 9.1-enclosing-2 { An interface may not have the same simple name
        as an enclosing type } {
    empty_class T91e2 {
	static class Foo {
	    interface T91e2 {}
	}
    }
} FAIL

tcltest::test 9.1-enclosing-3 { An interface may not have the same simple name
        as an enclosing type } {
    compile [saveas T91e3.java {
interface T91e3 {
    interface T91e3 {}
}
    }]
} FAIL

tcltest::test 9.1-enclosing-4 { An interface may not have the same simple name
        as an enclosing type } {
    compile [saveas T91e4.java {
interface T91e4 {
    Object o = new T91e4() {}; // anonymous classes have no simple name
}
    }]
} PASS
