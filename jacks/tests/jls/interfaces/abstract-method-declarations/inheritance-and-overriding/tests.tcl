tcltest::test 9.4.1-conflict-1 { An overriding method must not conflict in
        return type or throws clause } {
    empty_class T941c1 {
        interface I1 { void m(); }
        interface I2 extends I1 { int m(); }
    }
} FAIL

tcltest::test 9.4.1-conflict-2 { An overriding method must not conflict in
        return type or throws clause } {
    empty_class T941c2 {
        interface I1 { void m(); }
        interface I2 extends I1 { void m() throws Exception; }
    }
} FAIL

tcltest::test 9.4.1-conflict-3 { An interface must not inherit conflicting
        return types, but throws clauses are ok } {
    empty_class T941c3 {
        interface I1 { void m(); }
        interface I2 { int m(); }
        interface I3 extends I1, I2 {}
    }
} FAIL

tcltest::test 9.4.1-conflict-4 { An interface must not inherit conflicting
        return types, but throws clauses are ok } {
    empty_class T941c4 {
        class E1 extends Exception {}
        class E2 extends Exception {}
        interface I1 { void m() throws E1; }
        interface I2 { void m() throws E2; }
        interface I3 extends I1, I2 {}
    }
} PASS

tcltest::test 9.4.1-override-1 { methods not overridden are inherited } {
    empty_class T941o1 {
        interface I1 {
            void m();
            void m(int i);
        }
        interface I2 extends I1 {
            void m();
        }
        void foo(I2 i) {
            i.m(1);
        }
    }
} PASS

tcltest::test 9.4.1-override-2 { A method may be overridden to have a less
        permissive throws clause } {
    empty_class T941o2 {
        interface I1 { void m() throws Throwable; }
        interface I2 extends I1 { void m() throws Exception; }
    }
} PASS

