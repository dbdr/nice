# Note that the Java Spec Report prefers to call these "Interface Fields",
# not "Constants", since they are not necessarily constant by the formal
# definition in JLS 15.28.
tcltest::test 9.3-modifier-1 { An interface field may be redundantly public } {
    ok_pass_or_warn [empty_class T93m1 "interface I { public int i = 1; }"]
} OK

tcltest::test 9.3-modifier-2 { An interface field may not have public twice } {
    empty_class T93m2 "interface I { public public int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-3 { An interface field may be redundantly static } {
    ok_pass_or_warn [empty_class T93m3 "interface I { static int i = 1; }"]
} OK

tcltest::test 9.3-modifier-4 { An interface field may not have static twice } {
    empty_class T93m4 "interface I { static static int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-5 { An interface field may be redundantly final } {
    ok_pass_or_warn [empty_class T93m5 "interface I { final int i = 1; }"]
} OK

tcltest::test 9.3-modifier-6 { An interface field may not have final twice } {
    empty_class T93m6 "interface I { final final int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-7 { An interface field may not be protected } {
    empty_class T93m7 "interface I { protected int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-8 { An interface field may not be private } {
    empty_class T93m8 "interface I { private int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-9 { An interface field may not be abstract } {
    empty_class T93m9 "interface I { abstract int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-10 { An interface field may not be transient } {
    empty_class T93m10 "interface I { transient int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-11 { An interface field may not be volatile } {
    empty_class T93m11 "interface I { volatile int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-12 { An interface field may not be strictfp } {
    empty_class T93m12 "interface I { strictfp int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-13 { An interface field may not be native } {
    empty_class T93m13 "interface I { native int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-14 { An interface field may not be synchronized } {
    empty_class T93m14 "interface I { synchronized int i = 1; }"
} FAIL

tcltest::test 9.3-modifier-15 { Longest legal modifier sequence } {
    ok_pass_or_warn [empty_class T93m15 {
        interface I { public static final int i = 1; }
    }]
} OK

tcltest::test 9.3-modifier-16 { Stress test } {
    empty_class T93m16 {
        interface I {
            public static final protected private abstract transient volatile
            strictfp native synchronized public static final int i = 1;
        }
    }
} FAIL

tcltest::test 9.3-conflict-1 { An interface may not declare the same field
        twice } {
    empty_class T93c1 "interface I { int i = 1; int i = 2; }"
} FAIL

tcltest::test 9.3-conflict-2 { An interface may inherit a field with the same
        name twice, provided it doesn't either one by simple name } {
    empty_class T93c2 {
        interface I1 { int i = 1; }
        interface I2 { int i = 2; }
        interface I3 extends I1, I2 {}
    }
} PASS

tcltest::test 9.3-conflict-3 { An interface may inherit a field with the same
        name twice, but may not use it, even if both have the same value } {
    empty_class T93c3 {
        interface I1 { int i = 1; }
        interface I2 { int i = 1; }
        interface I3 extends I1, I2 { int j = i; }
    }
} FAIL

tcltest::test 9.3-conflict-4 { A field inherited through two paths is not
        ambiguous } {
    empty_class T93c4 {
        interface I1 { int i = 1; }
        interface I2 extends I1 {}
        interface I3 extends I1, I2 { int j = i; }
    }
} PASS
