# see more tests in 15.12

tcltest::test 6.5.7.2-package-1 { A qualified method name may not be
        qualified by a package name } {
    empty_class T6572p1 {
        T6572p1 foo() { return null; }
        int m() { return 1; }
        int i = foo.m(); // foo was classified as a (non-existant) package name
    }
} FAIL

tcltest::test 6.5.7.2-package-2 { A qualified method name may not be
        qualified by a package name } {
    empty_class T6572p2 {
        String s = java.toString(); // java was classified as a package name
    }
} FAIL

tcltest::test 6.5.7.2-type-1 { A method name qualified by a type name must
        name at least one static method in the type } {
    empty_class T6572t1 {
        String s = Object.toString();
    }
} FAIL

tcltest::test 6.5.7.2-type-2 { A method name qualified by a type name must
        name at least one static method in the type } {
    empty_class T6572t2 {
        String s = T6572t2.noSuchMethod();
    }
} FAIL

tcltest::test 6.5.7.2-type-3 { A method name qualified by a type name must
        name at least one static method in the type } {
    empty_class T6572t3 {
        static int m() { return 1; }
        int i = T6572t3.m();
    }
} PASS

tcltest::test 6.5.7.2-expression-1 { A method name qualified by an expression
        name must name at least one method in the type of the expression} {
    empty_class T6572e1 {
        T6572e1 t;
        String s = t.noSuchMethod();
    }
} FAIL

tcltest::test 6.5.7.2-expression-2 { A method name qualified by an expression
        name must name at least one method in the type of the expression} {
    empty_class T6572e2 {
        static int m() { return 1; }
        T6572e2 t;
        int i = t.m();
    }
} PASS

tcltest::test 6.5.7.2-expression-3 { A method name qualified by an expression
        name must name at least one method in the type of the expression} {
    empty_class T6572e3 {
        int t;
        int i = t.toString(); // primitive types have no members
    }
} FAIL

