tcltest::test 9.4-modifier-1 { interface methods may be redundantly public } {
    ok_pass_or_warn [empty_class T94m1 "interface I { public void m(); }"]
} OK

tcltest::test 9.4-modifier-2 { interface methods may not have public twice } {
    empty_class T94m1 "interface I { public public void m(); }"
} FAIL

tcltest::test 9.4-modifier-3 { interface methods may be redundantly
        abstract } {
    ok_pass_or_warn [empty_class T94m3 "interface I { abstract void m(); }"]
} OK

tcltest::test 9.4-modifier-4 { interface methods may not have abstract
        twice } {
    empty_class T94m4 "interface I { abstract abstract void m(); }"
} FAIL

tcltest::test 9.4-modifier-5 { interface methods may not be protected } {
    empty_class T94m5 "interface I { protected void m(); }"
} FAIL

tcltest::test 9.4-modifier-6 { interface methods may not be private } {
    empty_class T94m6 "interface I { private void m(); }"
} FAIL

tcltest::test 9.4-modifier-7 { interface methods may not be static } {
    empty_class T94m7 "interface I { static void m(); }"
} FAIL

tcltest::test 9.4-modifier-8 { interface methods may not be final } {
    empty_class T94m8 "interface I { final void m(); }"
} FAIL

tcltest::test 9.4-modifier-9 { interface methods may not be transient } {
    empty_class T94m9 "interface I { transient void m(); }"
} FAIL

tcltest::test 9.4-modifier-10 { interface methods may not be volatile } {
    empty_class T94m10 "interface I { volatile void m(); }"
} FAIL

tcltest::test 9.4-modifier-11 { interface methods may not be strictfp } {
    empty_class T94m11 "interface I { strictfp void m(); }"
} FAIL

tcltest::test 9.4-modifier-12 { interface methods may not be native } {
    empty_class T94m12 "interface I { native void m(); }"
} FAIL

tcltest::test 9.4-modifier-13 { interface methods may not be synchronized } {
    empty_class T94m13 "interface I { synchronized void m(); }"
} FAIL

tcltest::test 9.4-modifier-14 { Longest legal modifier sequence } {
    ok_pass_or_warn [empty_class T94m14 {
        interface I { public abstract void m(); }
    }]
} OK

tcltest::test 9.4-modifier-15 { Stress test } {
    empty_class T94m15 {
        interface I {
            public abstract protected private static final transient volatile
            strictfp native synchronized public abstract void m();
        }
    }
} FAIL

tcltest::test 9.4-conflict-1 { An interface may not declare the same method
        twice } {
    empty_class T94c1 "interface I { void m(); void m(); }"
} FAIL

tcltest::test 9.4-conflict-2 { An interface may inherit a method with the same
        name and signature twice } {
    empty_class T94c2 {
        interface I1 { void m(); }
        interface I2 { void m(); }
        interface I3 extends I1, I2 {}
    }
} PASS

tcltest::test 9.4-conflict-3 { An interface may inherit a method with the same
        name and signature twice, and there is no ambiguity in using it } {
    empty_class T94c3 {
        interface I1 { void m(); }
        interface I2 { void m(); }
        interface I3 extends I1, I2 {}
        void foo(I3 i) {
            i.m();
        }
    }
} PASS

tcltest::test 9.4-conflict-4 { A method inherited through two paths is not
        ambiguous } {
    empty_class T94c4 {
        interface I1 { void m(); }
        interface I2 extends I1 {}
        interface I3 extends I1, I2 {}
        void bar(I3 i) {
            i.m();
        }
    }
} PASS

tcltest::test 9.4-abstract-1 { An interface method is abstract, so it must
        not have a body } {
    empty_class T94a1 "interface I { void m() {} }"
} FAIL
