tcltest::test 9.3.1-init-1 { All interface fields must have an initializer } {
    compile [saveas T931i1.java {
        interface T931i1 { int i; }
    }]
} FAIL

tcltest::test 9.3.1-init-2 { Keyword this may not appear directly in init } {
    empty_class T931i2 {
        interface I {
            int j = 1;
            int i = this.j;
        }
    }
} FAIL

tcltest::test 9.3.1-init-3 { Keyword super may not appear directly in init } {
    empty_class T931i3 {
        interface I1 { int i = 1; }
        interface I2 extends I1 { int j = super.i; }
    }
} FAIL

tcltest::test 9.3.1-init-4 { Initializer may use anonymous class } {
    empty_class T931i4 {
        interface I { int i = new Object() { int foo() { return 1; } }.foo(); }
    }
} PASS

tcltest::test 9.3.1-init-5 { Initializer may use class literal } {
    empty_class T931i5 "interface I { Class c = I.class; }"
} PASS

# compare to tests for forward references in 8.3.2.3.
tcltest::test 9.3.1-illegal-forward-1 { Simple usage before
        declaration legal if not in declaring class } {
    empty_class T931if1 {
        interface I {
            int i = j;
            int j = 1;
        }
    }
} FAIL

tcltest::test 9.3.1-illegal-forward-2 { Simple usage before
        declaration legal if not in declaring class } {
    empty_class T931if2 {
        interface I {
            int i = i + 1;
        }
    }
} FAIL

tcltest::test 9.3.1-legal-forward-1 { Simple usage before
        declaration legal if not in declaring class } {
    empty_class T931lf1 {
        interface I {
            int i = I.j; // not simple usage
            int j = 1;
        }
    }
} PASS

tcltest::test 9.3.1-legal-forward-2 { Simple usage before
        declaration legal if not in declaring class } {
    empty_class T931lf2 {
        interface I {
            int i = new Object(){ int bar() { return j; } }.bar();
            // not in declaring class
            int j = 1;
        }
    }
} PASS

