# see more tests in 15.12

tcltest::test 6.5.7.1-simple-1 { A simple method name must name a method in
        the current type } {
    empty_class T6571s1 {
        int foo;
        int i = foo();
    }
} FAIL

tcltest::test 6.5.7.1-simple-2 { A simple method name must name a method in
        the current type } {
    empty_class T6571s2 {
        int foo() { return 1; }
        int i = foo();
    }
} PASS

